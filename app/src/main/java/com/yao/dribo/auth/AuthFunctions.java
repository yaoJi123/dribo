package com.yao.dribo.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yao.dribo.Utils.ModelUtils;
import com.yao.dribo.model.Bucket;
import com.yao.dribo.model.Like;
import com.yao.dribo.model.Shot;
import com.yao.dribo.model.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * functions of request through APIs
 * */

public class AuthFunctions {


    public static final int COUNT_PER_PAGE = 12;

    private static final String TAG = "Dribbble API";

    private static final String API_URL = "https://api.dribbble.com/v1/";

    private static final String USER_END_POINT = API_URL + "user";

    private static final String SP_AUTH = "auth";

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER = "user";

    private static final TypeToken<User> USER_TYPE = new TypeToken<User>(){};
    private static final String SHOTS_END_POINT = API_URL + "shots";
    private static final TypeToken<List<Shot>> SHOT_LIST_TYPE = new TypeToken<List<Shot>>(){};
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String BUCKET_END_POINT = API_URL + "buckets";
    private static final TypeToken<Bucket> BUCKET_TYPE = new TypeToken<Bucket>(){};
    private static final String KEY_SHOT_ID = "shot_id";
    private static final TypeToken<List<Like>> LIKE_LIST_TYPE = new TypeToken<List<Like>>(){};
    private static final TypeToken<Like> LIKE_TYPE = new TypeToken<Like>(){};

    private static OkHttpClient client = new OkHttpClient();

    private static String accessToken;
    private static User user;
    private static TypeToken<List<Bucket>> BucketListType = new TypeToken<List<Bucket>>(){};

    private static Request.Builder authRequestBuilder(String url) {
        return new Request.Builder().addHeader("Authorization", "Bearer " + accessToken).url(url);
    }

    private static Response makeRequest(Request request) throws  AuthException {
        try {
            Response response = client.newCall(request).execute();
            Log.d(TAG, response.header("X-RateLimit-Remaining"));
            return response;
        } catch (IOException e) {
            throw new AuthException(e.getMessage());
        }

    }

    private static Response makeGetRequest(String url) throws  AuthException {
        Request request = authRequestBuilder(url).build();
        return makeRequest(request);
    }

    private static Response makePostRequest(String url, FormBody formBody) throws   AuthException{
        Request request = authRequestBuilder(url)
                .post(formBody)
                .build();
        return makeRequest(request);
    }

    private static Response makePutRequest(String url, FormBody formBody) throws  AuthException {
        Request request = authRequestBuilder(url)
                .put(formBody)
                .build();
        return makeRequest(request);
    }

    private static Response makeDeleteRequest(String url) throws AuthException {
        Request request = authRequestBuilder(url)
                .delete()
                .build();
        return makeRequest(request);
    }

    private static Response makeDeleteRequest(String url, FormBody formBody) throws  AuthException{
        Request request = authRequestBuilder(url)
                .delete(formBody)
                .build();
        return makeRequest(request);
    }
    private static <T> T parseResponse(Response response,
                                       TypeToken<T> typeToken) throws  AuthException {
        String responseString;
        try {
            responseString = response.body().string();
        } catch (IOException e) {
            throw new AuthException(e.getMessage());
        }
        Log.d(TAG, responseString);
        try {
            return ModelUtils.toObject(responseString, typeToken);
        } catch (JsonSyntaxException e) {
            throw new AuthException(responseString);
        }
    }

    public static void init(@NonNull Context context) {
        accessToken = loadAccessToken(context);
        if (accessToken != null) {
            user = loadUser(context);
        }
    }

    public static boolean isLoggedIn() {
        return accessToken != null;
    }

    public static void login(@NonNull Context context,
                             @NonNull String accessToken) throws AuthException {
        AuthFunctions.accessToken = accessToken;
        storeAccessToken(context, accessToken);

        AuthFunctions.user = getUser();
        storeUser(context, user);
    }

    public static void logout(@NonNull Context context) {
        storeAccessToken(context, null);
        storeUser(context, null);

        accessToken = null;
        user = null;
    }

    public static User getUser() throws AuthException{
        return parseResponse(makeGetRequest(USER_END_POINT), USER_TYPE);
    }

    public static User getCurrentUser() {
        return user;
    }

