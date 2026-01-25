package com.dbrighthd.texturesplusmod.client.pojo;

import com.google.gson.annotations.SerializedName;

public class Tree {

    @SerializedName("sha")
    private String sha;

    @SerializedName("url")
    private String url;

    public String sha() { return sha; }
    public void sha(String sha) { this.sha = sha; }

    public String url() { return url; }
    public void url(String url) { this.url = url; }
}