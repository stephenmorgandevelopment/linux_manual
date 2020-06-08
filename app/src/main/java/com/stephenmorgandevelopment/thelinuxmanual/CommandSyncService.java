package com.stephenmorgandevelopment.thelinuxmanual;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class CommandSyncService extends JobIntentService {
    private static int page = 1;

    public static final String DISTRO = "distro";

    public static final int JOB_ID = 5001;

    private static String syncProgress = "waiting.";

    public static final String TAG = "CommandSyncService";

    private static CompositeDisposable globalDisposable;

    static volatile boolean working = false;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if(globalDisposable == null) {
            globalDisposable = new CompositeDisposable();
        }

        String distro;
        if(intent.getExtras() == null || (distro = intent.getExtras().getString(DISTRO)) == null) {
            Log.e(TAG, "Null intent or extra passed to job service.");
            return;
        }


        if(distro.equalsIgnoreCase(Ubuntu.NAME)) {
            syncProgress = "Connecting to " + Ubuntu.BASE_URL + ".";

            try {
                syncSimpleCommands(Ubuntu.BASE_URL);
            } catch (IOException ioe) {
                Log.e("CommandSyncService", "Ioexception in syncSimpleCommands: " + ioe.getMessage());
                ioe.printStackTrace();
            }
        }
    }

    private synchronized void syncSimpleCommands(String baseUrl) throws IOException {
        Disposable disposable = HttpClient.fetchDirsHtml()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .flatMapObservable(response -> {
                    if(response.isSuccessful() && response.code() == 200) {
                        String url = Ubuntu.BASE_URL + Ubuntu.getReleaseString() + "/" + Helpers.getLocal() + "/";

                        List<String> dirPaths = Ubuntu.crawlForManDirs(response.body().string());
                        List<Request> requests =  new ArrayList<>();

                        for(String path : dirPaths) {
                            Request req = new Request.Builder().url(url + path).build();  //+ "/").build();
                            requests.add(req);
                        }

                        return Observable.fromIterable(requests);
                    } else {
                        Log.e(TAG, "Request unsuccessful: " + response.code());
                    }

                    return Observable.error(new Throwable());

                })
                .concatMap(request -> Observable.just(HttpClient.getClient().newCall(request).execute()))
                .doOnComplete(() -> {
                    Log.d(TAG, "Successfully synced commands without descriptions.");
                    working = false;
                })
                .doOnError(response -> {
                    Log.e(TAG, "The following error occurred: " + response.toString());
                    response.printStackTrace();
                })
                .subscribe(response -> {
                            String reqUrl = response.request().url().toString();

                        Log.d(TAG, "Successful response from: " + reqUrl);
                        syncProgress = "\nPulled data from " + reqUrl + "\nProcessing data.";

                        List<SimpleCommand> pageCommands = Ubuntu.crawlForManPages(response.body().string(), reqUrl);

                        syncProgress = "\nSaving data locally.";

                        if(pageCommands.size() > 0) {
                            DatabaseHelper database = DatabaseHelper.getInstance();
                            database.addCommands(pageCommands);
                        }
                }
                , error -> {
                    Log.d(TAG, "Block attempting to stop crash.");
                        });

        globalDisposable.add(disposable);
    }

    @Override
    public boolean onStopCurrentWork() {
        working = false;
        return super.onStopCurrentWork();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(Helpers.getApplicationContext() == null) {
            DatabaseHelper.getInstance().close();
        }
    }

    public static String getSyncProgress() {
        return syncProgress;
    }

    public static void enqueueWork(Context context, Intent work) {
        working = true;
        enqueueWork(context, CommandSyncService.class, JOB_ID, work);
    }

}