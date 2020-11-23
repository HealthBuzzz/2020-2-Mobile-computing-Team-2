package com.healthbuzz.healthbuzz.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class TodayData {

    private int today_stretching_count;
    private int today_water_count;
    private int today_ranking;


    public TodayData(int today_stretching_count, int today_water_count, int today_ranking) {
        this.today_stretching_count = today_stretching_count;
        this.today_water_count = today_water_count;
        this.today_ranking = today_ranking;
    }

    public int getToday_stretching_count() {
        return today_stretching_count;
    }

    public int getToday_water_count() {
        return today_water_count;
    }

    public int getToday_ranking(){
        return today_ranking;
    }
}