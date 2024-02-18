package com.resautu.fsaplayer.utils;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;

import okhttp3.*;
public class HTTPClient {
    private static HTTPClient instance;
    private String _serverAddress;
    private String httpServerAddress;
    private OkHttpClient client;
    public synchronized static HTTPClient getInstance() {
        if (instance == null) {
            instance = new HTTPClient();
        }
        return instance;
    }
    public void init(String serverAddress) {
        _serverAddress = serverAddress;
        httpServerAddress = "http://" + _serverAddress;
        client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
            @Override
            public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                if(cookieStore.containsKey(url.host())){
                    cookieStore.get(url.host()).addAll(cookies);
                }else{
                    cookieStore.put(url.host(), cookies);
                }
            }
            @Override
            public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : Collections.emptyList();
            }
        }).build();
    }
    public String sendSyncGetRequest(){
        Request request = new Request.Builder()
                .url(httpServerAddress)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception e) {
            return "网络请求错误，请检查网络连接或网络地址是否正确！";
        }
    }
    public void sendAsyncGetRequest(){
        Request request = new Request.Builder()
                .url(httpServerAddress)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                return;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

            }
        });
    }
    public Response sendSyncPostRequest(Map<String, String> data, String path) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), data.toString());
        Request request = new Request.Builder()
                .url(httpServerAddress + path)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response;
        } catch (Exception e) {
            return null;
        }
    }
}
