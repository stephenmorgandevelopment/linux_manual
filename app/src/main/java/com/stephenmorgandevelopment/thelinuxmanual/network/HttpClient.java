package com.stephenmorgandevelopment.thelinuxmanual.network;

import static com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers.hasInternet;

import androidx.annotation.NonNull;

import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.UbuntuHtmlApiConverter;
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class HttpClient {
    @NonNull
    private final Preferences mPreferences;
    private static final OkHttpClient okClient;

    static {
        Cache cache = new okhttp3.Cache(
                new File(Helpers.getCacheDir(), "http_cache"),
                10_485_760);


        okClient = new OkHttpClient.Builder()
                .cache(cache)
                .readTimeout(18, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .followSslRedirects(false)
                .build();
    }

    @Inject
    public HttpClient(
            @NonNull Preferences preferences
    ) {
        mPreferences = preferences;
    }

    public OkHttpClient getClient() {
        return okClient;
    }

    public Single<Response> fetchDirsHtml() throws IOException {
        if (hasInternet()) {
            String url = UbuntuHtmlApiConverter.BASE_URL + mPreferences.getCurrentRelease() /*UbuntuHtmlApiConverter.getReleaseString()*/
                    + "/" + Helpers.getLocale();

            Request req = new Request.Builder().url(url).build();

            return Single.just(okClient.newCall(req).execute());
        } else {
            return Single.error(new Throwable("No internet connection."));
        }
    }

    public Single<Response> fetchCommandManPage(String pageUrl) {
        if (hasInternet()) {
            Request req = new Request.Builder().url(pageUrl).build();
            return Single.defer(() -> Single.just(okClient.newCall(req).execute()));
        } else {
            return Single.error(new Throwable("No internet.  Fetch Command ManPage failed."));
        }
    }

    public Single<Response> fetchDescription(@NonNull MatchingItem command) {
        Request request = new Request.Builder().url(command.getUrl()).build();
        return Single.defer(() -> Single.just(okClient.newCall(request).execute()));
    }
}
