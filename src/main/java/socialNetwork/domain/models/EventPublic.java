package socialNetwork.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class EventPublic extends Entity<Long>{
    private String name;
    private String description;
    private LocalDateTime date;

    public EventPublic(String name, String description, LocalDateTime date) {
        this.name = name;
        this.description = description;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "EventUser{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EventPublic eventUser = (EventPublic) o;
        return Objects.equals(name, eventUser.name) && Objects.equals(description, eventUser.description) && Objects.equals(date, eventUser.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, date);
    }
}
