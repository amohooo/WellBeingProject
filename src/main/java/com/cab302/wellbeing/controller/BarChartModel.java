package com.cab302.wellbeing.controller;

public class BarChartModel {
    String url;
    public Integer durationSum;

    public BarChartModel(String url, Integer durationSum) {
        this.url = url;
        this.durationSum = durationSum;
    }
}
