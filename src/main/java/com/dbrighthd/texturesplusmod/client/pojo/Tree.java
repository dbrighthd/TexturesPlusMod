package com.dbrighthd.texturesplusmod.client.pojo; 
import com.fasterxml.jackson.annotation.JsonProperty; 
public class Tree{
    @JsonProperty("sha") 
    public String getSha() { 
		 return this.sha; } 
    public void setSha(String sha) { 
		 this.sha = sha; } 
    String sha;
    @JsonProperty("url") 
    public String getUrl() { 
		 return this.url; } 
    public void setUrl(String url) { 
		 this.url = url; } 
    String url;
}
