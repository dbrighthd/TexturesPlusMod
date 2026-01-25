package com.dbrighthd.texturesplusmod.client.pojo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LatestCommit {

    @SerializedName("sha")
    private String sha;

    @SerializedName("node_id")
    private String nodeId;

    @SerializedName("commit")
    private Commit commit;

    @SerializedName("url")
    private String url;

    @SerializedName("html_url")
    private String htmlUrl;

    @SerializedName("comments_url")
    private String commentsUrl;

    @SerializedName("author")
    private Author author;

    @SerializedName("committer")
    private Committer committer;

    @SerializedName("parents")
    private List<Parent> parents;

    @SerializedName("stats")
    private Stats stats;

    @SerializedName("files")
    private List<File> files;

    public String sha() { return sha; }
    public void sha(String sha) { this.sha = sha; }

    public String nodeId() { return nodeId; }
    public void nodeId(String nodeId) { this.nodeId = nodeId; }

    public Commit commit() { return commit; }
    public void commit(Commit commit) { this.commit = commit; }

    public String url() { return url; }
    public void url(String url) { this.url = url; }

    public String htmlUrl() { return htmlUrl; }
    public void htmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public String commentsUrl() { return commentsUrl; }
    public void commentsUrl(String commentsUrl) { this.commentsUrl = commentsUrl; }

    public Author author() { return author; }
    public void author(Author author) { this.author = author; }

    public Committer committer() { return committer; }
    public void committer(Committer committer) { this.committer = committer; }

    public List<Parent> parents() { return parents; }
    public void parents(List<Parent> parents) { this.parents = parents; }

    public Stats stats() { return stats; }
    public void stats(Stats stats) { this.stats = stats; }

    public List<File> files() { return files; }
    public void files(List<File> files) { this.files = files; }
}