#!/bin/sh
openssl req -x509 -newkey rsa:2048 -keyout cert-key.pem -out cert.pem -days 360 -nodes
