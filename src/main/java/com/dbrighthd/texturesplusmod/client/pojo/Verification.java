package com.dbrighthd.texturesplusmod.client.pojo;

import com.google.gson.annotations.SerializedName;

public class Verification {

    @SerializedName("verified")
    private boolean verified;

    @SerializedName("reason")
    private String reason;

    @SerializedName("signature")
    private Object signature;

    @SerializedName("payload")
    private Object payload;

    @SerializedName("verified_at")
    private String verifiedAt;

    public boolean verified() { return verified; }
    public void verified(boolean verified) { this.verified = verified; }

    public String reason() { return reason; }
    public void reason(String reason) { this.reason = reason; }

    public Object signature() { return signature; }
    public void signature(Object signature) { this.signature = signature; }

    public Object payload() { return payload; }
    public void payload(Object payload) { this.payload = payload; }

    public String verifiedAt() { return verifiedAt; }
    public void verifiedAt(String verifiedAt) { this.verifiedAt = verifiedAt; }
}