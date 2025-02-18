package main

import (
	"bytes"
	"crypto/tls"
	"encoding/json"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"strconv"
	"strings"
	"time"
)

var proxy *httputil.ReverseProxy // The proxy object for redirecting object-storage requests

type storageResult struct {
	Type             string    `json:"type"`
	CreationDateTime time.Time `json:"creation_date_time"`
	Submissions      []string  `json:"submissions"`
	Source           []string  `json:"source"`

	MD5    string `json:"md5"`
	SHA1   string `json:"sha1"`
	SHA256 string `json:"sha256"`

	FileMime string   `json:"file_mime"`
	FileName []string `json:file_name"`

	DomainFQDN      string `json:"domain_fqdn"`
	DomainTLD       string `json:"domain_tld"`
	DomainSubDomain string `json:"domain_sub_domain"`

	IPAddress string `json:"ip_address"`
	IPv6      bool   `json:"ip_v6"`

	EmailAddress       string `json:"email_address"`
	EmailLocalPart     string `json:"email_local_part"`
	EmailDomainPart    string `json:"email_domain_part"`
	EmailSubAddressing string `json:"email_sub_addressing"`

	GenericIdentifier     string `json:"generic_identifier"`
	GenericType           string `json:"generic_type"`
	GenericDataRelAddress string `json:"generic_data_rel_address"`
}

type storageResponse struct {
	ResponseCode int
	Failure      string
	Result       storageResult
}

type myTransport struct {
}

func (t *myTransport) RoundTrip(request *http.Request) (*http.Response, error) {
	// Forwards the given request to storage and executes auto-tasking, if successfull

	// Since accessing the Form-values of the request changes the reader,
	// which cannot be rewinded / seeked, an error would be thrown, if the
	// request was forwarded with the reader at the wrong position.
	// For this reason, the whole body is read and a new reader is created,
	// which can be rewinded.
	var err error
	if request.ContentLength > 1024*1024*int64(conf.MaxUploadSize) {
		respBody := ioutil.NopCloser(bytes.NewBufferString("Upload too large"))
		resp := &http.Response{StatusCode: 413, Body: respBody}
		respBody.Close()
		log.Println("Upload too large")
		return resp, nil
	}
	reqbuf := make([]byte, request.ContentLength)
	_, err = io.ReadFull(request.Body, reqbuf)
	if err != nil {
		log.Printf("Error reading body!", err)
		return nil, err
	}

	reader := bytes.NewReader(reqbuf)
	reqrdr := ioutil.NopCloser(reader)
	request.Body = reqrdr

	defer func() {
		request.Body.Close()
		reqrdr.Close()
	}()

	request.ParseMultipartForm(1024 * 1024 * int64(conf.MaxUploadSize))
	// Read the name and the source from the request, because they can not be
	// reconstructed from storage's response.
	name := request.FormValue("name")
	source := request.FormValue("source")

	username := request.FormValue("username")
	password := request.FormValue("password")

	*request.URL = storageURIStoreSample

	// restore the reader for the body
	reader.Seek(0, 0)

	user, err := authenticate(username, password)
	if err != nil {
		return nil, err
	}

	form, _ := url.ParseQuery(request.URL.RawQuery)
	form.Set("user_id", strconv.Itoa(user.Id))
	request.URL.RawQuery = form.Encode()

	// Do the proxy-request
	trans := http.DefaultTransport.(*http.Transport)
	trans.TLSClientConfig = &tls.Config{InsecureSkipVerify: conf.DisableStorageVerify}

	response, err := http.DefaultTransport.RoundTrip(request)
	if err != nil {
		log.Printf("Error performing proxy-request!", err)
		return nil, err
	}

	// Parse the response. If it was successful, execute automatic tasks
	var resp storageResponse
	buf := make([]byte, response.ContentLength)

	_, err = io.ReadFull(response.Body, buf)
	if err != nil {
		log.Printf("Error reading body!", err)
		return nil, err
	}
	rdr := ioutil.NopCloser(bytes.NewReader(buf))
	defer func() {
		rdr.Close()
		response.Body.Close()
	}()
	json.Unmarshal(buf, &resp)
	log.Printf("%+v\n", resp)
	if resp.ResponseCode == 0 {
		log.Printf("\x1b[0;32mSuccessfully uploaded sample with SHA256: %s\x1b[0m", resp.Result.SHA256)
		// Execute automatic tasks
		for t := range conf.AutoTasks {
			if strings.Contains(resp.Result.FileMime, t) {
				autotasks := conf.AutoTasks[t]
				if len(autotasks) != 0 {
					task := TaskRequest{
						PrimaryURI:   resp.Result.SHA256,
						SecondaryURI: "",
						Filename:     name,
						Tasks:        autotasks,
						Tags:         []string{},
						Attempts:     0,
						Source:       source,
						Download:     true,
					}

					log.Printf("\x1b[0;33mAutomatically executing %+v\x1b[0m\n", task)
					handleOwnTasks([]TaskRequest{task})
				}
			}
		}
	} else {
		log.Printf("\x1b[0;31mUpload failed\x1b[0m\n")
		if response.StatusCode == 200 {
			response.StatusCode = 400 // bad request
		}
	}

	// restore the reader for the body
	response.Body = rdr
	return response, err
}

func httpRequestIncomingSample(w http.ResponseWriter, r *http.Request) {
	proxy.ServeHTTP(w, r)
}
