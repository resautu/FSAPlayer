package main

import (
	"fmt"
	"io"
	"net/http"
	"os"

	log "github.com/sirupsen/logrus"
)

var cookieSet map[string]string

func makeCookie(content map[string]string) http.Cookie{
	cookie := http.Cookie{
		Name: content["name"],
		Value: content["value"],
	}
	return cookie
}

func handleAudioRequest(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	_token, err := r.Cookie("token")
	if err != nil || cookieSet[_token.Name] == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
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
	_token, err := r.Cookie("token")
	if err != nil || cookieSet[_token.Name] == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
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

func handleLoginRequest(w http.ResponseWriter, r *http.Request){
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	log.Infof("Received login request from %s", r.RemoteAddr)

	_token, err := r.Cookie("token")
	if err == nil && cookieSet[_token.Name] != "" {
		w.WriteHeader(http.StatusOK)
		return
	}
	

	request_key := r.FormValue("key")
	rand_key := generateToken(16)
	if request_key == config["admin"] {
		cookie_tmp := &http.Cookie{
			Name: rand_key,
			Value: "admin",
		}
		cookieSet[rand_key] = "admin"
		http.SetCookie(w, cookie_tmp)
		w.WriteHeader(http.StatusOK)
		log.Infof("Admin logged in from %s", r.RemoteAddr)
	} else if request_key == config["custom"] {
		cookie_tmp := &http.Cookie{
			Name: rand_key,
			Value: "custom",
		}
		cookieSet[rand_key] = "custom"
		http.SetCookie(w, cookie_tmp)
		w.WriteHeader(http.StatusOK)
		log.Infof("Custom logged in from %s", r.RemoteAddr)
	} else {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		log.Infof("Unauthorized login from %s", r.RemoteAddr)
		return
	}

}

func startHttpServer(){
	http.HandleFunc("/audio", handleAudioRequest)
	http.HandleFunc("/inform", handleInformRequest)
	http.HandleFunc("/login", handleLoginRequest)
	
	cookieSet = make(map[string]string)

	fmt.Printf("Server listening on port %s...", config["Port"])
	err := http.ListenAndServe(fmt.Sprintf(":%s", config["Port"]), nil)
	if err != nil {
		fmt.Printf("Failed to start server: %v", err)
	}
}