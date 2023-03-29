#!/bin/sh

echo "Building Holmes-Gateway"
cd Holmes-Gateway/
go get
go build .
echo "Building Holmes-Storage"
cd ../Holmes-Storage/
go get
go build .
echo "Building Holmes-Totem-Dynamic"
cd ../Holmes-Totem-Dynamic/
go get
go build .
echo "Building Holmes-Frontend"
cd ../Holmes-Frontend/
go get
go build .
echo "Building Holmes-Totem"
cd ../Holmes-Totem/
sbt assembly
echo "Build finished, next update configuration files."
