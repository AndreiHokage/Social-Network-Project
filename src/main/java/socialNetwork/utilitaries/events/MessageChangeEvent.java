package socialNetwork.utilitaries.events;

import socialNetwork.domain.models.Message;
import socialNetwork.domain.models.MessageDTO;
import socialNetwork.utilitaries.observer.Observer;

import java.util.List;

public class MessageChangeEvent implements Event<MessageDTO,MessageChangeEventType>{

    private MessageChangeEventType type;
    private MessageDTO data;
    private MessageDTO oldData;

    public MessageChangeEvent(MessageChangeEventType type, MessageDTO data) {
        this.type = type;
        this.data = data;
    }

    public MessageChangeEvent(MessageChangeEventType type, MessageDTO data, MessageDTO oldData) {
        this.type = type;
        this.data = data;
        this.oldData = oldData;
    }

    @Override
    public MessageChangeEventType getType() {
        return type;
    }

    @Override
    public MessageDTO getData() {
        return data;
    }

    @Override
    public MessageDTO getOldData() {
        return oldData;
    }
}
