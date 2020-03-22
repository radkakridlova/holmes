package main

import (
	"flag"

	"holmes-dp/Holmes-Totem-Dynamic/check"
	"holmes-dp/Holmes-Totem-Dynamic/feed"
	"holmes-dp/Holmes-Totem-Dynamic/lib"
	"holmes-dp/Holmes-Totem-Dynamic/submit"
)

func main() {
	cPath := flag.String("config", "", "Path to the configuration file")
	flag.Parse()

	ctx := &lib.Ctx{}

	err := ctx.Init(*cPath)
	if err != nil {
		panic(err.Error())
	}

	err = feed.Run(ctx, false)
	if err != nil {
		panic(err.Error())
	}

	err = check.Run(ctx, false)
	if err != nil {
		panic(err.Error())
	}

	err = submit.Run(ctx, true)
	if err != nil {
		panic(err.Error())
	}
}
