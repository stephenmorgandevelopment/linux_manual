package com.stephenmorgandevelopment.thelinuxmanual.models;

import java.util.List;
import java.util.Map;

public class TextSearchResult {
	private final String query;
	private final List<SingleTextMatch> matchIndexes;

	public TextSearchResult(String query, List<SingleTextMatch> matchIndexes, int count) {
		this.query = query;
		this.matchIndexes = matchIndexes;
	}

	public String getQuery() {
		return query;
	}

	public int getCount() {
		return matchIndexes.size();
	}

	public List<SingleTextMatch> getMatchIndexes() {
		return matchIndexes;
	}
}
