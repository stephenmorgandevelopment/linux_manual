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
                    MainActivity.working = false;
                })
                .doOnError(response -> {
                    Log.e(TAG, "The following error occurred: " + response.toString());
                    response.printStackTrace();
                })
                .forEach(response -> {
                    if(response.isSuccessful() && response.code() == 200) {
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
                });

        globalDisposable.add(disposable);
    }

//    private void updateLocalCache(List<SimpleCommand> commands) {
//        DatabaseHelper database = DatabaseHelper.getInstance();
//        database.addCommands(commands);
//    }

//    private synchronized void syncCommandDescriptions() {
//        page = 1;
//
//        if(Ubuntu.getCommandsList() != null && Ubuntu.getCommandsList().size() > 0) {
//            Log.d(TAG, "Queueing requests for command descriptions.");
//            for(SimpleCommand command : Ubuntu.getCommandsList()) {
//                Request req = new Request.Builder().url(command.getUrl()).build();
//                HttpClient.getClient().newCall(req).enqueue(updateCommand(command));
//            }
//
//        } else {
//            Log.e(TAG, "Commands list is empty or null.");
//        }
//
//        Observable.fromIterable(HttpClient.getClient().dispatcher().queuedCalls())
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnComplete(() -> {
//                    Log.d(TAG, "Successfully synced commands descriptions.  Writing JSON to disk.");
//                    Ubuntu.writeSimpleCommandsToDisk(true, page++);
//                }).subscribe();
//    }

    private synchronized Callback updateCommand(final SimpleCommand command) {
        return new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "Failed updating command with the following error: " + e.toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() == 200 && response.body() != null) {
                    Log.d(TAG, "Successful Description response for: " + response.request().url().toString());
                    Ubuntu.addDescriptionToSimpleCommand(command, response.body().string());
                }
            }
        };
    }

    @Override
    public boolean onStopCurrentWork() {

        return super.onStopCurrentWork();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        if(globalDisposable != null && globalDisposable.size() > 0) {
//            globalDisposable.clear();
//        }
    }

    public static String getSyncProgress() {
        return syncProgress;
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CommandSyncService.class, JOB_ID, work);
    }

}