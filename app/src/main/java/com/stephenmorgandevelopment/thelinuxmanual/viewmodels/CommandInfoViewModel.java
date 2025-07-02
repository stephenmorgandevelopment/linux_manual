package com.stephenmorgandevelopment.thelinuxmanual.viewmodels;
//
//import androidx.lifecycle.ViewModel;
//
//import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
//import com.stephenmorgandevelopment.thelinuxmanual.models.SingleTextMatch;
//import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult;
//
//public class CommandInfoViewModel extends ViewModel {
//    public static final String TAG = CommandInfoViewModel.class.getSimpleName();
//
//    private long id;
//    private TextSearchResult searchResults;
//    private int currentMatchIndex;
//    private String shortName;
//
//    public CommandInfoViewModel() {
//
//    }
//
//    public void init(Command command) {
//        this.id = command.getId();
//        this.shortName = command.getShortName();
//    }
//
//    public void searchTextFor(String query, Command command) {
//        searchResults = command.searchDataForTextMatch(query);
//
//        if (searchResults.getCount() > 0) {
//            currentMatchIndex = 1;
//        } else {
//            currentMatchIndex = 0;
//        }
//    }
//
//    public boolean hasSearchResults() {
//        return !(searchResults == null || searchResults.getCount() == 0);
//    }
//
//    public SingleTextMatch getCurrentMatch() {
//        if(searchResults == null || searchResults.getCount() == 0) {
//            return null;
//        }
//
//        return searchResults.getMatch(currentMatchIndex-1);
//    }
//
//    public SingleTextMatch getNextMatch() {
//        if(searchResults == null || searchResults.getCount() == 0) {
//            return null;
//        }
//
//        if(++currentMatchIndex > searchResults.getCount()) {
//            currentMatchIndex = 1;
//        }
//
//         return searchResults.getMatch(currentMatchIndex-1);
//    }
//
//    public SingleTextMatch getPrevMatch() {
//        if(searchResults == null || searchResults.getCount() == 0) {
//            return null;
//        }
//
//        if(--currentMatchIndex == 0) {
//            currentMatchIndex = searchResults.getCount();
//        }
//
//        return searchResults.getMatch(currentMatchIndex-1);
//    }
//
//    public int calcEndIndex(int startIndex) {
//        return startIndex + searchResults.getQuery().length();
//    }
//
//    public long getId() {
//        return id;
//    }
//
//    public String getShortName() {
//        return shortName;
//    }
//
//    public TextSearchResult getSearchResults() {
//        return searchResults;
//    }
//
//    public String getPositionOfSizeString() {
//        return currentMatchIndex + "/" + searchResults.getCount();
//    }
//
//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        searchResults = null;
//    }
//}
