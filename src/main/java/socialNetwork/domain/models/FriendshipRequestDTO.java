package socialNetwork.domain.models;

import socialNetwork.guiControllers.FriendshipStatusController;

import java.time.LocalDateTime;
import java.util.Objects;

public class FriendshipRequestDTO {

    private User userThatSendsRequest;
    private User userThatReceivesRequest;
    private LocalDateTime localDateTime;
    private InvitationStage invitationStage;

    public FriendshipRequestDTO(User userThatSendsRequest, User userThatReceivesRequest, LocalDateTime localDateTime, InvitationStage invitationStage) {
        this.userThatSendsRequest = userThatSendsRequest;
        this.userThatReceivesRequest = userThatReceivesRequest;
        this.localDateTime = localDateTime;
        this.invitationStage = invitationStage;
    }

    public User getUserThatSendsRequest() {
        return userThatSendsRequest;
    }

    public void setUserThatSendsRequest(User userThatSendsRequest) {
        this.userThatSendsRequest = userThatSendsRequest;
    }

    public User getUserThatReceivesRequest() {
        return userThatReceivesRequest;
    }

    public void setUserThatReceivesRequest(User userThatReceivesRequest) {
        this.userThatReceivesRequest = userThatReceivesRequest;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public InvitationStage getInvitationStage() {
        return invitationStage;
    }

    public void setInvitationStage(InvitationStage invitationStage) {
        this.invitationStage = invitationStage;
    }

    @Override
    public String toString() {
        return "FriendshipRequestDTO{" +
                "userThatSendsRequest=" + userThatSendsRequest +
                ", userThatReceivesRequest=" + userThatReceivesRequest +
                ", localDateTime=" + localDateTime +
                ", invitationStage=" + invitationStage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendshipRequestDTO that = (FriendshipRequestDTO) o;
        return Objects.equals(userThatSendsRequest, that.userThatSendsRequest) && Objects.equals(userThatReceivesRequest, that.userThatReceivesRequest) && Objects.equals(localDateTime, that.localDateTime) && invitationStage == that.invitationStage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userThatSendsRequest, userThatReceivesRequest, localDateTime, invitationStage);
    }
}
