package http

import (
	"crypto/tls"
	"encoding/json"
	"fmt"
	router2 "holmes/Holmes-Frontend/interrogation/router"
	"log"
	"net/http"

	"holmes/Holmes-Frontend/interrogation/context"
	"holmes/Holmes-Frontend/push_to_holmes"
)

var (
	ctx *context.Ctx
)

func Start(c *context.Ctx, httpBinding, serverRoot, SSLCert, SSLKey string) {
	ctx = c

	mux := http.NewServeMux()
	fs := http.FileServer(http.Dir(serverRoot))
	mux.Handle("/", http.StripPrefix("/", fs))
	mux.HandleFunc("/api/", httpGenericRequestHandler)
	mux.HandleFunc("/upload", push_to_holmes.UploadFile)

	if SSLCert != "" && SSLKey != "" {

		cfg := &tls.Config{
			MinVersion:               tls.VersionTLS12,
			CurvePreferences:         []tls.CurveID{tls.CurveP521, tls.CurveP384, tls.CurveP256},
			PreferServerCipherSuites: true,
			CipherSuites: []uint16{
				tls.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
				tls.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
				tls.TLS_RSA_WITH_AES_256_GCM_SHA384,
				tls.TLS_RSA_WITH_AES_256_CBC_SHA,
			},
		}

		srv := &http.Server{
			Addr:         httpBinding,
			Handler:      mux,
			TLSConfig:    cfg,
			TLSNextProto: make(map[string]func(*http.Server, *tls.Conn, http.Handler), 0),
		}

		log.Fatal(srv.ListenAndServeTLS(SSLCert, SSLKey))
	} else {
		srv := &http.Server{
			Addr:         httpBinding,
			Handler:      mux,
		}

		log.Fatal(srv.ListenAndServe())
	}
}

func httpGenericRequestHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Add("Strict-Transport-Security", "max-age=63072000; includeSubDomains")
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Headers", "origin, content-type, accept")
	w.Header().Set("Content-Type", "application/json")

	if r.Method == "OPTIONS" {
		return
	}

	fmt.Println("Handling interrogation request")

	decoder := json.NewDecoder(r.Body)
	var cReq context.Request
	err := decoder.Decode(&cReq)
	if err != nil {
		err500(w, r, err)
		return
	}

	ctx.Debug.Printf("Request: %+v\n", cReq)

	j, err := json.Marshal(router2.Route(ctx, &cReq))
	if err != nil {
		err500(w, r, err)
		return
	}

	w.Write(j)
}

func err500(w http.ResponseWriter, r *http.Request, err interface{}) {
	ctx.Warning.Println(err)
	http.Error(w, fmt.Sprintf("Server error occured! - %v", err), 500)
}
