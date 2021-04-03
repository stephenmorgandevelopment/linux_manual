package com.stephenmorgandevelopment.thelinuxmanual.viewmodels;

import android.util.JsonReader;

import androidx.lifecycle.ViewModel;

import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SingleTextMatch;
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult;
import com.stephenmorgandevelopment.thelinuxmanual.ui.CommandInfoFragment;

public class CommandInfoViewModel extends ViewModel {
    public static final String TAG = CommandInfoViewModel.class.getSimpleName();

    private long id;
    private TextSearchResult searchResults;
    private int currentMatchIndex;
    private String shortName;

    public CommandInfoViewModel() {

    }

    public void searchTextFor(String query, Command command) {
        searchResults = command.searchDataForTextMatch(query);

        if (searchResults.getCount() > 0) {
            currentMatchIndex = 1;
        } else {
            currentMatchIndex = 0;
        }
    }

    public void init(Command command) {
        this.id = command.getId();
        this.shortName = command.getShortName();
    }

    public SingleTextMatch getNextMatch() {
        if(searchResults.getCount() == 0) {
            return null;
        }

        if(++currentMatchIndex > searchResults.getCount()) {
            currentMatchIndex = 1;
        }

         return searchResults.getMatchIndexes().get(currentMatchIndex-1);
    }

    public long getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public TextSearchResult getSearchResults() {
        return searchResults;
    }

    public int getCurrentMatchIndex() {
        return currentMatchIndex;
    }

    public int getResultsCount() {
        return searchResults.getCount();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        searchResults = null;
    }
}
