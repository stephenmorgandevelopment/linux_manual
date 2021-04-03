package com.stephenmorgandevelopment.thelinuxmanual.models;

public class SingleTextMatch {
    private final String section;
    private final int index;

    public SingleTextMatch(String section, int index) {
        this.section = section;
        this.index = index;
    }

    public String getSection() {
        return section;
    }

    public int getIndex() {
        return index;
    }
}
