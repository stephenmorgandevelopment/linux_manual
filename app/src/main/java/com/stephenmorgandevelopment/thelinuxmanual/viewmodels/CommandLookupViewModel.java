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

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CommandLookupViewModel extends ViewModel {
	private static final String TAG = CommandLookupViewModel.class.getSimpleName();

	private final SavedStateHandle savedStateHandler;
	private final MutableLiveData<List<SimpleCommand>> matchListData = new MutableLiveData<>();


	private String searchText;
	private final static String SEARCH_TEXT_KEY = "SEARCH_KEY";

	private static final CompositeDisposable updateDescriptionDisposables = new CompositeDisposable();

	public CommandLookupViewModel(SavedStateHandle savedStateHandle) {
		super();
		this.savedStateHandler = savedStateHandle;

		searchText = savedStateHandler.contains(SEARCH_TEXT_KEY)
				? savedStateHandler.get(SEARCH_TEXT_KEY)
				: null;
	}

	public void searchForMatchesByName(Editable query) {
		updateDescriptionDisposables.clear();
		String searchQuery = trimAndCleanSearchQuery(query);

		Disposable disposable = UbuntuRepository.getInstance().getPartialMatches(searchQuery)
				.doAfterSuccess(matchListData::postValue)
				.doOnError(error ->
						Log.d(TAG, "Error searching for matches" + error.toString()))
				.subscribe();

		MainActivityViewModel.addDisposable(disposable);
	}

	public void setSavedSearchText(String text) {
		searchText = text;
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

	public static void addDisposable(Disposable disposable) {
		updateDescriptionDisposables.add(disposable);
	}

	public static void cleanup() {
		updateDescriptionDisposables.clear();
	}
}
