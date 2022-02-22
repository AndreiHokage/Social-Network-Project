package socialNetwork.domain.models;

import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Friendship extends Entity<UnorderedPair<Long, Long>> {
    private LocalDateTime date;

    /**
     * constructor
     * @param idFirstUser - Long
     * @param idSecondUser - Long
     */
    public Friendship(Long idFirstUser, Long idSecondUser) {
        setIdEntity(new UnorderedPair<>(idFirstUser, idSecondUser));
    }
    /**
     * constructor
     * @param idFirstUser - Long
     * @param idSecondUser - Long
     * @param date - LocalDateTime - when the 2 users became friends
     */
    public Friendship(Long idFirstUser, Long idSecondUser, LocalDateTime date) {
        setIdEntity(new UnorderedPair<>(idFirstUser, idSecondUser));
        this.date = date;
    }

    /**
     * getter method for the date of friendship
     * @return - LocalDateTime
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * checks if the user is in the friendship
     * @param idUser - Long
     * @return - true if user is in the friendship, false otherwise
     */
    public boolean hasUser(Long idUser){
        return getId().left.compareTo(idUser) == 0 ||
                getId().right.compareTo(idUser) == 0;
    }

    /**
     * overrides equals method
     * @param o - Object instance
     * @return - boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(date, that.date);
    }

    /**
     * overrides hashCode method
     * @return - int
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), date);
    }

    /**
     * overrides toString method
     * @return - String
     */
    @Override
    public String toString() {
        return "Id of first user: " + getId().left +
                ", Id of second user: " + getId().right +
                ", Date: " + date.toString();
    }
}
