package com.dbrighthd.texturesplusmod.client.pojo;

import com.google.gson.annotations.SerializedName;

public class Stats {

    @SerializedName("total")
    private int total;

    @SerializedName("additions")
    private int additions;

    @SerializedName("deletions")
    private int deletions;

    public int total() { return total; }
    public void total(int total) { this.total = total; }

    public int additions() { return additions; }
    public void additions(int additions) { this.additions = additions; }

    public int deletions() { return deletions; }
    public void deletions(int deletions) { this.deletions = deletions; }
}