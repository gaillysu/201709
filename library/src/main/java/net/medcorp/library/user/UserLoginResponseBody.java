package net.medcorp.library.user;

/**
 * Created by Administrator on 2016/6/22.
 */
public class UserLoginResponseBody extends BaseResponse {
    private String user;

    public UserLoginResponseBody(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
