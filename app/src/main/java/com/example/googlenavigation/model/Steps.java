package com.example.googlenavigation.model;

/**
 * Created by harsh on 5/7/17.
 */

public class Steps {
    private Location start_location;
    private Location end_location;
    private OverviewPolyLine polyline;
    private String html_instructions;

    public Location getStart_location() {
        return start_location;
    }

    public Location getEnd_location() {
        return end_location;
    }

    public OverviewPolyLine getPolyline() {
        return polyline;
    }

    public String getHtml_instructions() {
        return html_instructions;
    }
}
