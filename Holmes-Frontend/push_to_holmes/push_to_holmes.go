package push_to_holmes

import (
	"bufio"
	"bytes"
	"crypto/tls"
	"errors"
	"io"
	"io/ioutil"
	"log"
	"mime/multipart"
	"net/http"
	"os"
	"path/filepath"
	"time"

	"gopkg.in/mgo.v2/bson"
	"sync"

	"encoding/json"
	"fmt"
	"github.com/rakyll/magicmime"
	"net/url"
	"strconv"
	"strings"

	"github.com/jlaffaye/ftp"
)

type critsSample struct {
	Id  bson.ObjectId `json:"_id" bson:"_id,omitempty"`
	MD5 string        `json:"md5"`
}

type Task struct {
	PrimaryURI   string              `json:"primaryURI"`
	SecondaryURI string              `json:"secondaryURI"`
	Filename     string              `json:"filename"`
	Tasks        map[string][]string `json:"tasks"`
	Tags         []string            `json:"tags"`
	Attempts     int                 `json:"attempts"`
	Source       string              `json:"source"`
	Download     bool                `json:"download"`
	Comment      string              `json:"comment"`
}

type config struct {
	// These are the conf that are read in from the command line
	// All that are in here are saved to the log and restored, when resuming a log
	CritsFileServer string
	LocalUpload		bool
	Directory       string
	Comment         string
	Source          string
	MimetypePattern string
	Recursive       bool
	Insecure        bool

	FPath      string
	Tasks      string
	TagsStr    string
	GatewayURI string
	Username   string
	Password   string
	Tasking    bool
	Workers	   int

	FtpServer		string
	FtpDirectory	string
	FtpUsername		string
	FtpPassword		string
}

var (
	processed  map[string]struct{} // if a filename is in this struct, it was processed with code 200
	resumeLog  string
	resume     bool
	logFile    *os.File
	tags       []string
	topLevel   bool
	client     *http.Client
	wg         sync.WaitGroup
	c          chan string
	logC       chan string
	errorStatus int

	conf *config

	debug   *log.Logger
	info    *log.Logger
	warning *log.Logger

	ftpConn	*ftp.ServerConn
)

func worker() {
	for true {
		sample := <-c
		debug.Printf("Working on %s\n", sample)
		name, retCode := copySample(sample)
		logC <- name + "\t" + strconv.Itoa(retCode) + "\n"
		if retCode != 200 {
			errorStatus = retCode
		}
	}
}

func logger() {
	for true {
		line := <-logC
		_, err := logFile.WriteString(line)
		if err != nil {
			debug.Fatal(err)
		}
		wg.Done()
	}
}

func initLogger() {
	var err error
	logC = make(chan string)

	if resumeLog != "" {
		// Resume previously unfinished operation
		resume = true
		processed = make(map[string]struct{})
		log.Println("Resuming...")
		logFile, err = os.OpenFile(resumeLog, os.O_RDWR, 0666)
		if err != nil {
			debug.Fatal("Could not open log-file:\n", err)
		}

		scanner := bufio.NewScanner(logFile)

		// Read conf
		scanner.Scan()
		err = json.Unmarshal([]byte(scanner.Text()), &conf)
		if err != nil {
			debug.Fatal("Could not load conf from previous session:\n", err)
		}

		// build lookup-table to quickly identify, whether a sample was already uploaded
		for scanner.Scan() {
			t := scanner.Text()
			// name -> retcode
			parts := strings.Split(t, "\t")
			retcode, err := strconv.Atoi(parts[1])
			if err != nil {
				warning.Fatal("Couldn't parse logfile:\n", err)
			}
			if retcode == 200 {
				// only files that were already processed successfully are in the map
				processed[parts[0]] = struct{}{}
			}

		}
	} else {
		resume = false
	}

	// prepare the new log-file
	os.Mkdir("log", 0755)
	logFileName := time.Now().Format("log/Holmes-Toolbox_2006-01-02_15:04:05.log")
	info.Println("logging to", logFileName)
	logFile, err = os.OpenFile(logFileName, os.O_CREATE|os.O_WRONLY, 0666)
	if err != nil {
		debug.Fatal("Could not open log-file:\n", err)
	}

	// Write all the commandline-conf to the log-file
	opt, err := json.Marshal(conf)
	if err != nil {
		debug.Fatal(err)
	}
	_, err = logFile.WriteString(string(opt) + "\n")
	if err != nil {
		debug.Fatal(err)
	}

	go logger()
}

