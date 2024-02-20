package com.resautu.fsaplayer.data;
import com.google.gson.annotations.SerializedName;

import java.io.File;

public class ItemData{
    @SerializedName("Name")
    private String itemName;
    @SerializedName("Value")
    private String hashValue;
    public ItemData(String hashValue, String itemName){
        this.hashValue = hashValue;
        this.itemName = itemName;
    }
    public String getHashValue(){
        return hashValue;
    }
    public String getItemName(){
        return itemName;
    }
    @Override
    public int hashCode() {
        return hashValue.hashCode();
    }
}
