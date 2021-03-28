package com.stephenmorgandevelopment.thelinuxmanual.repos;

import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService;
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class UbuntuRepository implements ManPageRepository {
    private static UbuntuRepository instance;
    public static final String TAG = "UbuntuRepository";

    public static UbuntuRepository getInstance() {
        if (instance == null) {
            instance = new UbuntuRepository();
        }
        return instance;
    }

    private UbuntuRepository() {

    }

    public void syncDatabase() {

    }

    public List<SimpleCommand> searchDatabase() {
        return null;
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
                .flatMap(Ubuntu::crawlForCommandInfo);
    }

    public LiveData<String> launchSyncService() {
        if (!CommandSyncService.isWorking()) {
            MutableLiveData<String> progress = new MutableLiveData<>();
            progress.postValue("Running initial sync to build local command database.");

            Intent intent = new Intent();
            intent.putExtra(CommandSyncService.DISTRO, Ubuntu.NAME);

            CommandSyncService.enqueueWork(Helpers.getApplicationContext(), intent, progress);

            return progress;
        }

        return CommandSyncService.getProgress();
    }
}
