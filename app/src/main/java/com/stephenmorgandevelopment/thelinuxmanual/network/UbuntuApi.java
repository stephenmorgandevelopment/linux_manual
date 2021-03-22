package com.stephenmorgandevelopment.thelinuxmanual.network;

import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers.getLocal;

public class UbuntuApi implements HttpApi{
    private static UbuntuApi instance;
    private static Cache cache;
    private static OkHttpClient okClient;

    private UbuntuApi() {
        if(cache == null) {
            cache = new okhttp3.Cache(
                    new File(Helpers.getCacheDir(), "http_cache"),
                    10485760);
        }

        okClient = new OkHttpClient.Builder()
                .cache(cache)
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    public static HttpApi getInstance() {
        if(instance == null) {
            instance = new UbuntuApi();
        }
        return instance;
    }

    @Override
    public Object fetchSimpleCommands() {
//        fetchDirsHtml();
        return null;
    }

    @Override
    public Object fetchManPage(String pageUrl) {


        return null;
    }


    private Single<Response> fetchDirsHtml() throws IOException {
        String url = Ubuntu.BASE_URL + Ubuntu.getReleaseString() + "/" + getLocal();
        Request req = new Request.Builder().url(url).build();

        return Single.just(okClient.newCall(req).execute());
    }

    private Single<Response> fetchManPageHtml(String url) {
        Request req = new Request.Builder().url(url).build();
        return Single.defer(() -> Single.just(okClient.newCall(req).execute()));
    }

}
