package main

import(
	"fmt"
	"io"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

func handleAudioRequestGin(c *gin.Context) {

	cookie := c.Request.Cookies()
	if cookie == nil {
		c.JSON(http.StatusUnauthorized, "Unauthorized")
		return
	
	}
	noLogin := true
	for _, v := range cookie {
		if cookieSet[v.Name] != "" {
			noLogin = false
		}
	}
	if noLogin {
		c.JSON(http.StatusUnauthorized, "Unauthorized")
		log.Infof("Unauthorized audio request from %s", c.Request.RemoteAddr)
		return
	}

	lock.RLock()
	hash := c.Query("hash")
	lock.RUnlock()
	if hash == "" {
		c.JSON(http.StatusBadRequest, "Missing hash")
		return
	}

	// 根据哈希值找到对应的音频文件
	audioFileName := audioHashes[hash]

	if audioFileName == "" {
		c.JSON(http.StatusNotFound, "Audio file not found")
		return
	}
	audioFilePath := config["MusicSourceDir"] + "/" + audioFileName

	// 打开音频文件
	audioFile, err := os.Open(audioFilePath)
	if err != nil {
		log.Error("Failed to open audio file: ", err)
		c.JSON(http.StatusInternalServerError, "Failed to open audio file")
		return
	}
	defer audioFile.Close()

	// 将音频文件内容发送给客户端
	c.Header("Content-Type", "audio/mpeg")
	_, err = io.Copy(c.Writer, audioFile)

	if err != nil {
		log.Error("Failed to open audio file: ", err)
		c.JSON(http.StatusInternalServerError, "Failed to open audio file")
		return
	}
}

func handleInformRequestGin(c *gin.Context) {
	cookie := c.Request.Cookies()

	if cookie == nil {
		c.JSON(http.StatusUnauthorized, "Unauthorized")
		return
	
	}
	noLogin := true
	for _, v := range cookie {
		if cookieSet[v.Name] != "" {
			noLogin = false
		}
	}
	if noLogin {
		c.JSON(http.StatusUnauthorized, "Unauthorized")
		return
	}

	lock.RLock()
	defer lock.RUnlock()

	informFile, err := os.Open(informPath)

	if err != nil {
		log.Error("Failed to open inform file: ", err)
		c.JSON(http.StatusInternalServerError, "Failed to open inform file")
		return
	}
	defer informFile.Close()
	c.Header("Content-Type", "application/json")
	_, err = io.Copy(c.Writer, informFile)

	if err != nil {
		log.Error("Failed to open inform file: ", err)
		c.JSON(http.StatusInternalServerError, "Failed to open inform file")
		return
	}
}

func handleLoginRequestGin(c *gin.Context){

	cookie := c.Request.Cookies()
	

	request_key := c.Request.FormValue("key")
	rand_key := generateToken(16)
	if request_key == config["admin"] {
		for _, v := range cookie {
			if cookieSet[v.Name] == "admin"{
				c.JSON(http.StatusOK, "Already logged in")
				return
			}
		}
		cookieSet[rand_key] = "admin"
		
		c.SetCookie(rand_key, "admin", 360000, "/", "", false, true)
		c.JSON(http.StatusOK, "Admin")
		log.Infof("Admin logged in from %s", c.Request.RemoteAddr)
	} else if request_key == config["custom"] {
		for _, v := range cookie {
			if cookieSet[v.Name] == "custom"{
				c.JSON(http.StatusOK, "Already logged in")
				return
			}
		}
		cookieSet[rand_key] = "custom"

		c.SetCookie(rand_key, "custom", 360000, "/", "", false, true)
		c.JSON(http.StatusOK, "Custom")
		log.Infof("Custom logged in from %s", c.Request.RemoteAddr)
	} else {
		c.JSON(http.StatusUnauthorized, "Unauthorized")
		return
	}

}

func handleBaseRequestGin(c *gin.Context){
	c.JSON(http.StatusNotFound, "Not Found")
}

func startGinHttpServer(){

	server := gin.Default()
	server.GET("/audio", handleAudioRequestGin)
	server.GET("/inform", handleInformRequestGin)
	server.POST("/login", handleLoginRequestGin)
	server.GET("/", handleBaseRequestGin)

	cookieSet = make(map[string]string)

	fmt.Printf("Gin server listening on port %s...", config["Port"])
	err := server.Run(fmt.Sprintf(":%s", config["Port"]))
	if err != nil {
		fmt.Printf("Failed to start server: %v", err)
	}
}