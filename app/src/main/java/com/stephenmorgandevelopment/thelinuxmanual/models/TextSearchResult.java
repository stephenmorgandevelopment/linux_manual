package com.stephenmorgandevelopment.thelinuxmanual.models;

import java.util.List;
import java.util.Map;

public class TextSearchResult {
	private String query;
	private int count;
	//	private List<Integer> matchIndexes = new ArrayList<>();
	private Map<String, List<Integer>> matchIndexes;

	public TextSearchResult(String query, Map<String, List<Integer>> matchIndexes, int count) {
		this.query = query;
		this.matchIndexes = matchIndexes;
		this.count = count;
	}

	public String getQuery() {
		return query;
	}

	public int getCount() {
		return count;
	}

	public Map<String, List<Integer>> getMatchIndexes() {
		return matchIndexes;
	}

}
