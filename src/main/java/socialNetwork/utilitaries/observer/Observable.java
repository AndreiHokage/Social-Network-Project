package socialNetwork.utilitaries.observer;

import socialNetwork.utilitaries.events.Event;

import java.util.ArrayList;
import java.util.List;

public interface Observable<E extends Event>{
    void addObserver(Observer<E> observer);
    void removeObserver(Observer<E> observer);
    void notifyObservers(E event);
}
