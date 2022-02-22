package socialNetwork.utilitaries.events;

import socialNetwork.domain.models.EventPublic;

public class EventPublicChangeEvent implements Event<EventPublic,EventPublicChangeEventType>{
    private EventPublicChangeEventType type;
    private EventPublic oldData , data;

    public EventPublicChangeEvent(EventPublicChangeEventType type, EventPublic data){
        this.type = type;
        this.data = data;
    }

    public EventPublicChangeEvent(EventPublicChangeEventType type, EventPublic data, EventPublic oldData) {
        this.type = type;
        this.oldData = oldData;
        this.data = data;
    }

    @Override
    public EventPublicChangeEventType getType() {
        return type;
    }

    @Override
    public EventPublic getData() {
        return data;
    }

    @Override
    public EventPublic getOldData() {
        return oldData;
    }
}
