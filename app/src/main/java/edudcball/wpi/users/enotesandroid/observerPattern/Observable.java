package edudcball.wpi.users.enotesandroid.observerPattern;

import java.util.ArrayList;
import java.util.List;

public class Observable {

    private final static int OVERFLOW_CRITERIA = 10;

    private List<IObserver> observers = new ArrayList<>();

    public void subscribe(IObserver observer){
        observers.add(observer);
    }

    public void clearObservers(){
        observers.clear();
    }

    protected void notifyObservers(){
        for (IObserver observer: observers){
            observer.update();
        }
    }
}
