package com.dbrighthd.texturesplusmod.client.pojo; 
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
public class LatestCommit {
    @JsonProperty("sha") 
    public String getSha() { 
		 return this.sha; } 
    public void setSha(String sha) { 
		 this.sha = sha; } 
    String sha;
    @JsonProperty("node_id") 
    public String getNode_id() { 
		 return this.node_id; } 
    public void setNode_id(String node_id) { 
		 this.node_id = node_id; } 
    String node_id;
    @JsonProperty("commit") 
    public Commit getCommit() { 
		 return this.commit; } 
    public void setCommit(Commit commit) { 
		 this.commit = commit; } 
    Commit commit;
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
    @JsonProperty("comments_url") 
    public String getComments_url() { 
		 return this.comments_url; } 
    public void setComments_url(String comments_url) { 
		 this.comments_url = comments_url; } 
    String comments_url;
    @JsonProperty("author") 
    public Author getAuthor() { 
		 return this.author; } 
    public void setAuthor(Author author) { 
		 this.author = author; } 
    Author author;
    @JsonProperty("committer") 
    public Committer getCommitter() { 
		 return this.committer; } 
    public void setCommitter(Committer committer) { 
		 this.committer = committer; } 
    Committer committer;
    @JsonProperty("parents") 
    public ArrayList<Parent> getParents() { 
		 return this.parents; } 
    public void setParents(ArrayList<Parent> parents) { 
		 this.parents = parents; } 
    ArrayList<Parent> parents;
    @JsonProperty("stats") 
    public Stats getStats() { 
		 return this.stats; } 
    public void setStats(Stats stats) { 
		 this.stats = stats; } 
    Stats stats;
    @JsonProperty("files") 
    public ArrayList<File> getFiles() { 
		 return this.files; } 
    public void setFiles(ArrayList<File> files) {
		 this.files = files; } 
    ArrayList<File> files;
}
