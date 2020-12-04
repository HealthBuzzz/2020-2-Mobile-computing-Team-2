package com.healthbuzz.healthbuzz.data.model;

public class TodayStretching {
    private int hour, minute;

    public TodayStretching(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }
}
