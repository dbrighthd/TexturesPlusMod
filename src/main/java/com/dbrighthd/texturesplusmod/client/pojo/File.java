package com.dbrighthd.texturesplusmod.client.pojo; 
import com.fasterxml.jackson.annotation.JsonProperty; 
public class File{
    @JsonProperty("sha") 
    public String getSha() { 
		 return this.sha; } 
    public void setSha(String sha) { 
		 this.sha = sha; } 
    String sha;
    @JsonProperty("filename") 
    public String getFilename() { 
		 return this.filename; } 
    public void setFilename(String filename) { 
		 this.filename = filename; } 
    String filename;
    @JsonProperty("status") 
    public String getStatus() { 
		 return this.status; } 
    public void setStatus(String status) { 
		 this.status = status; } 
    String status;
    @JsonProperty("additions") 
    public int getAdditions() { 
		 return this.additions; } 
    public void setAdditions(int additions) { 
		 this.additions = additions; } 
    int additions;
    @JsonProperty("deletions") 
    public int getDeletions() { 
		 return this.deletions; } 
    public void setDeletions(int deletions) { 
		 this.deletions = deletions; } 
    int deletions;
    @JsonProperty("changes") 
    public int getChanges() { 
		 return this.changes; } 
    public void setChanges(int changes) { 
		 this.changes = changes; } 
    int changes;
    @JsonProperty("blob_url") 
    public String getBlob_url() { 
		 return this.blob_url; } 
    public void setBlob_url(String blob_url) { 
		 this.blob_url = blob_url; } 
    String blob_url;
    @JsonProperty("raw_url") 
    public String getRaw_url() { 
		 return this.raw_url; } 
    public void setRaw_url(String raw_url) { 
		 this.raw_url = raw_url; } 
    String raw_url;
    @JsonProperty("contents_url") 
    public String getContents_url() { 
		 return this.contents_url; } 
    public void setContents_url(String contents_url) { 
		 this.contents_url = contents_url; } 
    String contents_url;
    @JsonProperty("patch") 
    public String getPatch() { 
		 return this.patch; } 
    public void setPatch(String patch) { 
		 this.patch = patch; } 
    String patch;
}
