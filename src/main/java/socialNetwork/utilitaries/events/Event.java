package socialNetwork.utilitaries.events;

import socialNetwork.domain.models.Friendship;

public interface Event <T> {

    public ChangeEventType getType();

    public T getData();

    public T getOldData();
}
