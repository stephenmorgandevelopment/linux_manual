package com.stephenmorgandevelopment.thelinuxmanual.viewmodels;

import android.text.Editable;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CommandLookupViewModel extends ViewModel {
	private static final String TAG = CommandLookupViewModel.class.getSimpleName();

	private final SavedStateHandle savedStateHandler;
	private final  MutableLiveData<List<SimpleCommand>> matchListData =
			new MutableLiveData<>();

	private String searchText;
	private final static String SEARCH_TEXT_KEY = "SEARCH_KEY";

	private final CompositeDisposable updateDescriptionDisposables = new CompositeDisposable();

	public CommandLookupViewModel(SavedStateHandle savedStateHandle) {
		this.savedStateHandler = savedStateHandle;

		searchText = savedStateHandler.contains(SEARCH_TEXT_KEY)
				? savedStateHandler.get(SEARCH_TEXT_KEY)
				: null;
	}

	public void searchForMatchesByName(Editable query) {
		updateDescriptionDisposables.clear();
		String searchQuery = trimAndCleanSearchQuery(query);

		Disposable disposable = UbuntuRepository.getInstance()
				.getPartialMatches(searchQuery)
//				.doAfterSuccess(this::iterateMatchesAndUpdateSimpleCommands)
				.doAfterSuccess(matchListData::postValue)
//				.observeOn(AndroidSchedulers.mainThread())
				.observeOn(Schedulers.computation())
				.doOnError(error ->
						Log.d(TAG, "Error searching for matches" + error.toString()))		//Toast.makeText(getContext(), "Invalid character entered", Toast.LENGTH_LONG).show())
				.subscribe(
						this::iterateMatchesAndUpdateSimpleCommands,
//						matchListData::postValue,
						error -> Log.d(TAG, "SQL error: " + error.toString()));

		MainActivityViewModel.addDisposable(disposable);
	}

	private void iterateMatchesAndUpdateSimpleCommands(List<SimpleCommand> simpleCommands) {
//		Disposable disposable = Observable.fromIterable(simpleCommands)
//				.forEach()
//				.observeOn(AndroidSchedulers.mainThread())
//				.doAfterNext(matchListData.postValue())
//				.subscribeOn(Schedulers.computation())
//				.subscribe(value -> null,
//						error -> null
//						);


		for(SimpleCommand command : simpleCommands) {
			if(command.needsDescription()) {
				Disposable disposable = UbuntuRepository.getInstance().fetchDescription(command)
						.subscribeOn(Schedulers.computation())
						.doAfterSuccess(simpleCommand -> matchListData.postValue(simpleCommands))
//						.observeOn(AndroidSchedulers.mainThread())
						.observeOn(Schedulers.computation())
						.subscribe(
								simpleCommand -> command.setDescription(simpleCommand.getDescription()),
								error -> Log.i(TAG, "Error fetching description: " + error.getMessage())
						);
			}
		}
	}

	private void updateSimpleCommand() {

		;
	}

	public void setSavedSearchText(Editable text) {
		searchText = String.valueOf(text).trim();
		savedStateHandler.set(SEARCH_TEXT_KEY, text);
	}

	public String getSearchText() {
		return searchText;
	}

	private String trimAndCleanSearchQuery(Editable s) {
		String searchQuery = String.valueOf(s).replaceAll("'", "");
		searchQuery = searchQuery.replaceAll("%", "");
		searchQuery = searchQuery.replaceAll("^(/W/s)$", "");

		return searchQuery;
	}

	public LiveData<List<SimpleCommand>> getMatchListData() {
		return matchListData;
	}

}
