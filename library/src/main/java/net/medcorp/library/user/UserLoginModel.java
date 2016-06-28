package net.medcorp.library.user;

/**
 * Created by Administrator on 2016/6/22.
 */
public class UserLoginModel {
    private String email;
    private String password;

    public UserLoginModel() {
    }

    public UserLoginModel(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
