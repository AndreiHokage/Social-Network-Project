package socialNetwork.utilitaries.events;

import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;

public class FriendRequestChangeEvent implements Event<FriendRequest,FriendRequestChangeEventType> {

    private FriendRequestChangeEventType type;
    private FriendRequest data, oldData;

    public FriendRequestChangeEvent(FriendRequestChangeEventType type, FriendRequest data) {
        this.type = type;
        this.data = data;
    }

    public FriendRequestChangeEvent(FriendRequestChangeEventType type,
                                    FriendRequest data, FriendRequest oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    @Override
    public FriendRequestChangeEventType getType() {
        return type;
    }

    @Override
    public FriendRequest getData() {
        return data;
    }

    @Override
    public FriendRequest getOldData() {
        return oldData;
    }
}
