package com.example.eliezer.jchatapp;

/**
 * Created by AkshayeJH on 30/11/17.
 */

public class Conv {

    public boolean seen;
    public long timestamp;
    public int counter;

    public Conv(){

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public Conv(boolean seen, long timestamp,int counter) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}
