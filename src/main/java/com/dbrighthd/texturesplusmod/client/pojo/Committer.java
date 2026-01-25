package com.dbrighthd.texturesplusmod.client.pojo;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Committer {

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("date")
    private Date date;

    @SerializedName("login")
    private String login;

    @SerializedName("id")
    private int id;

    @SerializedName("node_id")
    private String nodeId;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("gravatar_id")
    private String gravatarId;

    @SerializedName("url")
    private String url;

    @SerializedName("html_url")
    private String htmlUrl;

    @SerializedName("followers_url")
    private String followersUrl;

    @SerializedName("following_url")
    private String followingUrl;

    @SerializedName("gists_url")
    private String gistsUrl;

    @SerializedName("starred_url")
    private String starredUrl;

    @SerializedName("subscriptions_url")
    private String subscriptionsUrl;

    @SerializedName("organizations_url")
    private String organizationsUrl;

    @SerializedName("repos_url")
    private String reposUrl;

    @SerializedName("events_url")
    private String eventsUrl;

    @SerializedName("received_events_url")
    private String receivedEventsUrl;

    @SerializedName("type")
    private String type;

    @SerializedName("site_admin")
    private boolean siteAdmin;

    @SerializedName("user_view_type")
    private String userViewType;

    public String name() { return name; }
    public void name(String name) { this.name = name; }

    public String email() { return email; }
    public void email(String email) { this.email = email; }

    public Date date() { return date; }
    public void date(Date date) { this.date = date; }

    public String login() { return login; }
    public void login(String login) { this.login = login; }

    public int id() { return id; }
    public void id(int id) { this.id = id; }

    public String nodeId() { return nodeId; }
    public void nodeId(String nodeId) { this.nodeId = nodeId; }

    public String avatarUrl() { return avatarUrl; }
    public void avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String gravatarId() { return gravatarId; }
    public void gravatarId(String gravatarId) { this.gravatarId = gravatarId; }

    public String url() { return url; }
    public void url(String url) { this.url = url; }

    public String htmlUrl() { return htmlUrl; }
    public void htmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public String followersUrl() { return followersUrl; }
    public void followersUrl(String followersUrl) { this.followersUrl = followersUrl; }

    public String followingUrl() { return followingUrl; }
    public void followingUrl(String followingUrl) { this.followingUrl = followingUrl; }

    public String gistsUrl() { return gistsUrl; }
    public void gistsUrl(String gistsUrl) { this.gistsUrl = gistsUrl; }

    public String starredUrl() { return starredUrl; }
    public void starredUrl(String starredUrl) { this.starredUrl = starredUrl; }

    public String subscriptionsUrl() { return subscriptionsUrl; }
    public void subscriptionsUrl(String subscriptionsUrl) { this.subscriptionsUrl = subscriptionsUrl; }

    public String organizationsUrl() { return organizationsUrl; }
    public void organizationsUrl(String organizationsUrl) { this.organizationsUrl = organizationsUrl; }

    public String reposUrl() { return reposUrl; }
    public void reposUrl(String reposUrl) { this.reposUrl = reposUrl; }

    public String eventsUrl() { return eventsUrl; }
    public void eventsUrl(String eventsUrl) { this.eventsUrl = eventsUrl; }

    public String receivedEventsUrl() { return receivedEventsUrl; }
    public void receivedEventsUrl(String receivedEventsUrl) { this.receivedEventsUrl = receivedEventsUrl; }

    public String type() { return type; }
    public void type(String type) { this.type = type; }

    public boolean siteAdmin() { return siteAdmin; }
    public void siteAdmin(boolean siteAdmin) { this.siteAdmin = siteAdmin; }

    public String userViewType() { return userViewType; }
    public void userViewType(String userViewType) { this.userViewType = userViewType; }
}