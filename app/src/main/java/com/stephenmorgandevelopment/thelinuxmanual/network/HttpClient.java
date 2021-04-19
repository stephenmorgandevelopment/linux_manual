package com.stephenmorgandevelopment.thelinuxmanual.network;

import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers.hasInternet;

public class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();

    private static Cache cache;
    private static final OkHttpClient okClient;

    static {
        if(cache == null) {
            cache = new okhttp3.Cache(
                    new File(Helpers.getCacheDir(), "http_cache"),
                    10485760);
        }

        okClient = new OkHttpClient.Builder()
                .cache(cache)
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
//                .followRedirects(true)
                .build();
    }

//    public static HttpClient getInstance() {
//        if(instance == null) {
//            instance = new HttpClient();
//        }
//        return instance;
//    }

    private HttpClient() {}

    public static OkHttpClient getClient() {
        return okClient;
    }

    public static Single<Response> fetchDirsHtml() throws IOException {
         if(hasInternet()) {
            String url = UbuntuHtmlApiConverter.BASE_URL + UbuntuHtmlApiConverter.getReleaseString() + "/" + Helpers.getLocale();
            Request req = new Request.Builder().url(url).build();

            return Single.just(okClient.newCall(req).execute());
        } else {
            return Single.error(new Throwable("No internet connection."));
        }
    }

    public static Single<Response> fetchCommandManPage(String pageUrl) {
        if(hasInternet()) {
            Request req = new Request.Builder().url(pageUrl).build();
            return Single.defer(() -> Single.just(okClient.newCall(req).execute()));
        } else {
            return Single.error(new Throwable("No internet.  Fetch Command ManPage failed."));
        }
    }

    public static Single<Response> fetchDescription(SimpleCommand command) {
        Request request = new Request.Builder().url(command.getUrl()).build();
        return Single.defer(() -> Single.just(okClient.newCall(request).execute()));
    }
//
//    public static Single<Response> fetchManPageList(Request request) throws IOException {
//        return Single.just(HttpClient.getClient().newCall(request).execute());
//    }
//
//    public static Single<Response> fetchManPageList(String url) throws IOException {
//        Request req = new Request.Builder().url(url).build();
//        return Single.just(HttpClient.getClient().newCall(req).execute());
//    }
//
//    public static Request buildRequest(String url) {
//        return new Request.Builder().url(url).build();
//    }
}
