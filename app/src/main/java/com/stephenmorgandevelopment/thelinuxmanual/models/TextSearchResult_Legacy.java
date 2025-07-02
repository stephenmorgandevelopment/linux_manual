package com.stephenmorgandevelopment.thelinuxmanual.models;

import java.util.List;


public record TextSearchResult_Legacy(String query, List<SingleTextMatch> matchIndexes) {

    public int getCount() {
        return matchIndexes.size();
    }

    public SingleTextMatch getMatch(int index) {
        return matchIndexes.get(index);
    }
}
