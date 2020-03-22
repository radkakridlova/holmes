package main

import (
	"fmt"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"os"
)

func main() {
	serverPort := ":8017"
	serverRoot := "web"

	mux := http.NewServeMux()

	fs := http.FileServer(http.Dir(serverRoot))
	mux.Handle("/", http.StripPrefix("/", fs))
	mux.HandleFunc("/upload", uploadFile)

	/*cfg := &tls.Config{
		MinVersion:               tls.VersionTLS12,
		CurvePreferences:         []tls.CurveID{tls.CurveP521, tls.CurveP384, tls.CurveP256},
		PreferServerCipherSuites: true,
		CipherSuites: []uint16{
			tls.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
			tls.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
			tls.TLS_RSA_WITH_AES_256_GCM_SHA384,
			tls.TLS_RSA_WITH_AES_256_CBC_SHA,
		},
	}*/

	srv := &http.Server{
		Addr:         serverPort,
		Handler:      mux,
		/*TLSConfig:    cfg,
		TLSNextProto: make(map[string]func(*http.Server, *tls.Conn, http.Handler), 0),*/
	}


	fmt.Println("Starting HTTP server on port " + serverPort + " with root dir " + serverRoot)
	log.Fatal(srv.ListenAndServe())
}

func uploadFile(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Redirect(w, r, "/", http.StatusSeeOther) // vrati spat na root stranku ak request nieje POST
		return
	}

	file, handle, err := r.FormFile("uploadFile")
	if err != nil {
		fmt.Println(w, "%v", err)
		return
	}
	defer file.Close()

	err = saveFile(w, file, handle) // Ulozenie suboru

	/*if err != nil {
		http.Redirect(w, r, "/", http.StatusCreated) // vrati spat na root stranku ak request nieje POST
	} else {
		http.Redirect(w, r, "/", http.StatusInternalServerError) // vrati spat na root stranku ak request nieje POST
	}*/

	/*mimeType := handle.Header.Get("Content-Type")
	switch mimeType {
	case "image/jpeg":
		saveFile(w, file, handle)
	case "image/png":
		saveFile(w, file, handle)
	default:
		jsonResponse(w, http.StatusBadRequest, "The format file is not valid.")
	}*/
}

func saveFile(w http.ResponseWriter, file multipart.File, handle *multipart.FileHeader) error {
	var saveFileDir = "uploads/"

	fmt.Println("Writing file")
	fmt.Println(w, "%v", handle.Header)
	// create new file
	f, err := os.OpenFile(saveFileDir + handle.Filename, os.O_WRONLY|os.O_CREATE, 0666)
	if err != nil {
		fmt.Println(err)
		return err
	}
	defer f.Close()

	// copy uploaded file to new file
	io.Copy(f, file)

	push_to_holmes() // push file to ObjectStorage

	fmt.Println(w, "File uploaded successfully!.")
	return nil
}