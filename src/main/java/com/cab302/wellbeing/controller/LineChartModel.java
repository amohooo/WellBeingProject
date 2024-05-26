package com.cab302.wellbeing.controller;

import java.time.LocalDate;

/**
 * This class represents a model for a Line Chart.
 * It contains the session date and the sum of the durations.
 */
public class LineChartModel {
    LocalDate sessionDate; // The session date
    Integer durationSum; // The sum of the durations

    /**
     * Constructs a new LineChartModel with the specified session date and duration sum.
     *
     * @param sessionDate The session date
     * @param durationSum The sum of the durations
     */
    public LineChartModel(LocalDate sessionDate, Integer durationSum) {
        this.sessionDate = sessionDate; // Set the session date
        this.durationSum = durationSum; // Set the duration sum
    }
}
