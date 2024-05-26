package com.cab302.wellbeing.controller;

/**
 * This class represents a model for a Bar Chart.
 * It contains the URL of the chart and the sum of the durations.
 */
public class BarChartModel {
    String url;
    // The sum of the durations

    /**
     * Constructs a durationSum The sum of the durations.
     */
    public Integer durationSum; // Make it public for easy access

    /**
     * Constructs a new BarChartModel with the specified URL and duration sum.
     *
     * @param url The URL of the bar chart
     * @param durationSum The sum of the durations
     */
    public BarChartModel(String url, Integer durationSum) {
        this.url = url; // Set the URL
        this.durationSum = durationSum; // Set the duration sum
    }
}