    public static void storeAccessToken(@NonNull Context context, @Nullable String token) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SP_AUTH, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public static String loadAccessToken(@NonNull Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SP_AUTH, Context.MODE_PRIVATE);
        return sp.getString(KEY_ACCESS_TOKEN, null);
    }

    public static void storeUser(@NonNull Context context, @Nullable User user) {
        ModelUtils.save(context, KEY_USER, user);
    }

    public static User loadUser(@NonNull Context context) {
        return ModelUtils.read(context, KEY_USER, new TypeToken<User>(){});
    }

    public static List<Shot> getShot(int page) throws AuthException{
        String url = SHOTS_END_POINT + "?page=" + page;
        return parseResponse(makeGetRequest(url), SHOT_LIST_TYPE);
    }

    public static List<Bucket> getUserBuckets(@NonNull String userId, int page) throws AuthException {
        String url = USER_END_POINT + "/" + userId + "/buckets?page=" + page;
        return parseResponse(makeGetRequest(url),  BucketListType);
    }

    public static List<Bucket> getUserBuckets() throws AuthException {
        String url = USER_END_POINT + "/" + "buckets?per_page=" + Integer.MAX_VALUE;
        return parseResponse(makeGetRequest(url), BucketListType);
    }
    public static List<Bucket> getUserBuckets(int page) throws AuthException{
        String url = USER_END_POINT + "/" + "buckets?page=" + page;
        return parseResponse(makeGetRequest(url), BucketListType);
    }

    public static Bucket newBucket(String name, String description) throws AuthException{
        FormBody formBody = new FormBody.Builder()
                .add(KEY_NAME, name)
                .add(KEY_DESCRIPTION, description)
                .build();
        return parseResponse(makePostRequest(BUCKET_END_POINT, formBody), BUCKET_TYPE);
    }

    public static List<Bucket> getShotBuckets(String id) throws AuthException {
        String url = SHOTS_END_POINT + "/" + id + "/buckets?per_page=" + Integer.MAX_VALUE;
        return parseResponse(makeGetRequest(url), BucketListType);
    }

    public static void addBucketShot(String addedId, String id) throws AuthException {
        String url = BUCKET_END_POINT + "/" + addedId + "/shots";
        FormBody formBody = new FormBody.Builder()
                .add(KEY_SHOT_ID, id)
                .build();

        Response response = makePutRequest(url, formBody);
        checkStatusCode(response, HttpURLConnection.HTTP_NO_CONTENT);
    }



    public static void removeBucketShot(String removedId, String id) throws AuthException{
        String url = BUCKET_END_POINT + "/" + removedId + "/shots";
        FormBody formBody = new FormBody.Builder()
                .add(KEY_SHOT_ID, id)
                .build();

        Response response = makeDeleteRequest(url, formBody);
        checkStatusCode(response, HttpURLConnection.HTTP_NO_CONTENT);
    }

    private static void checkStatusCode(Response response, int httpNoContent) throws AuthException {
        if (response.code() != httpNoContent) {
            throw new AuthException(response.message());
        }
    }


    public static List<Shot> getLikedShots(int page) throws AuthException {
        List<Like> likes = getLikes(page);
        List<Shot> likedShotes = new ArrayList<>();
        for (Like like : likes) {
            likedShotes.add(like.shot);
        }
        return likedShotes;
    }

    private static List<Like> getLikes(int page) throws AuthException {
        String url = USER_END_POINT + "/likes?page=" + page;
        return parseResponse(makeGetRequest(url), LIKE_LIST_TYPE);
    }


    public static List<Shot> getBucketShots(String bucketId, int page) throws AuthException {
        String url = BUCKET_END_POINT + "/" + bucketId + "/shots";
        return parseResponse(makeGetRequest(url), SHOT_LIST_TYPE);
    }

    public static Like likeShot(String id) throws AuthException {
        String url = SHOTS_END_POINT + "/" + id + "/like";
        Response response = makePostRequest(url, new FormBody.Builder().build());

        checkStatusCode(response, HttpURLConnection.HTTP_CREATED);

        return parseResponse(response, LIKE_TYPE);
    }

    public static void unlikeShot(@NonNull String id) throws AuthException {
        String url = SHOTS_END_POINT + "/" + id + "/like";
        Response response = makeDeleteRequest(url);
        checkStatusCode(response, HttpURLConnection.HTTP_NO_CONTENT);
    }

    public static Boolean isLikingShot(String id) throws AuthException {
        String url = SHOTS_END_POINT + "/" + id + "/like";
        Response response = makeGetRequest(url);
        switch (response.code()) {
            case HttpURLConnection.HTTP_OK:
                return true;
            case HttpURLConnection.HTTP_NOT_FOUND:
                return false;
            default:
                throw new AuthException(response.message());
        }
    }
}
