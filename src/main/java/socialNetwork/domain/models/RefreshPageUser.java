package socialNetwork.domain.models;

public class RefreshPageUser {
    private boolean refreshFriend;
    private boolean refreshFriendRequest;
    private boolean refreshChat;

    public RefreshPageUser(boolean refreshFriend, boolean refreshFriendRequest, boolean refreshChat) {
        this.refreshFriend = refreshFriend;
        this.refreshFriendRequest = refreshFriendRequest;
        this.refreshChat = refreshChat;
    }

    public boolean isRefreshFriend() {
        return refreshFriend;
    }

    public void setRefreshFriend(boolean refreshFriend) {
        this.refreshFriend = refreshFriend;
    }

    public boolean isRefreshFriendRequest() {
        return refreshFriendRequest;
    }

    public void setRefreshFriendRequest(boolean refreshFriendRequest) {
        this.refreshFriendRequest = refreshFriendRequest;
    }

    public boolean isRefreshChat() {
        return refreshChat;
    }

    public void setRefreshChat(boolean refreshChat) {
        this.refreshChat = refreshChat;
    }

    @Override
    public String toString() {
        return "RefreshPageUser{" +
                "refreshFriend=" + refreshFriend +
                ", refreshFriendRequest=" + refreshFriendRequest +
                ", refreshChat=" + refreshChat +
                '}';
    }
}
