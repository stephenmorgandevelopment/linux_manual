package com.stephenmorgandevelopment.thelinuxmanual;

import static com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter.crawlForManPages;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter;
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItemKt;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
import okhttp3.Response;

@AndroidEntryPoint
@Singleton
public class CommandSyncService extends JobIntentService {
    @Inject
    HttpClient mClientHttp;
    @Inject
    Preferences mPreferences;
    @Inject
    SimpleCommandsDatabase mRoomDatabase;

    @Inject
    public CommandSyncService() {
    }

    public static final String DISTRO = "distro";
    public static final String COMPLETE_TAG = "complete";
    public static final int JOB_ID = 5001;
    public static final String TAG = "CommandSyncService";

    private static MutableLiveData<String> progress;

    private static CompositeDisposable globalDisposable;

    private static volatile boolean working = false;

    public static LiveData<String> enqueueWork(Context context, Intent work) {
        if (progress == null) {
            progress = new MutableLiveData<>();
        }
        progress.setValue("Running initial sync to build local command database.");

        working = true;
        enqueueWork(context, CommandSyncService.class, JOB_ID, work);

        return progress;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (globalDisposable == null) {
            globalDisposable = new CompositeDisposable();
        }

        updateProgress("\n\nConnecting to " + UbuntuHtmlApiConverter.BASE_URL + ".");

        try {
            syncSimpleCommands();
        } catch (IOException ioe) {
            Log.e(TAG, "Error in syncSimpleCommands: " + ioe.getMessage());
        }
    }

    private synchronized void syncSimpleCommands() throws IOException {
        Disposable disposable = mClientHttp.fetchDirsHtml()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMapObservable(this::mapHtmlToManDirs)
                .concatMap(request -> Observable.just(mClientHttp.getClient().newCall(request).execute()))
                .doOnComplete(CommandSyncService.this::stopWork)
                .subscribe(this::processAndSaveResponse, error -> {
                    Log.d(TAG, String.valueOf(error.getMessage()));
                    Log.d(TAG, error.toString());
                });

        globalDisposable.add(disposable);
    }

    private void processAndSaveResponse(Response response) throws IOException {
        String reqUrl = response.request().url().toString();

        if (reqUrl.endsWith("3/")) {
            updateProgress("\nPulled data from " + reqUrl
                    + "\nLarge data set, longest processing."
                    + "\nProcessing data...");
        } else {
            updateProgress("\nPulled data from " + reqUrl
                    + "\nProcessing data...");
        }

        List<SimpleCommand> pageCommands = crawlForManPages(response.body().string(), reqUrl);
        updateProgress("\nSaving data locally...");

        if (pageCommands.size() > 0) {
            mRoomDatabase.dao().insertAll(MatchingItemKt.toMatchingItems(pageCommands));
        }
    }

    public Observable<Request> mapHtmlToManDirs(Response response) throws IOException {
        if (response.isSuccessful() && response.code() == 200) {
            String url = UbuntuHtmlApiConverter.BASE_URL + mPreferences.getCurrentRelease()
                    + "/" + Helpers.getLocale() + "/";

            List<String> dirPaths = UbuntuHtmlApiConverter.crawlForManDirs(response.body().string());
            List<Request> requests = new ArrayList<>();

            for (String path : dirPaths) {
                Request req = new Request.Builder().url(url + path).build();
                requests.add(req);
            }

            return Observable.fromIterable(requests);
        } else {
            Log.e(TAG, "Request unsuccessful: " + response.code());
        }

        return Observable.error(new Throwable());
    }

    private static void updateProgress(String progressString) {
        if (progress != null) {
            progress.postValue(progressString);
        }
    }

    @Override
    public boolean onStopCurrentWork() {
        working = false;
        updateProgress(COMPLETE_TAG);
        globalDisposable.clear();
        progress = null;

        if (Helpers.getApplicationContext() == null) {
            mRoomDatabase.close();
        }

        return super.onStopCurrentWork();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static boolean isWorking() {
        return working;
    }

    public void stopWork() {
        working = false;
        updateProgress(COMPLETE_TAG);
        if (Helpers.getApplicationContext() == null) {
            mRoomDatabase.close();
        }
    }

    public static LiveData<String> getProgress() {
        return progress;
    }
}