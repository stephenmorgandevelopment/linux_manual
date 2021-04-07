package com.stephenmorgandevelopment.thelinuxmanual.repos;

import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService;
import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UbuntuRepository implements ManPageRepository {
    private static UbuntuRepository instance;
    public static final String TAG = "UbuntuRepository";

    private static CompositeDisposable disposables;

    public static UbuntuRepository getInstance() {
        if (instance == null) {
            instance = new UbuntuRepository();
        }
        return instance;
    }

    private UbuntuRepository() {
        disposables = new CompositeDisposable();
    }

    public Single<Map<String, String>> getCommandData(SimpleCommand simpleCommand) {
        final LocalStorage storage = LocalStorage.getInstance();

        if(storage.hasCommand(simpleCommand.getId())) {
            try {
                return Single.just(storage.loadCommand(simpleCommand.getId()).getData());
            } catch (IOException ioe) {
                Log.i(TAG, "Unexpected file error loading - " + simpleCommand.getName() +": " + ioe.getMessage());
            }
        }

        if(Helpers.hasInternet()) {
            return fetchCommandData(simpleCommand.getUrl())
                    .doAfterSuccess((dataMap) -> {
                        storage.saveCommand(new Command(simpleCommand.getId(), dataMap));
                    });
        }

        return Single.error(new Throwable("Must have internet."));
    }

    public Single<Map<String, String>> fetchCommandData(String pageUrl) {
        return HttpClient.fetchCommandManPage(pageUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMap(UbuntuHtmlAdapter::crawlForCommandInfo);
    }

    public LiveData<String> updateDescription(SimpleCommand match) {
        MutableLiveData<String> liveDescription = new MutableLiveData<>();

        Disposable disposable = HttpClient.fetchDescription(match)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .flatMap(response -> {
                    match.setDescription(UbuntuHtmlAdapter.crawlForDescription(response.body().string()));
                    return Single.just(match);
                })
                .doOnSuccess(simpleCommand -> {
                    liveDescription.postValue(simpleCommand.getDescription());
                })
                .doOnError(error -> {
                    liveDescription.postValue(Helpers.getApplicationContext()
                            .getString(R.string.unable_to_fetch));

                    Log.i(TAG, error.getMessage());
                })
                .subscribe(simpleCommand -> {
                            DatabaseHelper.getInstance().updateCommand(simpleCommand);
                        }
                        , error -> {
                            Log.e(TAG, error.toString());
                        });

        disposables.add(disposable);
        return liveDescription;
    }

    public LiveData<String> launchSyncService() {
        if (!CommandSyncService.isWorking()) {
            Intent intent = new Intent();
            intent.putExtra(CommandSyncService.DISTRO, UbuntuHtmlAdapter.NAME);

            return CommandSyncService.enqueueWork(Helpers.getApplicationContext(), intent);
        }

        return CommandSyncService.getProgress();
    }

    public static void cleanBackgroundThreads() {
        if(disposables != null) {
            disposables.clear();
        }
    }
}
