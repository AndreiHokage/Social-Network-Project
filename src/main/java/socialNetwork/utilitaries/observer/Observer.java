package socialNetwork.utilitaries.observer;

import socialNetwork.utilitaries.events.Event;

public interface Observer<E extends Event> {
    void update(E event);
}
