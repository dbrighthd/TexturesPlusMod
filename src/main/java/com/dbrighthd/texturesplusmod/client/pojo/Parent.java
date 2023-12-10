package com.dbrighthd.texturesplusmod.client.pojo; 
import com.fasterxml.jackson.annotation.JsonProperty; 
public class Parent{
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
    @JsonProperty("html_url") 
    public String getHtml_url() { 
		 return this.html_url; } 
    public void setHtml_url(String html_url) { 
		 this.html_url = html_url; } 
    String html_url;
}
