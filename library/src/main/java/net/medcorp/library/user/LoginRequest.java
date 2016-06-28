package net.medcorp.library.user;

/**
 * Created by Administrator on 2016/6/22.
 */
public class LoginRequest {

    private String token;
    private RequestUserLoginRequest params;

    public LoginRequest() {
    }

    public LoginRequest(String token, RequestUserLoginRequest params) {
        this.token = token;
        this.params = params;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public RequestUserLoginRequest getParams() {
        return params;
    }

    public void setParams(RequestUserLoginRequest params) {
        this.params = params;
    }
}
