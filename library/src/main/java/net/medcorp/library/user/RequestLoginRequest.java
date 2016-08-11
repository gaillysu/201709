package net.medcorp.library.user;


import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/6/22.
 */
public class RequestLoginRequest {

    private String url;
    private String token;
    private String email;
    private String password;
    private OkHttpClient okHttp;
    private UserLoginResponseBody responseBody;
    private UserLoginResponseListener listener;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public RequestLoginRequest(String url, String token, String email, String password) {
        this.url = url;
        this.token = token;
        this.email = email;
        this.password = password;
    }

    public void getUserLoginResponse(UserLoginResponseListener listener) {
        this.listener = listener;
        String responseUserProfile;
        UserLoginModel user = new UserLoginModel(email, password);
        RequestUserLoginRequest params = new RequestUserLoginRequest(user);
        LoginRequest requestBodyModel = new LoginRequest(token, params);
        okHttp = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, new Gson().toJson(requestBodyModel));
        Request request = new Request.Builder().url(url).post(requestBody).build();
        try {
            Response response = okHttp.newCall(request).execute();
            if (response.isSuccessful()) {
                Gson mgson = new Gson();
                responseBody = mgson.fromJson(response.body().string(), UserLoginResponseBody.class);
                responseUserProfile = responseBody.getUser();
                listener.requestSuccess(responseUserProfile);
            } else {
                responseUserProfile = responseBody.getMessage();
                listener.requestFail(responseUserProfile);
                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface UserLoginResponseListener {
        void requestSuccess(String json);

        void requestFail(String failMessage);
    }
}
