package com.example.myspotify;

public class Song {

    private String id;
    private String name;

    public Song(String id, String name) {
        this.name = name;
        this.id = id;
    }

    public void removeFeat() {
        if (name.contains("(feat.")) {
            int index = name.indexOf("(feat.");
            name = name.substring(0, index);
        }
    }

    public void shortenName() {
        int MAX_LENGTH = 30;
        if (name.length() > MAX_LENGTH) {
            name = name.substring(0, MAX_LENGTH);
        }
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}