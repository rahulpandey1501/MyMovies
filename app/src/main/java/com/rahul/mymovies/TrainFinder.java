package com.rahul.mymovies;

/**
 * Created by rahul on 10/3/16.
 */
public class TrainFinder {
    String name, code, state_name;

    public String getName() {
        return name != null?name:"";
    }

    public String getCode() {
        return code != null?code:"";
    }

    public String getState_name() {
        return state_name;
    }

    public TrainFinder(String name, String code, String state_name) {

        this.name = name;
        this.code = code;
        this.state_name = state_name;
    }
}
