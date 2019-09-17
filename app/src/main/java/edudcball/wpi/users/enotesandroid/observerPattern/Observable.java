package edudcball.wpi.users.enotesandroid.observerPattern;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Observable {

    private final static int OVERFLOW_CRITERIA = 3;

    private List<IObserver> observers = new ArrayList<>();

    public void subscribe(IObserver observer){
        if(observers.contains(observer)) return;
        if(observers.size() > OVERFLOW_CRITERIA) Log.d("MYAPP", "Observers overflow!");
        observers.add(observer);
    }

    public void unSubscribe(IObserver observer){
        observers.remove(observer);
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
