package com.example.googlenavigation;

import com.example.googlenavigation.model.DirectionResult;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface GoogleRouteAPI {

    @GET("/maps/api/directions/json")
    Call<DirectionResult> getRoutes(
            @Query("origin") String param1,
            @Query("destination") String param2,
            @Query("sensor") Boolean param3,
            @Query("mode") String param4,
            @Query("alternatives") Boolean param5);
}
