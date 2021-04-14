package com.stephenmorgandevelopment.thelinuxmanual.viewmodels;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityViewModel extends ViewModel {
	private final static String TAG = "MainActivityViewModel";
	private final static String COMMANDS_LIST_KEY = "COMMANDS_LIST";

	private static final Map<Long, Boolean> loadingInfo = new LinkedHashMap<>();
	private LiveData<String> syncProgress;
	private final MutableLiveData<Command> addPageData = new MutableLiveData<>();
	private final MutableLiveData<Throwable> onErrorData = new MutableLiveData<>();
	private final List<Command> commandsList;
	private final SavedStateHandle savedStateHandler;
	private final static CompositeDisposable disposables = new CompositeDisposable();

	public MainActivityViewModel(SavedStateHandle savedStateHandle) {
		super();
		this.savedStateHandler = savedStateHandle;

		commandsList = savedStateHandler.contains(COMMANDS_LIST_KEY)
				? savedStateHandler.get(COMMANDS_LIST_KEY)
				: new ArrayList<>();
	}

	public void syncDatabase() {
		syncProgress = UbuntuRepository.getInstance().launchSyncService();
	}

	public void loadManpage(SimpleCommand simpleCommand) {
		Disposable disposable = UbuntuRepository.getInstance()
				.getCommandData(simpleCommand)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.computation())
				.flatMap(list ->
						Single.just(new Command(simpleCommand.getId(), list)))
				.doOnError(error -> {
					Log.d("MainActivityViewModel",
							"Error pulling man page - "
									+ simpleCommand.getName());
				})
				.subscribe(addPageData::postValue, onErrorData::postValue);

		disposables.add(disposable);
	}

	public boolean isLoading(long id) {
		if (!loadingInfo.containsKey(id)) {
			return false;
		}
		return loadingInfo.get(id);
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
		savedStateHandler.set(COMMANDS_LIST_KEY, commandsList);
	}

	public void removeCommandFromCommandList(Command command) {
		commandsList.remove(command);
	}

	public void clearCommandsList() {
		commandsList.clear();
	}

	public List<Command> getCommandsList() {
		return commandsList;
	}

	public Command getCommandFromListById(long id) {
		for (Command command : commandsList) {
			if (command.getId() == id) {
				return command;
			}
		}

		return null;
	}

	public boolean commandsListHasId(long id) {
		for (Command command : commandsList) {
			if (command.getId() == id) {
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

	public void clearAddPageData() {
		addPageData.setValue(null);
	}

	public LiveData<Throwable> getOnErrorData() {
		return onErrorData;
	}

	public static void addDisposable(Disposable disposable) {
		disposables.add(disposable);
	}

	public static void cleanup() {
		disposables.clear();
	}

//	public void setSavedSearchText(String text) {
//		searchText = text;
//		savedStateHandler.set(SEARCH_TEXT_KEY, text);
//	}
//
//	public String getSearchText() {
//		return searchText;
//	}
}
