package com.dbrighthd.texturesplusmod.client.pojo; 
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Committer{
    @JsonProperty("name") 
    public String getName() { 
		 return this.name; } 
    public void setName(String name) { 
		 this.name = name; } 
    String name;
    @JsonProperty("email") 
    public String getEmail() { 
		 return this.email; } 
    public void setEmail(String email) { 
		 this.email = email; } 
    String email;
    @JsonProperty("date") 
    public Date getDate() { 
		 return this.date; } 
    public void setDate(Date date) {
		 this.date = date; } 
    Date date;
    @JsonProperty("login") 
    public String getLogin() { 
		 return this.login; } 
    public void setLogin(String login) { 
		 this.login = login; } 
    String login;
    @JsonProperty("id") 
    public int getId() { 
		 return this.id; } 
    public void setId(int id) { 
		 this.id = id; } 
    int id;
    @JsonProperty("node_id") 
    public String getNode_id() { 
		 return this.node_id; } 
    public void setNode_id(String node_id) { 
		 this.node_id = node_id; } 
    String node_id;
    @JsonProperty("avatar_url") 
    public String getAvatar_url() { 
		 return this.avatar_url; } 
    public void setAvatar_url(String avatar_url) { 
		 this.avatar_url = avatar_url; } 
    String avatar_url;
    @JsonProperty("gravatar_id") 
    public String getGravatar_id() { 
		 return this.gravatar_id; } 
    public void setGravatar_id(String gravatar_id) { 
		 this.gravatar_id = gravatar_id; } 
    String gravatar_id;
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
    @JsonProperty("followers_url") 
    public String getFollowers_url() { 
		 return this.followers_url; } 
    public void setFollowers_url(String followers_url) { 
		 this.followers_url = followers_url; } 
    String followers_url;
    @JsonProperty("following_url") 
    public String getFollowing_url() { 
		 return this.following_url; } 
    public void setFollowing_url(String following_url) { 
		 this.following_url = following_url; } 
    String following_url;
    @JsonProperty("gists_url") 
    public String getGists_url() { 
		 return this.gists_url; } 
    public void setGists_url(String gists_url) { 
		 this.gists_url = gists_url; } 
    String gists_url;
    @JsonProperty("starred_url") 
    public String getStarred_url() { 
		 return this.starred_url; } 
    public void setStarred_url(String starred_url) { 
		 this.starred_url = starred_url; } 
    String starred_url;
    @JsonProperty("subscriptions_url") 
    public String getSubscriptions_url() { 
		 return this.subscriptions_url; } 
    public void setSubscriptions_url(String subscriptions_url) { 
		 this.subscriptions_url = subscriptions_url; } 
    String subscriptions_url;
    @JsonProperty("organizations_url") 
    public String getOrganizations_url() { 
		 return this.organizations_url; } 
    public void setOrganizations_url(String organizations_url) { 
		 this.organizations_url = organizations_url; } 
    String organizations_url;
    @JsonProperty("repos_url") 
    public String getRepos_url() { 
		 return this.repos_url; } 
    public void setRepos_url(String repos_url) { 
		 this.repos_url = repos_url; } 
    String repos_url;
    @JsonProperty("events_url") 
    public String getEvents_url() { 
		 return this.events_url; } 
    public void setEvents_url(String events_url) { 
		 this.events_url = events_url; } 
    String events_url;
    @JsonProperty("received_events_url") 
    public String getReceived_events_url() { 
		 return this.received_events_url; } 
    public void setReceived_events_url(String received_events_url) { 
		 this.received_events_url = received_events_url; } 
    String received_events_url;
    @JsonProperty("type") 
    public String getType() { 
		 return this.type; } 
    public void setType(String type) { 
		 this.type = type; } 
    String type;
    @JsonProperty("site_admin") 
    public boolean getSite_admin() { 
		 return this.site_admin; } 
    public void setSite_admin(boolean site_admin) { 
		 this.site_admin = site_admin; } 
    boolean site_admin;
    @JsonProperty("user_view_type")
    public String getUser_view_type() {
        return this.user_view_type; }
    public void setUser_view_type(String user_view_type) {
        this.user_view_type = user_view_type; }
    String user_view_type;
}
