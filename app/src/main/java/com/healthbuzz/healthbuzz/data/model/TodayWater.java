package com.healthbuzz.healthbuzz.data.model;

public class TodayWater {
    private int hour, minute, amount;

    public TodayWater(int hour, int minute, int amount) {
        this.hour = hour;
        this.minute = minute;
        this.amount = amount;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getAmount() {
        return amount;
    }
}
