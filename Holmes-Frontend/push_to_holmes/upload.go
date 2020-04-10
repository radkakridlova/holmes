package push_to_holmes

import (
	"fmt"
	"io"
	"net/http"
	"os"
)

func UploadFile(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Redirect(w, r, "/", http.StatusSeeOther) // vrati spat na root stranku ak request nieje POST
		return
	}

	//get the multipart reader for the request.
	reader, err := r.MultipartReader()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	//copy each part to destination.
	var saveFileDir = "push_to_holmes/uploads/"
	for {
		part, err := reader.NextPart()
		if err == io.EOF {
			break
		} else if err != nil {
		 	http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		//if part.FileName() is empty, skip this iteration.
		if part.FileName() == "" {
			continue
		}

		fmt.Println("Writing file " + part.FileName())

		// create new file
		f, err := os.OpenFile(saveFileDir + part.FileName(), os.O_WRONLY|os.O_CREATE, 0666)
		if err != nil {
			fmt.Println("error writing file")
			fmt.Println(err)
			return
		}

		// copy uploaded file to new file
		if _, err := io.Copy(f, part); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		err = f.Close()
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	fmt.Println("Files uploaded successfully!. Pushing to holmes.")

	status := PushToHolmes() // push file to ObjectStorage
	outString := ""
	if status != 200 {
		outString = fmt.Sprintf("{\"response\": \"push failed.\", \"success\": false, \"error\": \"Pushing to Holmes failed with status %d\"}", status)
	} else {
		outString = "{\"response\": \"files pushed, all good.\", \"success\": true}"
	}

	_, err = io.WriteString(w, outString)
	if err != nil {
		fmt.Println(err)
	}

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
