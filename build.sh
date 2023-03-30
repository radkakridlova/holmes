#!/bin/sh


echo "Instalujem Holmes"
go install
echo "Building Holmes-Totem"
cd Holmes-Totem/
sbt assembly
echo "Instalacia skoncila, dalej upravte konfiguracie a spustite cez run.sh."