/*
config
	/*flag.StringVar(&resumeLog, "resume", "", "Path to the log-file of a previously unfinished operation. If this parameter is used, all the others (except for 'workers') are overwritten with the saved values from the log")
	flag.StringVar(&conf.FPath, "file", "", "File containing a list of samples (MD5, SHAX, CRITs ID) to upload. Files are first searched locally. If they are not found and a CRITs file server is specified, they are taken from there. (optional)")
	flag.StringVar(&conf.Comment, "comment", "", "Comment of submitter")
	flag.StringVar(&conf.Source, "src", "", "Source information for the files")
	flag.BoolVar(&conf.Insecure, "insecure", false, "If set, disables certificate checking")
	flag.BoolVar(&conf.Tasking, "tasking", false, "Specify whether to do sample upload or tasking")
	flag.StringVar(&conf.Username, "user", "", "Your username for authenticating to the master-gateway.")
	flag.StringVar(&conf.Password, "pw", "", "Your password for authenticating to the master-gateway. If this value is not set, you will be prompted for it.")
	flag.StringVar(&conf.GatewayURI, "gateway", "", "The URI of the master-gateway.")
	flag.StringVar(&conf.TagsStr, "tags", "", "The tags for these tasks.")

	// object specific
	flag.StringVar(&conf.CritsFileServer, "cfs", "", "Full URL to your CRITs file server, as a fallback (optional)")
	flag.StringVar(&conf.MimetypePattern, "mime", "", "Only upload files with the specified mime-type (as substring)")
	flag.StringVar(&conf.Directory, "dir", "", "Directory of samples to upload")
	flag.IntVar(&numWorkers, "workers", 1, "Number of parallel workers")
	flag.BoolVar(&conf.Recursive, "rec", false, "If set, the directory specified with \"-dir\" will be iterated recursively")

	// tasking specific
	flag.StringVar(&conf.Tasks, "tasks", "", "The tasks to execute.")

	flag.Parse()
*/
func PushToHolmes() int {
	log.Println("Preparing...")
	errorStatus = 200

	var confPath string
	confPath, _ = filepath.Abs(filepath.Dir(os.Args[0]))
	confPath = filepath.Join(confPath, "/config/frontend.conf")
	//confPath = filepath.Join(confPath, "archive-Holmes-Frontend/config/frontend.conf")

	// Read config
	conf = &config{}
	cfile, _ := os.Open(confPath)
	err := json.NewDecoder(cfile).Decode(&conf)
	if err != nil {
		log.Fatalf("%s: %s", "Couldn't read config file", err)
	}

	// setup logging
	warning = log.New(os.Stderr, "\033[31m[WARNING]\033[0m ", log.Ldate|log.Ltime|log.Lshortfile)
	info = log.New(os.Stdout, "\033[92m[INFO]\033[0m ", log.Ldate|log.Ltime)
	debug = log.New(os.Stdout, "\033[34m[DEBUG]\033[0m ", log.Ldate|log.Ltime|log.Lshortfile)

	if !conf.Tasking {
		//TODO: Enable logging for tasking, as well
		initLogger()
	}

	err = json.Unmarshal([]byte(conf.TagsStr), &tags)
	if err != nil {
		warning.Fatal("Error while parsing list of tags! ", err)
	}

	// setup global http client
	tr := &http.Transport{}
	if conf.Insecure {
		// Disable SSL verification
		tr = &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		}
	}
	client = &http.Client{Transport: tr}

	if conf.FtpServer != "" {
		ftpConn, err := ftp.Dial(conf.FtpServer, ftp.DialWithTimeout(5*time.Second)) //"192.168.0.103:21"
		if err != nil {
			log.Fatal(err.Error())
		}

		err = ftpConn.Login(conf.FtpUsername, conf.FtpPassword)
		if err != nil {
			fmt.Println(err.Error())
		}
		err = ftpConn.ChangeDir(conf.FtpDirectory) // directory from root of server e.g. samples
		if err != nil {
			fmt.Println(err.Error())
		}
	}

	// decide to add new tasks OR upload objects
	if conf.Tasking {
		main_tasking()
	} else {
		main_object()
	}

	//if ftpConn != nil {
	//	if err := ftpConn.Quit(); err != nil {
	//		log.Fatal(err)
	//	}
	//}

	info.Println("==================")
	info.Println("Finished execution")

	return errorStatus
}

