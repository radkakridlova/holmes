#!/bin/sh

echo "Installing project"
go install
sbt assembly
echo "Install finished, next update configuration files."
