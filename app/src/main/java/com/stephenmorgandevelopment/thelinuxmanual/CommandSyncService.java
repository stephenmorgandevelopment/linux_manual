package com.stephenmorgandevelopment.thelinuxmanual;

import android.app.Application;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.strictmode.IntentReceiverLeakedViolation;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.stephenmorgandevelopment.thelinuxmanual.distros.Distribution;
import com.stephenmorgandevelopment.thelinuxmanual.distros.LinuxDistro;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.schedulers.IoScheduler;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommandSyncService extends JobIntentService {
    Distribution distribution;
    private static int page = 1;

    public static final String DISTRO = "distro";
    public static final String SYNC_TYPE = "sync_type";
    public static final String SYNC_NAMES = "sync_names";
    public static final String SYNC_DESCRIPTIONS = "sync_descriptions";
    public static final int JOB_ID = 5001;

    private static String syncProgress;
//    public static final int COMMAND_DESCRIPTION_JOB_ID = 5101;

    public static final String TAG = "CommandSyncService";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String distro = "error";
        if(intent.getExtras() != null) {
            distro = intent.getExtras().getString(DISTRO);
            if(distro == null || distro.isEmpty()) {
                return;
            }
        }


        if(distro.equalsIgnoreCase(Ubuntu.NAME)) {
            distribution = new Ubuntu();
            try {
                if(intent.getStringExtra(SYNC_TYPE).equals(SYNC_NAMES)) {
                    syncSimpleCommands(Ubuntu.BASE_URL);
                } else {
//                    syncCommandDescriptions();
                }

            } catch (IOException ioe) {
                Log.e("CommandSyncService", "Ioexception in syncSimpleCommands: " + ioe.getMessage());
                ioe.printStackTrace();
            }
        }
    }

    private synchronized void syncSimpleCommands(String baseUrl) throws IOException {
        syncProgress = "Connecting to " + Ubuntu.BASE_URL + "";

        Disposable disposable = HttpClient.fetchDirsHtml()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .flatMapObservable(response -> {
                    if(response.isSuccessful() && response.code() == 200) {
                        String url = Ubuntu.BASE_URL + Ubuntu.getReleaseString() + "/" + Helpers.getLocal() + "/";

                        List<String> dirPaths = distribution.crawlForManDirs(response.body().string());
                        List<Request> requests =  new ArrayList<>();

                        for(String path : dirPaths) {
                            Request req = new Request.Builder().url(url + path).build();  //+ "/").build();
                            requests.add(req);
                        }

                        return Observable.fromIterable(requests);
//                        return Observable.fromArray(requests.toArray(new Request[0]));
                    } else {
                        Log.e(TAG, "Request unsuccessful: " + response.code());
                    }

                    return Observable.error(new Throwable());

                })
                .concatMap(request -> Observable.just(HttpClient.getClient().newCall(request).execute()))
//                .concatMap(request -> {
//                    return  Observable.just(HttpClient.getClient().newCall(request).execute());
//                })
                .doOnComplete(() -> {
                    Log.d(TAG, "Successfully synced commands without descriptions.");
                    MainActivity.working = false;
                    //syncCommandDescriptions();

                    //Ubuntu.writeSimpleCommandsToDisk(true);
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

                        Ubuntu.addToCommandList(distribution.crawlForManPages(response.body().string(), reqUrl));

                        Ubuntu.writeSimpleCommandsToDisk(true, page++);
                        //Toast.makeText(getBaseContext(), "Successful response from: " + reqUrl, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private synchronized void syncCommandDescriptions() {
        page = 1;

        if(Ubuntu.getCommandsList() != null && Ubuntu.getCommandsList().size() > 0) {
            Log.d(TAG, "Queueing requests for command descriptions.");
            for(SimpleCommand command : Ubuntu.getCommandsList()) {
                Request req = new Request.Builder().url(command.getUrl()).build();
                HttpClient.getClient().newCall(req).enqueue(updateCommand(command));
            }

        } else {
            Log.e(TAG, "Commands list is empty or null.");
        }

        Observable.fromIterable(HttpClient.getClient().dispatcher().queuedCalls())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    Log.d(TAG, "Successfully synced commands descriptions.  Writing JSON to disk.");
                    Ubuntu.writeSimpleCommandsToDisk(true, page++);
                }).subscribe();
    }

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



//    public Observable<List<Call>> fetchDirHtml(List<String> paths) {
//        String url = Ubuntu.BASE_URL + Ubuntu.getReleaseString() + "/" + Helpers.getLocal() + "/";
//        for(String path : paths) {
//            Request req = new Request.Builder().url(url + path + "/").build();
//            HttpClient.getClient().newCall(req).enqueue(new Callback() {
//                @Override
//                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                    Log.e(TAG, "Request unsuccessful: " + e.toString());
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                    String reqUrl = response.request().url().toString();
//                    Log.d(TAG, "Successful response from: " + reqUrl);
//                    Ubuntu.addToCommandList(distribution.crawlForManPages(response.body().string(), reqUrl));
//                }
//            });
//        }
//
//        return Observable.just(HttpClient.getClient().dispatcher().queuedCalls());
//
//    }

    public static String getSyncProgress() {
        return syncProgress;
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CommandSyncService.class, JOB_ID, work);
    }

}
