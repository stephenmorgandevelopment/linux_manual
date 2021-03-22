package com.stephenmorgandevelopment.thelinuxmanual;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityViewModel extends AndroidViewModel {
    private final static String TAG = "MainActivityViewModel";

    private static volatile Map<Long, Boolean> loadingInfo = new LinkedHashMap<>();
    private LiveData<String> syncProgress;
    private final MutableLiveData<Command> addPageData = new MutableLiveData<>();
    private final List<Command> commandsList = new ArrayList<>();
    private final Map<Long, Disposable> disposables = new HashMap<>();

    private String searchText = null;

    public MainActivityViewModel(Application application) {
        super(application);

    }

    public void syncDatabase() {
        syncProgress = UbuntuRepository.getInstance().launchSyncService();
    }

    public void loadManpage(SimpleCommand simpleCommand) {
        Disposable disposable = UbuntuRepository.getInstance()
                .fetchCommandData(simpleCommand.getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMap(list -> Single.just(new Command(simpleCommand.getId(), list)))
                .doOnError(error -> {
                    Log.d("MainActivityViewModel",
                            "Error pulling man page - "
                                    + simpleCommand.getName());
                })
                .subscribe(addPageData::postValue, error -> {
                    Log.d(TAG, "Error in fetchCommandPage");
                    error.printStackTrace();
                });


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
    }

//    public void addCommandToPageDataAndNotify(Command command) {
//        addPageData.postValue(command);
//        List<Command> tempCommandList = pageData.getValue() == null
//                ? new ArrayList<>()
//                : pageData.getValue();
//        tempCommandList.add(command);
//        pageData.postValue(tempCommandList);
//    }

    public boolean isLoading(long id) {
        if (!loadingInfo.containsKey(id)) {
            return false;
        }
        return loadingInfo.get(id);
    }

    public boolean isLoading(Command command) {
        return isLoading(command.getId());
    }

    public void setSearchText(String text) {
        searchText = text;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setLoading(long id, boolean loading) {
        loadingInfo.put(id, loading);
    }

    public void removeLoadingKey(long id) {
        loadingInfo.remove(id);
    }

    public void addCommandToCommandList(Command command) {
        commandsList.add(command);
        removeLoadingKey(command.getId());
    }

    public List<Command> getCommandsList() {
        return commandsList;
    }

    public boolean commandsListHasId(long id) {
        for(Command command : commandsList) {
            if(command.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public LiveData<String> getSyncProgress() {
        return syncProgress;
    }

    public LiveData<Command> getAddPageData() {
        return addPageData;
    }
}
