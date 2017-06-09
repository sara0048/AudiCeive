package com.bignerdranch.android.audiceive;

public class Scene {
    String name;
    String address;
    String details;
    String link;
    int imageID;
    int sceneID;

    Scene(String name, String address, String details, String link, int imageID, int sceneID) {
        this.name = name;
        this.address = address;
        this.details = details;
        this.link = link;
        this.imageID = imageID;
        this.sceneID = sceneID;
    }
}
