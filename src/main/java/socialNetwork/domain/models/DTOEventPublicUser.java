package socialNetwork.domain.models;

import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDateTime;
import java.util.Objects;

public class DTOEventPublicUser extends Entity<UnorderedPair<Long,Long>>{
    private Long idUser;
    private Long idEventPublic;
    private EventNotification receivedNotification;

    public DTOEventPublicUser(Long idUser, Long idEventPublic,EventNotification receivedNotification) {
        this.idUser = idUser;
        this.idEventPublic = idEventPublic;
        this.receivedNotification = receivedNotification;
    }

    public DTOEventPublicUser(Long idUser, Long idEventPublic){
        this.idUser = idUser;
        this.idEventPublic = idEventPublic;
        this.receivedNotification = EventNotification.APPROVE;
    }

    public EventNotification getReceivedNotification() {
        return receivedNotification;
    }

    public void setReceivedNotification(EventNotification receivedNotification) {
        this.receivedNotification = receivedNotification;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Long getIdEventPublic() {
        return idEventPublic;
    }

    public void setIdEventPublic(Long idEventPublic) {
        this.idEventPublic = idEventPublic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DTOEventPublicUser that = (DTOEventPublicUser) o;
        return Objects.equals(idUser, that.idUser) && Objects.equals(idEventPublic, that.idEventPublic)  && Objects.equals(receivedNotification, that.receivedNotification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idUser, idEventPublic, receivedNotification);
    }

    @Override
    public String toString() {
        return "DTOEventPublicUser{" +
                "idUser=" + idUser +
                ", idEventPublic=" + idEventPublic +
                ", receivedNotification=" + receivedNotification +
                '}';
    }
}
