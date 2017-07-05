package com.example.googlenavigation.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionResult {
    @SerializedName("routes")
    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }
}