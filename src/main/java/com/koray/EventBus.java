package com.koray;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer o) {
        observers.add(o);
    }

    public void publish(GameEvent event) {
        for (Observer o : observers) {
            o.onEvent(event);
        }
    }
}
