package com.stephenmorgandevelopment.thelinuxmanual.network;

import android.util.Log;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();

    private static HttpClient httpClient;
    private static String clientUrl;
    private static Cache cache;
    private static Retrofit retrofit;

    private HttpClient() {
        cache = new okhttp3.Cache(new File(Helpers.getCacheDir(), "http_cache"), 10485760);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cache(cache)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(clientUrl)
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }


    public static HttpClient getInstance() {
        if(httpClient == null) {
            if (clientUrl == null) {
                clientUrl = Preferences.getDefaultUrl();
            }
            httpClient = new HttpClient();
        }
        return httpClient;
    }

    public static HttpClient getInstance(String url) {
        try {
            if (!clientUrl.equals(url)) {
                httpClient = null;
                clientUrl = url;
                httpClient = new HttpClient();
            }
        } catch (NullPointerException e) {
            if(clientUrl == null) {
                clientUrl = url;
                return getInstance();
            } else {
                Log.e(TAG, Helpers.getApplicationContext().getString(R.string.unexpected_error));
                e.printStackTrace();
                return null;
            }
        }

        return httpClient;
    }


    private static void setUrl(String url) {
        clientUrl = url;
    }

//    public synchronized

}
