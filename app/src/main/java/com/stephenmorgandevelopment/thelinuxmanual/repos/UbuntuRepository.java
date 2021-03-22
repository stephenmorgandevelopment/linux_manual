package com.stephenmorgandevelopment.thelinuxmanual.repos;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.stephenmorgandevelopment.thelinuxmanual.CommandSyncService;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class UbuntuRepository implements ManPageRepository {
    private static UbuntuRepository instance;
    public static final String TAG = "UbuntuRepository";

    public static UbuntuRepository getInstance() {
        if(instance == null) {
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

    public Command getCommandById(long id) {


        return null;
    }

    

    public Single<Map<String, String>> fetchCommandData(String pageUrl) {
       return HttpClient.fetchCommandManPage(pageUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMap(Ubuntu::crawlForCommandInfo);
    }

//    private void getManPage(SimpleCommand command) {
//        Disposable disposable = HttpClient.fetchCommandManPage(command.getUrl())
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.computation())
//                .flatMapCompletable(response -> {
//                    if (response.isSuccessful() && response.code() == 200) {
//                        ((MainActivity) requireActivity())
//                                .getPagerAdapter()
//                                .addPage(id, Ubuntu.crawlForCommandInfo(response.body().string()));
//
//                        return Completable.complete();
//                    }
//
//                    return Completable.error(new Throwable("Response returned with code: " + response.code()));
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnError(error -> {
//                    fetchingDataDialog.setVisibility(View.GONE);
//                    Toast.makeText(getContext(), "Error fetching data\n" + error.getMessage(), Toast.LENGTH_LONG).show();
//                })
//                .subscribe(() -> {
//                    ((MainActivity) requireActivity()).getPagerAdapter().notifyDataSetChanged();
//                    fetchingDataDialog.setVisibility(View.GONE);
//                    viewModel.setLoading(id, false);
//
//                }, error -> {
//                    Log.d(TAG, "Error in fetchCommandPage");
//                    error.printStackTrace();
//                    viewModel.setLoading(id, false);
//                });
//
//        disposables.put(id, disposable);
//    }

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
