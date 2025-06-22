package com.s23010162.safewalk;

public class Recording {
    private String name;
    private long dateAdded;

    public Recording(String name, long dateAdded) {
        this.name = name;
        this.dateAdded = dateAdded;
    }

    public String getName() {
        return name;
    }

    public long getDateAdded() {
        return dateAdded;
    }
} 