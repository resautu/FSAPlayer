package main

import (
	"encoding/json"
	"os"
	"sync"
	"time"
	"strconv"
	log "github.com/sirupsen/logrus"
)

var(
	config map[string]string
	lock sync.RWMutex
	hashcalculateInterval uint64
	audioHashes map[string]string
	informPath string
)

func main() {
	file, err := os.ReadFile("config.json")
	log.SetLevel(log.InfoLevel)
	informPath = "inform.json"
	if err != nil {
		log.Panicln("Config file not found. Please check the path and try again.")
	}
	err = json.Unmarshal(file, &config)
	if err != nil {
		log.Panicln("Config file is not valid. Please check the file and try again.")
	}
	
	reCalculateHash(config["MusicSourceDIr"])

	startHttpServer()
	hashcalculateInterval, err = strconv.ParseUint(config["HashCalculateInterval"], 10, 64)
	if err != nil && config["AutoCalculateHash"] == "true" {
		log.Panicln("hashcalculateInterval must be a interger.")
	}

	ticker := time.NewTicker(time.Duration(hashcalculateInterval) * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ticker.C:
			if config["AutoUpdate"] == "true" {
				go reCalculateHash(config["MusicSourceDIr"])
			}
		}
	}

}
