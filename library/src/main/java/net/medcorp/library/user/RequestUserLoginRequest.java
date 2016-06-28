package net.medcorp.library.user;

/**
 * Created by Administrator on 2016/6/22.
 */
public class RequestUserLoginRequest {

    private UserLoginModel user;

    public RequestUserLoginRequest(UserLoginModel user) {
        this.user = user;
    }

    public UserLoginModel getUser() {
        return user;
    }

    public void setUser(UserLoginModel user) {
        this.user = user;
    }
}
