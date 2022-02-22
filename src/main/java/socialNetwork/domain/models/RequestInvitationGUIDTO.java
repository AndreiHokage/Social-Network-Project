package socialNetwork.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class RequestInvitationGUIDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDateTime localDateTime;
    private InvitationStage invitationStage;

    public RequestInvitationGUIDTO(Long id,String firstName, String lastName, LocalDateTime localDateTime, InvitationStage invitationStage) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.localDateTime = localDateTime;
        this.invitationStage = invitationStage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
        return "RequestInvitationGUIDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", localDateTime=" + localDateTime +
                ", invitationStage=" + invitationStage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestInvitationGUIDTO that = (RequestInvitationGUIDTO) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(localDateTime, that.localDateTime) && invitationStage == that.invitationStage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, localDateTime, invitationStage);
    }
}
