package com.dbrighthd.texturesplusmod.client.pojo; 
import com.fasterxml.jackson.annotation.JsonProperty; 
public class Commit{
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
    @JsonProperty("message") 
    public String getMessage() { 
		 return this.message; } 
    public void setMessage(String message) { 
		 this.message = message; } 
    String message;
    @JsonProperty("tree") 
    public Tree getTree() { 
		 return this.tree; } 
    public void setTree(Tree tree) { 
		 this.tree = tree; } 
    Tree tree;
    @JsonProperty("url") 
    public String getUrl() { 
		 return this.url; } 
    public void setUrl(String url) { 
		 this.url = url; } 
    String url;
    @JsonProperty("comment_count") 
    public int getComment_count() { 
		 return this.comment_count; } 
    public void setComment_count(int comment_count) { 
		 this.comment_count = comment_count; } 
    int comment_count;
    @JsonProperty("verification") 
    public Verification getVerification() { 
		 return this.verification; } 
    public void setVerification(Verification verification) { 
		 this.verification = verification; } 
    Verification verification;
}
