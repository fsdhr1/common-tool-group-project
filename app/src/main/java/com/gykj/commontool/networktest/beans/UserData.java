package com.gykj.commontool.networktest.beans;

import java.io.Serializable;

public class UserData implements Serializable {
    private String access_token;
    private String refresh_token;
    private String token_header;
    private String token_type;

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getAccess_token() {
        return this.access_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getRefresh_token() {
        return this.refresh_token;
    }

    public void setToken_header(String token_header) {
        this.token_header = token_header;
    }

    public String getToken_header() {
        return this.token_header;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getToken_type() {
        return this.token_type;
    }

}