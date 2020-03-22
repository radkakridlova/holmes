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
echo "Building Holmes-Interrogation"
cd ../Holmes-Interrogation/
go get
go build .
echo "Building Holmes-Toolbox just in case..."
cd ../Holmes-Toolbox/
go get
echo "Building Holmes-Totem"
cd ../Holmes-Totem/
sbt assembly
echo "Build finished. Update configuration files next, and generate SSL cerftificate for Holmes-Gateway."
