package socialNetwork.domain.models;

import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDateTime;
import java.util.Objects;

public class FriendRequest extends Entity<UnorderedPair<Long,Long>> {

    private Long fromUserID;
    private Long toUserID;
    private InvitationStage invitationStage;
    private LocalDateTime dateRequest;

    public FriendRequest(Long fromUserID, Long toUserID, InvitationStage invitationStage, LocalDateTime localDateTime) {
        setIdEntity(new UnorderedPair<>(fromUserID,toUserID));
        this.fromUserID = fromUserID;
        this.toUserID = toUserID;
        this.invitationStage = invitationStage;
        this.dateRequest = localDateTime;
    }

    public Long getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(Long fromUserID) {
        this.fromUserID = fromUserID;
    }

    public Long getToUserID() {
        return toUserID;
    }

    public void setToUserID(Long toUserID) {
        this.toUserID = toUserID;
    }

    public InvitationStage getInvitationStage() {
        return invitationStage;
    }

    public void setInvitationStage(InvitationStage invitationStage) {
        this.invitationStage = invitationStage;
    }

    public LocalDateTime getDateRequest() {
        return dateRequest;
    }

    public void setDateRequest(LocalDateTime dateRequest) {
        this.dateRequest = dateRequest;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "fromUserID=" + fromUserID +
                ", toUserID=" + toUserID +
                ", invitationStage=" + invitationStage +
                ", localDateTime=" + dateRequest +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FriendRequest that = (FriendRequest) o;
        return Objects.equals(fromUserID, that.fromUserID) && Objects.equals(toUserID, that.toUserID) && invitationStage == that.invitationStage && Objects.equals(dateRequest, that.dateRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fromUserID, toUserID, invitationStage, dateRequest);
    }
}
