package com.stephenmorgandevelopment.thelinuxmanual;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
import okhttp3.Response;

public class CommandSyncService extends JobIntentService {
    public static final String DISTRO = "distro";
    public static final String COMPLETE_TAG = "complete";
    public static final int JOB_ID = 5001;
    public static final String TAG = "CommandSyncService";

    private static MutableLiveData<String> progress;

    private static CompositeDisposable globalDisposable;

    private static volatile boolean working = false;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        progress.postValue(progress.getValue().concat("waiting..."));

        if (globalDisposable == null) {
            globalDisposable = new CompositeDisposable();
        }

        progress.postValue("\n\nConnecting to " + UbuntuHtmlAdapter.BASE_URL + ".");

        try {
            syncSimpleCommands();
        } catch(IOException ioe) {
            Log.e(TAG, "Error in syncSimpleCommands: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    private synchronized void syncSimpleCommands() throws IOException {
        Disposable disposable = HttpClient.fetchDirsHtml()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMapObservable(this::mapHtmlToManDirs)
                .concatMap(request -> Observable.just(HttpClient.getClient().newCall(request).execute()))
                .doOnComplete(CommandSyncService::stopWork)
                .subscribe(this::processAndSaveResponse, error -> {
                            Log.d(TAG, error.getMessage());
                            Log.d(TAG, error.toString());
                        });

        globalDisposable.add(disposable);
    }

    private void processAndSaveResponse(Response response) throws IOException {
        String reqUrl = response.request().url().toString();

        if(reqUrl.endsWith("3/")) {
            progress.postValue("\nPulled data from " + reqUrl
                    + "\nLarge data set, longest processing."
                    + "\nProcessing data...");
        } else {
            progress.postValue("\nPulled data from " + reqUrl
                    + "\nProcessing data...");
        }

        List<SimpleCommand> pageCommands = UbuntuHtmlAdapter.crawlForManPages(response.body().string(), reqUrl);

        progress.postValue("\nSaving data locally...");

        if (pageCommands.size() > 0) {
            DatabaseHelper.getInstance().addCommands(pageCommands);
        }
    }

    public Observable<Request> mapHtmlToManDirs(Response response) throws IOException {
        if (response.isSuccessful() && response.code() == 200) {
            String url = UbuntuHtmlAdapter.BASE_URL + UbuntuHtmlAdapter.getReleaseString() + "/" + Helpers.getLocal() + "/";

            List<String> dirPaths = UbuntuHtmlAdapter.crawlForManDirs(response.body().string());
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

    @Override
    public boolean onStopCurrentWork() {
        working = false;
        progress.postValue(COMPLETE_TAG);
        return super.onStopCurrentWork();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Helpers.getApplicationContext() == null) {
            DatabaseHelper.getInstance().close();
        }
    }

    public static void enqueueWork(Context context, Intent work, MutableLiveData<String> prog) {
        progress = prog;
        working = true;
        enqueueWork(context, CommandSyncService.class, JOB_ID, work);
    }

    public static boolean isWorking() {
        return working;
    }

    public static void stopWork() {
        working = false;
        progress.postValue(COMPLETE_TAG);
    }

    public static LiveData<String> getProgress() {
        return progress;
    }
}