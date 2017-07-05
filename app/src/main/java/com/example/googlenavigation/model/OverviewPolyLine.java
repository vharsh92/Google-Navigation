package com.example.googlenavigation.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harsh on 5/7/17.
 */

public class OverviewPolyLine {
    @SerializedName("points")
    public String points;

    public String getPoints() {
        return points;
    }
}
