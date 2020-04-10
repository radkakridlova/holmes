# Holmes-Frontend: A Webfrontendd for Holmes Processing

![Screenshot](http://i.imgur.com/cB0P9qq.png)

## Overview 

The Holmes-Frontend is supposed to be used to easily retrieve data from your Holmes installation and display it in a human readable way.


## WARNING: State of Development 

Holmes-Frontend is currently in an very early state. Please keep in mind that configuration options and significant code changes are expected and can occur at any time.


## Installation

You can host the frontend with any common webserver that is able to serve html files. Alternativly you can simply clone the project to your local system and open then index file in your browser.

The important part is to edit the config file which can be found under `assets/js/app/config.json.example`, after this is done you just need to remove the example prefix and the frontend is good to go.

Please keep in mind that a running Holmes-Interrogation instance is vital for the frontend to work!


## Internal structure

Please referee to the CONTRIBUTING.md file.

# Holmes-Interrogation: An Interrogation Planner for Holmes Processing [![Build Status](https://travis-ci.org/HolmesProcessing/Holmes-Interrogation.svg?branch=master)](https://travis-ci.org/HolmesProcessing/Holmes-Interrogation)

## Overview 

The Holmes-Interrogation Planner is defined by the SKALD Architecture and serves as the focal point for performing analysis and render the data derived from Holmes-Totem.


## WARNING: State of Development 

Holmes-Interrogation is currently in an very early state. Please keep in mind that configuration options and significant code changes are expected and can occur at any time.

We also current only support a Service for API interaction.


## Compilation

After cloning this project you can simple build it using `go build` or `go install`. This requires a configured and working [Go environment](https://golang.org/doc/install) and the following packages:

    go get github.com/aws/aws-sdk-go
    go get github.com/gocql/gocql


## Installation

You can deploy the binary everywhere you want, per default it is statically linked and does not need any libraries on the machine.

Next up you need to fill the `config.json.example` with your own values and rename it to `config.json`, after this simply execute the binary.


### Dependencies

All necessary dependencies can be found in the `Compilation` section.


## Usage

The binary accepts one command line parameter: `--config` to specify where it should look for the configuration file. If this is omitted it will search for the file in the current working directory.


## Internal structure

![Structure](http://i.imgur.com/aykLKjH.png)

The main process starts a few listeners to support multiple protocols. Currently only HTTP is supported, AMQP is planned and others are possible.

The listeners then wait for requests, parse them protocol specific and generate a generic request.

This generic request is then passed to a internal router that matches the request to a module and function and calls said function.

This produces a generic result which is then passed back from the router to the lister and the lister packs the result protocol specific and sends it back.

