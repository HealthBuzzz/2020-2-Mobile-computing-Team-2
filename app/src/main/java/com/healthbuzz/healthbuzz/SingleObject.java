package com.healthbuzz.healthbuzz;

import android.util.Log;

import java.util.ArrayList;

class LongWrapper implements Subject {
    private final String TAG = "LongWrapper";
    private final ArrayList<Observer> observers;
    private long value;

    public LongWrapper() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        int i = observers.indexOf(o);
        if (i >= 0) {
            observers.remove(i);
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(value);
        }
    }

    public void measurementsChanged() {
        notifyObservers();
    }

    public void setValue(long value) {
        Log.e(TAG, "Value  " + value);
        this.value = value;
        measurementsChanged();  // 변경이 발생할 때, 알림을 돌리는 방법 선택
    }

    public long getValue() {
        return value;
    }
}

class SingleObject {

    //create an object of SingleObject
    private static final SingleObject instance = new SingleObject();

    public LongWrapper stretching_count = new LongWrapper();
    public LongWrapper water_count = new LongWrapper();
    public LongWrapper stretching_time_left = new LongWrapper();
    public LongWrapper water_time_left = new LongWrapper();

    //make the constructor private so that this class cannot be
    //instantiated
    private SingleObject() {
    }

    //Get the only object available
    public static SingleObject getInstance() {
        return instance;
    }
}
