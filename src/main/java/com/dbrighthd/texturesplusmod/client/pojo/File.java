package com.dbrighthd.texturesplusmod.client.pojo;

import com.google.gson.annotations.SerializedName;

public class File {

    @SerializedName("previous_filename")
    private String previousFilename;

    @SerializedName("sha")
    private String sha;

    @SerializedName("filename")
    private String filename;

    @SerializedName("status")
    private String status;

    @SerializedName("additions")
    private int additions;

    @SerializedName("deletions")
    private int deletions;

    @SerializedName("changes")
    private int changes;

    @SerializedName("blob_url")
    private String blobUrl;

    @SerializedName("raw_url")
    private String rawUrl;

    @SerializedName("contents_url")
    private String contentsUrl;

    @SerializedName("patch")
    private String patch;

    public String previousFilename() { return previousFilename; }
    public void previousFilename(String previousFilename) { this.previousFilename = previousFilename; }

    public String sha() { return sha; }
    public void sha(String sha) { this.sha = sha; }

    public String filename() { return filename; }
    public void filename(String filename) { this.filename = filename; }

    public String status() { return status; }
    public void status(String status) { this.status = status; }

    public int additions() { return additions; }
    public void additions(int additions) { this.additions = additions; }

    public int deletions() { return deletions; }
    public void deletions(int deletions) { this.deletions = deletions; }

    public int changes() { return changes; }
    public void changes(int changes) { this.changes = changes; }

    public String blobUrl() { return blobUrl; }
    public void blobUrl(String blobUrl) { this.blobUrl = blobUrl; }

    public String rawUrl() { return rawUrl; }
    public void rawUrl(String rawUrl) { this.rawUrl = rawUrl; }

    public String contentsUrl() { return contentsUrl; }
    public void contentsUrl(String contentsUrl) { this.contentsUrl = contentsUrl; }

    public String patch() { return patch; }
    public void patch(String patch) { this.patch = patch; }
}