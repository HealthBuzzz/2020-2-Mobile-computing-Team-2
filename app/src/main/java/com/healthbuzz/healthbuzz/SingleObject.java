package com.healthbuzz.healthbuzz;

import java.util.ArrayList;

class LongWrapper implements Subject {
    private ArrayList<Observer> observers;
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

    public void setMeasurements(long value) {
        this.value = value;
        measurementsChanged();  // 변경이 발생할 때, 알림을 돌리는 방법 선택
    }

    public long getValue() {
        return this.value;
    }
}
public class SingleObject {

    //create an object of SingleObject
    private static SingleObject instance = new SingleObject();

    public LongWrapper stretching_count, water_count, stretching_time_left, water_time_left;

    //make the constructor private so that this class cannot be
    //instantiated
    private SingleObject(){}

    //Get the only object available
    public static SingleObject getInstance(){
        return instance;
    }

    public void showMessage(){
        System.out.println("Hello World!");
    }
}