func main_tasking() {
	info.Println("Doing tasking...")

	allTasks := make([]Task, 0)
	task := &Task{PrimaryURI: "", SecondaryURI: "", Filename: "", Tasks: nil, Tags: tags, Attempts: 0, Source: "", Comment: conf.Comment, Download: true}

	file, err := os.Open(conf.FPath)
	if err != nil {
		warning.Fatal("Couln't open file containing sample list:", err.Error())
	}
	scanner := bufio.NewScanner(file)
	scanner.Split(bufio.ScanLines)

	err = json.Unmarshal([]byte(conf.Tasks), &task.Tasks)
	if err != nil {
		warning.Fatal("Error while parsing list of tasks:", err)
	}

	// line by line
	for scanner.Scan() {
		t := scanner.Text()
		fmt.Sscanf(t, "%s %s %s", &task.PrimaryURI, &task.Filename, &task.Source)
		allTasks = append(allTasks, *task)
	}
	file.Close()

	jsoned, err := json.Marshal(allTasks)
	if err != nil {
		warning.Fatal("Failed to marshal allTasks:", err)
	}

	debug.Printf("All tasks packed: %+v\n", string(jsoned))

	data := &url.Values{}
	data.Set("task", string(jsoned))
	data.Add("username", conf.Username)
	data.Add("password", conf.Password)

	req, err := http.NewRequest("POST", conf.GatewayURI+"/task/", bytes.NewBufferString(data.Encode()))
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Add("Content-Length", strconv.Itoa(len(data.Encode())))

	resp, err := client.Do(req)
	if err != nil {
		warning.Fatal("Error sending allTasks: ", err)
	}

	tskerrors, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		warning.Fatal("Error reading allTasks response: ", err)
	}

	if string(tskerrors) == "" {
		info.Println("The server returned an empty string (success)")
	} else {
		warning.Println("The server returned the following errors:")
		warning.Println(string(tskerrors))
	}
}

func main_object() {
	info.Println("Uploading objects...")

	c = make(chan string)
	for i := 0; i < conf.Workers; i++ {
		debug.Printf("Starting worker #%d\n", i)
		go worker()
	}

	if conf.FPath != "" {
		file, err := os.Open(conf.FPath)
		if err != nil {
			warning.Println("Couln't open file containing sample list!", err.Error())
			return
		}
		defer file.Close()

		scanner := bufio.NewScanner(file)
		scanner.Split(bufio.ScanLines)
		// line by line
		for scanner.Scan() {
			sample := scanner.Text()
			wg.Add(1)
			if resume {
				_, already_processed := processed[sample]
				if already_processed {
					info.Printf("Skipping sample %s, because it was already uploaded successfully\n", sample)
					logC <- sample + "\t200\n"
					continue
				}
			}
			c <- sample
			//go copySample(scanner.Text())
		}
	}

	if conf.LocalUpload {
		if conf.Directory != "" {
			err := magicmime.Open(magicmime.MAGIC_MIME_TYPE | magicmime.MAGIC_SYMLINK | magicmime.MAGIC_ERROR)
			if err != nil {
				fmt.Println(err)
			} else {
				defer magicmime.Close()
			}

			fullPath, err := filepath.Abs(conf.Directory)

			if err != nil {
				warning.Println("path error:", err)
				return
			}
			topLevel = true
			err = filepath.Walk(fullPath, walkFn)
			if err != nil {
				warning.Println("walk error:", err)
				return
			}
		}
	}

	// Load file from ftp server
	if conf.FtpServer != "" {
		if conf.FtpDirectory != "" {
			err := magicmime.Open(magicmime.MAGIC_MIME_TYPE | magicmime.MAGIC_SYMLINK | magicmime.MAGIC_ERROR)
			if err != nil {
				fmt.Println(err)
			} else {
				defer magicmime.Close()
			}

			entries, err := ftpConn.List("")
			if err != nil {
				fmt.Println(err.Error())
			}

			for _, entry := range entries {
				err = walkFnFtp(entry.Name, entry.Type)
				if err != nil {
					warning.Println("ftp walk error:", err)
				}
			}
		}
	}

	wg.Wait()
}

func walkFn(path string, fi os.FileInfo, err error) error {
	if fi.IsDir() {
		if conf.Recursive {
			return nil
		} else {
			if topLevel {
				topLevel = false
				return nil
			} else {
				return filepath.SkipDir
			}
		}
	}
	if resume {
		_, already_processed := processed[path]
		if already_processed {
			wg.Add(1)
			info.Printf("Skipping sample %s, because it was already uploaded successfully\n", path)
			logC <- path + "\t200\n"
			return nil
		}
	}

	mimetype, err := magicmime.TypeByFile(path)
	if err != nil {
		warning.Println("mimetype error (skipping "+path+"):", err)
		return nil
	}
	if strings.Contains(mimetype, conf.MimetypePattern) {
		info.Println("Adding " + path + " (" + mimetype + ")")
		wg.Add(1)
		c <- path
		return nil
	} else {
		info.Println("Skipping " + path + " (" + mimetype + ")")
		return nil
	}
}

