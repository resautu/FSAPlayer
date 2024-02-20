package com.resautu.fsaplayer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class FileUtil {

    public static boolean fileWriter(String path, String content){
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeBytesToFile(byte[] data, File file){
        try {

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] fileReader(String path){
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        try {
            byte[] data = new byte[(int) file.length()];
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
