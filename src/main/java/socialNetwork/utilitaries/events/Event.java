package socialNetwork.utilitaries.events;

import socialNetwork.domain.models.Friendship;

public interface Event <T , E> {

    public E getType();

    public T getData();

    public T getOldData();
}
