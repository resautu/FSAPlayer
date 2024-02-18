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

func autoCalculateHash(){
	hashcalculateInterval, err := strconv.ParseUint(config["HashCalculateInterval"], 10, 64)
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

func configKey(){
	
	keyFilePath := config["KeyFilePath"]
	if keyFilePath == "" {
		keyFilePath = "./key.json"
	}
	_, err := os.Stat(keyFilePath)
	if err != nil {
		keys := make(map[string]string)
		keys["admin"] = generateToken(16)
		keys["custom"] = generateToken(16)
		storeJsonFile(keyFilePath, keys)
		config["admin"] = keys["admin"]
		config["custom"] = keys["custom"]
	} else {
		file, err := os.ReadFile(keyFilePath)
		if err != nil {
			log.Panicln("Key file not found. Please check the path and try again.")
		}
		err = json.Unmarshal(file, &config)
		if err != nil {
			log.Panicln("Key file is not valid. Please check the file and try again.")
		}
	}
	
}


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
	fileInfo, err := os.Stat(config["MusicSourceDIr"])
	if err != nil || !fileInfo.IsDir() {
		log.Errorln("Music source Dictory found.")
		config["MusicSourceDIr"] = "."
	}
	reCalculateHash(config["MusicSourceDIr"])
	configKey()
	startHttpServer()

	if config["AutoCalculateHash"] == "true" {
		autoCalculateHash()
	}
	

}
