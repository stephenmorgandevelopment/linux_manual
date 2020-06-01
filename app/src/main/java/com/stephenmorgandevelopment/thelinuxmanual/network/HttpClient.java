package com.stephenmorgandevelopment.thelinuxmanual.network;

import android.util.Log;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();

    private static HttpClient httpClient;
    private static HttpClientService httpClientService;
    private static String clientUrl;
    private static Cache cache;
    private static OkHttpClient okClient;
    //private static Retrofit retrofit;

    private HttpClient() {


        cache = new okhttp3.Cache(new File(Helpers.getCacheDir(), "http_cache"), 10485760);

        okClient = new OkHttpClient.Builder()
                .cache(cache)
//                .callTimeout(45, TimeUnit.SECONDS)
//                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
//                .writeTimeout(45, TimeUnit.SECONDS)
                .build();

//        retrofit = new Retrofit.Builder()
//                .baseUrl(clientUrl)
//                .client(httpClient)
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .build();
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
        if(httpClient == null) {
            clientUrl = url;
            httpClient = new HttpClient();
        }

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

    public static OkHttpClient getClient() {
        if(okClient == null) {
            getInstance();
        }

        return okClient;
    }


    public static Single<Response> fetchDirsHtml() throws IOException {
        if(okClient == null) {
            getInstance();
        }

        if(Helpers.hasInternet()) {
            //OkHttpClient client = HttpClient.getInstance().getClient();
            String url = Ubuntu.BASE_URL + Ubuntu.getReleaseString() + "/" + Helpers.getLocal();  // + "/";
            Request req = new Request.Builder().url(url).build();

            return Single.just(okClient.newCall(req).execute());

        } else {
            return Single.error(new Throwable("No internet connection."));
        }
    }

    public static Single<Call> fetchCommandManPage(String pageUrl) {
        if(okClient == null) {
            getInstance();
        }

        if(Helpers.hasInternet()) {
            Request req = new Request.Builder().url(pageUrl).build();
            return Single.just(okClient.newCall(req));
        } else {
            return Single.error(new Throwable("No internet.  FetchCommmandManPage failed."));
        }
    }


//    public HttpClientService getService() {
//        if(httpClientService == null) {
//            httpClientService = retrofit.create(HttpClientService.class);
//        }
//        return httpClientService;
//    }
//    public synchronized

}
