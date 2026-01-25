package com.dbrighthd.texturesplusmod.client.pojo;

import com.google.gson.annotations.SerializedName;

public class Commit {

    @SerializedName("author")
    private Author author;

    @SerializedName("committer")
    private Committer committer;

    @SerializedName("message")
    private String message;

    @SerializedName("tree")
    private Tree tree;

    @SerializedName("url")
    private String url;

    @SerializedName("comment_count")
    private int commentCount;

    @SerializedName("verification")
    private Verification verification;

    public Author author() { return author; }
    public void author(Author author) { this.author = author; }

    public Committer committer() { return committer; }
    public void committer(Committer committer) { this.committer = committer; }

    public String message() { return message; }
    public void message(String message) { this.message = message; }

    public Tree tree() { return tree; }
    public void tree(Tree tree) { this.tree = tree; }

    public String url() { return url; }
    public void url(String url) { this.url = url; }

    public int commentCount() { return commentCount; }
    public void commentCount(int commentCount) { this.commentCount = commentCount; }

    public Verification verification() { return verification; }
    public void verification(Verification verification) { this.verification = verification; }
}