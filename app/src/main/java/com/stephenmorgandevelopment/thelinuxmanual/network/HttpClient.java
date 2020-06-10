package com.stephenmorgandevelopment.thelinuxmanual.network;

import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();

    private static HttpClient httpClient;
    private static String clientUrl;
    private static Cache cache;
    private static OkHttpClient okClient;

    private HttpClient() {
        if(cache == null) {
            cache = new okhttp3.Cache(new File(Helpers.getCacheDir(), "http_cache"), 10485760);
        }

        okClient = new OkHttpClient.Builder()
                .cache(cache)
                .readTimeout(20, TimeUnit.SECONDS)
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
            String url = Ubuntu.BASE_URL + Ubuntu.getReleaseString() + "/" + Helpers.getLocal();
            Request req = new Request.Builder().url(url).build();

            return Single.just(okClient.newCall(req).execute());

        } else {
            return Single.error(new Throwable("No internet connection."));
        }
    }

    public static Single<Response> fetchCommandManPage(String pageUrl) {
        if(okClient == null) {
            getInstance();
        }

        if(Helpers.hasInternet()) {
            Request req = new Request.Builder().url(pageUrl).build();
            return Single.defer(() -> Single.just(okClient.newCall(req).execute()));
        } else {
            return Single.error(new Throwable("No internet.  Fetch Command ManPage failed."));
        }
    }
}
