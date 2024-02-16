package main

import (
	"fmt"
	"net/http"
	"os"
	"io"
	log "github.com/sirupsen/logrus"
)

func handleAudioRequest(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	log.Infof("Received audio request from %s", r.RemoteAddr)
	lock.RLock()
	hash := r.URL.Query().Get("hash")
	lock.RUnlock()
	if hash == "" {
		http.Error(w, "Missing hash", http.StatusBadRequest)
		return
	}

	// 根据哈希值找到对应的音频文件
	audioFilePath := audioHashes[hash]
	if audioFilePath == "" {
		http.Error(w, "Audio file not found", http.StatusNotFound)
		return
	}
	// 打开音频文件
	audioFile, err := os.Open(audioFilePath)
	if err != nil {
		log.Error("Failed to open audio file: ", err)
		http.Error(w, "Failed to open audio file", http.StatusInternalServerError)
		return
	}
	defer audioFile.Close()

	// 将音频文件内容发送给客户端
	w.Header().Set("Content-Type", "audio/mpeg")
	_, err = io.Copy(w, audioFile)
	if err != nil {
		log.Error("Failed to open audio file: ", err)
		http.Error(w, "Failed to send audio file", http.StatusInternalServerError)
		return
	}
}

func handleInformRequest(w http.ResponseWriter, r *http.Request){
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	log.Infof("Received inform request from %s", r.RemoteAddr)
	lock.RLock()
	defer lock.RUnlock()

	informFile, err := os.Open(informPath)

	if err != nil {
		log.Error("Failed to open inform file: ", err)
		http.Error(w, "Failed to open inform file", http.StatusInternalServerError)
		return
	}
	defer informFile.Close()
	w.Header().Set("Content-Type", "application/json")
	_, err = io.Copy(w, informFile)
	if err != nil {
		log.Error("Failed to open inform file: ", err)
		http.Error(w, "Failed to send inform file", http.StatusInternalServerError)
		return
	}

}

func startHttpServer(){
	http.HandleFunc("/audio", handleAudioRequest)
	http.HandleFunc("/inform", handleInformRequest)

	fmt.Printf("Server listening on port %s...", config["Port"])
	err := http.ListenAndServe(fmt.Sprintf(":%s", config["Port"]), nil)
	if err != nil {
		fmt.Printf("Failed to start server: %v", err)
	}
}