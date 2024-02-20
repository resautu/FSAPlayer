package main

import (
	"crypto/md5"
	"crypto/rand"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"time"

	log "github.com/sirupsen/logrus"
)

type item struct {
	Name string
	Value string
}
type infom struct {
	CalculateTime string
	ServerIp string
	Port string
	ServerDesc string
	Hashes map[string]item
}

func calculateHash(path string) string {
	file, err := os.Open(path)
	if err != nil {
		return ""
	}
	defer file.Close()
	md5 := md5.New()
	io.Copy(md5, file)
	return fmt.Sprintf("%x", md5.Sum(nil))
}

func getAudioFiles(path string) []string {
	files := []string{}
	err := filepath.Walk(path, func(path string, info os.FileInfo, err error) error {
		if !info.IsDir() && (filepath.Ext(path) == ".mp3" || filepath.Ext(path) == ".flac" || filepath.Ext(path) == ".wav"){

			files = append(files, path)
		}
		return nil
	})
	if err != nil {
		log.Warn("Music source Dictory not found. Please check the path and try again.")
		return nil
	}
	return files
}

func calculateAllFilesHash(path string) map[string]string {
	files := getAudioFiles(path)
	if files == nil {
		log.Warn("Music source Dictory not found. Please check the path and try again.")
		return nil
	}
	hashes := make(map[string]string)
	for _, file := range files {
		hashes[calculateHash(file)] = filepath.Base(file)
	}
	return hashes
}

func storeHashJson(path string) {
	hashes := audioHashes
	if hashes == nil {
		log.Warn("Calculating hashes failed. Please check the path and try again.")
	}

	export_hashmap := map[string]item{}
	for k, v := range hashes {
		export_hashmap[k] = item{v, k}
	}

	content := &infom{
		CalculateTime: time.Now().Format("2006-01-02 15:04:05"),
		ServerIp: "",
		Port: config["Port"],
		ServerDesc: config["Desc"],
		Hashes: export_hashmap,
	}
	file, err := json.MarshalIndent(content, "", " ")
	if err != nil {
		log.Warn(err)
	}
	err = os.WriteFile(informPath, file, 0644)
	if err != nil {
		log.Warn(err)
	}
}

func storeJsonFile(path string, content map[string]string) {
	file, err := json.MarshalIndent(content, "", " ")
	if err != nil {
		log.Warn(err)
	}
	err = os.WriteFile(path, file, 0644)
	if err != nil {
		log.Warn(err)
	}
}

func loadHashJson() map[string]string {
	lock.RLock()
	defer lock.RUnlock()
	file, err := os.ReadFile(informPath)
	if err != nil {
		log.Warn(err)
	}
	var content infom
	err = json.Unmarshal(file, &content)
	audioHashes = make(map[string]string)
	for k, v := range content.Hashes {
		audioHashes[k] = v.Name
	}

	if err != nil {
		log.Warn(err)
	}
	return audioHashes
}

func reCalculateHash(path string) {
	lock.Lock()
	defer lock.Unlock()
	audioHashes = calculateAllFilesHash(path)
	if audioHashes == nil {
		log.Warn("Calculating hashes failed. Please check the path and try again.")
	}
	storeHashJson(path)
}

func generateToken(keylenth int) string {
	b := make([]byte, keylenth)
	_, err := rand.Read(b)
	if err != nil {
		log.Warn("Error: ", err)
	}
	return fmt.Sprintf("%x", b)
}