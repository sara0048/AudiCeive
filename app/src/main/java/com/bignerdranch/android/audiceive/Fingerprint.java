package com.bignerdranch.android.audiceive;

public class Fingerprint {
    private short anchorFrequency;
    private short pointFrequency;
    private byte delta;
    private short absoluteTime;
    private int sceneID;

    public Fingerprint(short anchorFrequency, short pointFrequency, byte delta, short absoluteTime, int sceneID) {
        this.anchorFrequency = anchorFrequency;
        this.pointFrequency = pointFrequency;
        this.delta = delta;
        this.absoluteTime = absoluteTime;
        this.sceneID = sceneID;
    }

    public short getAnchorFrequency() {
        return anchorFrequency;
    }

    public short getPointFrequency() {
        return pointFrequency;
    }

    public byte getDelta() {
        return delta;
    }

    public short getAbsoluteTime() {
        return absoluteTime;
    }

    public int getSceneID() {
        return sceneID;
    }
}
