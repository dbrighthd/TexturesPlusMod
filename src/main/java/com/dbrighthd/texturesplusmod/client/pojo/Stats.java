package com.dbrighthd.texturesplusmod.client.pojo; 
import com.fasterxml.jackson.annotation.JsonProperty; 
public class Stats{
    @JsonProperty("total") 
    public int getTotal() { 
		 return this.total; } 
    public void setTotal(int total) { 
		 this.total = total; } 
    int total;
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
}