func walkFnFtp(path string, fi ftp.EntryType) error {
	if fi == ftp.EntryTypeFolder {
		if conf.Recursive {
			return nil
		} else {
			if topLevel {
				topLevel = false
				return nil
			} else {
				return filepath.SkipDir
			}
		}
	}
	if resume {
		_, already_processed := processed[path]
		if already_processed {
			wg.Add(1)
			info.Printf("Skipping sample %s, because it was already uploaded successfully\n", path)
			logC <- path + "\t200\n"
			return nil
		}
	}

	if fi == ftp.EntryTypeFile {
		mimetype, err := magicmime.TypeByFile(path)
		if err != nil {
			warning.Println("mimetype error (skipping "+path+"):", err)
			return nil
		}
		if strings.Contains(mimetype, conf.MimetypePattern) {
			info.Println("Adding " + path + " (" + mimetype + ")")
			wg.Add(1)

			info.Println("Downloading " + path)
			r, err := ftpConn.Retr(path)
			if err != nil {
				panic(err)
			}
			buf, err := ioutil.ReadAll(r)
			if err != nil {
				panic(err)
			}

			err = ioutil.WriteFile("push_to_holmes/downloads/" + path, buf, 0644)
			if err != nil {
				warning.Println(err.Error())
			}

			c <- "push_to_holmes/downloads/" + path
			return nil
		} else {
			info.Println("Skipping " + path + " (" + mimetype + ")")
			return nil
		}
	}

	return nil
}

func copySample(name string) (string, int) {
	// set all necessary parameters
	parameters := url.Values{}
	//"user_id": user id of uploader; is filled in by Gateway based on the specified username
	parameters.Add("source", conf.Source)       // (TODO) Gateway should match existing sources (command line argument)
	parameters.Add("name", filepath.Base(name)) // filename
	parameters.Add("date", time.Now().Format(time.RFC3339))
	parameters.Add("comment", conf.Comment) // comment from submitter (command line argument)
	parameters["tags"] = tags
	parameters.Add("username", conf.Username)
	parameters.Add("password", conf.Password)

	request, err := buildRequest(conf.GatewayURI+"/samples/", parameters, name)
	if err != nil {
		warning.Fatal("buildRequest failed:", err.Error())
	}

	resp, err := client.Do(request)
	if err != nil {
		warning.Fatal("sending sample request failed:", err.Error())
	}

	body := &bytes.Buffer{}
	_, err = body.ReadFrom(resp.Body)
	if err != nil {
		warning.Fatal("reading sample request response failed:", err.Error())
	}
	SafeResponseClose(resp)
	//resp.Body.Close()

	info.Println("-----------------------------------------------------")
	info.Println("Uploaded: ", name)
	info.Println("Resp.Code:", resp.StatusCode)
	info.Println("Resp.Body:", body)
	info.Println("-----------------------------------------------------")


	info.Println("Removing", name)
	err = os.Remove(name)
	if err != nil {
		warning.Println(err)
	}

	return name, resp.StatusCode
}

func buildRequest(uri string, params url.Values, hash string) (*http.Request, error) {
	debug.Println("Building request...")

	var r io.Reader

	// check if local file
	r, err := os.Open(hash)
	defer r.(*os.File).Close()

	if err != nil {
		debug.Println("Found non local file", hash)

		// not a local file
		// try to get file from crits file server
		cId := &critsSample{}
		if err := bson.Unmarshal([]byte(hash), cId); err != nil {
			return nil, err
		}
		rawId := cId.Id.Hex()

		resp, err := client.Get(conf.CritsFileServer + "/" + rawId)
		if err != nil {
			return nil, err
		}
		// return if file does not exist
		if resp.StatusCode != 200 {
			return nil, errors.New("Couldn't download file")
		}
		r = resp.Body
		// For files coming from CRITs: TODO: find real name somehow

		defer SafeResponseClose(resp)
	}
	// build Holmes-Storage PUT request
	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)
	part, err := writer.CreateFormFile("sample", hash)
	if err != nil {
		return nil, err
	}

	_, err = io.Copy(part, r)
	if err != nil {
		return nil, err
	}

	for key, valMul := range params {
		for _, val := range valMul {
			err = writer.WriteField(key, val)
			if err != nil {
				return nil, err
			}
		}
	}

	err = writer.Close()
	if err != nil {
		return nil, err
	}

	request, err := http.NewRequest("POST", uri, body)
	if err != nil {
		return nil, err
	}
	request.Header.Add("Content-Type", writer.FormDataContentType())

	return request, nil
}

func SafeResponseClose(r *http.Response) {
	if r == nil {
		return
	}

	io.Copy(ioutil.Discard, r.Body)
	r.Body.Close()
}
