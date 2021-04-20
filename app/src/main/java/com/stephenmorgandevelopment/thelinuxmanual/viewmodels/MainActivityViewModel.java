package com.stephenmorgandevelopment.thelinuxmanual.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.data.LocalStorage;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
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

		commandsList = new ArrayList<>();
	}

	public void syncDatabase() {
		syncProgress = UbuntuRepository.getInstance().launchSyncService();
	}

	public void changeRelease(UbuntuHtmlApiConverter.Release release) {
		Preferences.setRelease(release.getName());
		UbuntuHtmlApiConverter.setRelease(release);
		DatabaseHelper.changeTable(release.getName());
		savedStateHandler.remove(COMMANDS_LIST_KEY);

		if(DatabaseHelper.getInstance().hasData()) {
			return;
		}

		if(Helpers.hasInternet()) {
			syncDatabase();
			LocalStorage.getInstance().wipeAll();
		}

		onErrorData.postValue(
				new Throwable(Helpers.string(R.string.release_sync_no_data_no_internet)));
	}

	public Observable<Command> loadSavedCommands() {
		List<SimpleCommand> simpleCommands = DatabaseHelper.getInstance()
				.getCommandsByIds(savedStateHandler.get(COMMANDS_LIST_KEY));

		return Observable.fromIterable(simpleCommands)
				.concatMap(simpleCommand -> Observable.just(
						UbuntuRepository.getInstance().getCommandFromStorage(simpleCommand))
				).subscribeOn(Schedulers.io());
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
		commandsList.add(0, command);
		removeLoadingKey(command.getId());
		savedStateHandler.set(COMMANDS_LIST_KEY, getIdsFromCommandList());
	}

	public List<Long> getIdsFromCommandList() {
		List<Long> ids = new ArrayList<>();
		for(Command command : commandsList) {
			ids.add(command.getId());
		}
		return ids;
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

	public boolean hasSavedState() {
		return savedStateHandler.get(COMMANDS_LIST_KEY) != null
				&& ((List<Long>)savedStateHandler.get(COMMANDS_LIST_KEY)).size() > 0;
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

	@Override
	protected void onCleared() {
		super.onCleared();

		this.savedStateHandler.remove(COMMANDS_LIST_KEY);
		this.commandsList.clear();
	}
}
