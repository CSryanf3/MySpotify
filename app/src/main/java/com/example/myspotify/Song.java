package com.example.myspotify;

public class Song {

    private String id;
    private String name;

    public Song(String id, String name) {
        System.out.println(name);
        if (name.contains("(feat.")) {
            int index = name.indexOf("(feat.");
            name = name.substring(0, index);
        }
        System.out.println(name);
        this.name = name;
        this.id = id;
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