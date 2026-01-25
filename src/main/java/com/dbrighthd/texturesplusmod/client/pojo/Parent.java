package com.dbrighthd.texturesplusmod.client.pojo;

import com.google.gson.annotations.SerializedName;

public class Parent {

    @SerializedName("sha")
    private String sha;

    @SerializedName("url")
    private String url;

    @SerializedName("html_url")
    private String htmlUrl;

    public String sha() { return sha; }
    public void sha(String sha) { this.sha = sha; }

    public String url() { return url; }
    public void url(String url) { this.url = url; }

    public String htmlUrl() { return htmlUrl; }
    public void htmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }
}