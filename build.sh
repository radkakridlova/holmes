#!/bin/sh


echo "Instalujem Holmes"
go install

echo "Building Holmes-Gateway"
cd Holmes-Gateway/
go build .

echo "Building Holmes-Storage"
cd ../Holmes-Storage/
go build .

echo "Building Holmes-Totem-Dynamic"
cd ../Holmes-Totem-Dynamic/
go build .

echo "Building Holmes-Frontend"
cd ../Holmes-Frontend/
go build .

echo "Adresar na uploady Holmes-Frontend/push_to_holmes/uploads"
mkdir ../Holmes-Frontend/push_to_holmes/uploads

echo "Building Holmes-Totem"
cd ../Holmes-Totem/
sbt assembly

echo "Instalacia skoncila, run.sh spustite sluzby."
