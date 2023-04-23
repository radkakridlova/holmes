package main

import (
	"bytes"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"time"
	"path/filepath"
)

type Result struct {
	Filename         string    `json:"filename"`
	Data             string    `json:"data"`
	MD5              string    `json:"md5"`
	SHA1             string    `json:"sha1"`
	SHA256           string    `json:"sha256"`
	ServiceName      string    `json:"service_name"`
	Tags             []string  `json:"tags"`
	Comment          string    `json:"comment"`
	StartedDateTime  time.Time `json:"started_date_time"`
	FinishedDateTime time.Time `json:"finished_date_time"`
}

// Jednoduchy klient na testovanie posielania suborov do maectoowl webservisu
// prejde vsetky subory v priecinku testfiles a kazdy pripravi a skusi poslat
// rovnako ako ich posiela Holmes-Totem-Dynamic
func main() {
	//file, err := os.Open("test_cuckoo_report_2.json")
	
	currentDirectory, err := os.Getwd()
    if err != nil {
        log.Fatal(err)
    }
	testfilesDirectory := filepath.Join(currentDirectory, "testfiles")
	filepath.Walk(testfilesDirectory, func(path string, info os.FileInfo, err error) error {
        if err != nil {
            log.Fatalf(err.Error())
        }
        // treba obskocit adresar
		if (info.Name() != "testfiles") {
			fmt.Printf("File Name: %s\n", info.Name())
			fileBytes := prepareFileForSending(filepath.Join(testfilesDirectory, info.Name()))
		
			// Toto moze padnut ak sa zabudne nastavit MAEC flag v konfiguracii servisu
			sendToOntology(fileBytes)
		}
		
        return nil
    })
}

func prepareFileForSending(filename string) []byte {
	file, err := os.Open(filename)

	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	results, err := ioutil.ReadAll(file)
	fmt.Println("Results size before marshall: ")
	fmt.Println(binary.Size(results))

	// build the final result obj
	resultMsg, err := json.Marshal(Result{
		Filename:         "test",
		Data:             string(results),//string(resultsJ),
		MD5:              "md5String",
		SHA1:             "sha1String",
		SHA256:           "sha256String",
		ServiceName:      "test",
		Tags:             []string{"tag1","tag2"},
		Comment:          "comment",
		StartedDateTime:  time.Now(),
		FinishedDateTime: time.Now(),
	})
	// fmt.Println("Results message after marshall: ")
	// fmt.Println(binary.Size(resultMsg))

	return resultMsg
}

type TasksCreateResp struct {
	TaskID int `json:"task_id"`
}


func sendToOntology(resultMsg[] byte) {

	//	http://localhost:8081/MaecToOwlWeb/ws/generic
	// Treba dat pozor aby vo fuseki existoval /upload endpoint.
	resp, err := http.Post("http://localhost:8080/maectoowlweb/ws/convert", "application/json", bytes.NewBuffer(resultMsg))
	if err != nil {
		log.Fatalln(err)
	}

	tskerrors, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error reading ontology response: ", err)
	}

	if string(tskerrors) == "" {
		fmt.Println("The server returned an empty string (success)")
	} else {
		fmt.Println("The server returned the following:")
		fmt.Println(string(tskerrors))
	}
}