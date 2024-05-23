package com.cab302.wellbeing.controller;

import java.time.LocalDate;

public class LineChartModel {
    LocalDate sessionDate;
    Integer durationSum;

    public LineChartModel(LocalDate sessionDate, Integer durationSum) {
        this.sessionDate = sessionDate;
        this.durationSum = durationSum;
    }
}
