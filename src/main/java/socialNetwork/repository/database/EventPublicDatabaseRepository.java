package socialNetwork.repository.database;

import socialNetwork.domain.models.EventPublic;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.Paginator;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.events.Event;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventPublicDatabaseRepository implements PagingRepository<Long, EventPublic> {

    private String url;
    private String user;
    private String password;

    public EventPublicDatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Optional<EventPublic> find(Long idSearchedEntity) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement findStatement = connection.prepareStatement(
                    "select * from eventPublic where id = ?");
            ) {
            findStatement.setLong(1,idSearchedEntity);
            ResultSet resultSet = findStatement.executeQuery();
            if(resultSet.next() == false)
                return Optional.empty();
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
            EventPublic eventPublic = new EventPublic(name,description,date);
            eventPublic.setIdEntity(idSearchedEntity);
            return Optional.of(eventPublic);
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public List<EventPublic> getAll() {
        List<EventPublic> eventPublicList = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement findStatement = connection.prepareStatement(
                    "select id from eventPublic");
            ) {
            ResultSet resultSet = findStatement.executeQuery();
            while (resultSet.next()){
                Long id = resultSet.getLong("id");
                EventPublic eventPublic = find(id).get();
                eventPublicList.add(eventPublic);
            }
            return eventPublicList;
        } catch (SQLException throwables) {
            throw  new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Page<EventPublic> getAll(Pageable pageable) {
        Paginator<EventPublic> paginator = new Paginator<EventPublic>(pageable,getAll());
        return paginator.paginate();
    }

    @Override
    public Optional<EventPublic> save(EventPublic eventPublic) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement saveStatement = connection.prepareStatement(
                    "insert into eventPublic(name,description,date) values(?,?,?)");
            ) {
            saveStatement.setString(1,eventPublic.getName());
            saveStatement.setString(2,eventPublic.getDescription());
            saveStatement.setTimestamp(3, Timestamp.valueOf(eventPublic.getDate()));
            int rows = saveStatement.executeUpdate();
            if( rows == 0 )
                return  Optional.of(eventPublic);
            return  Optional.empty();
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Optional<EventPublic> remove(Long idEntity) {
        return Optional.empty();
    }

    @Override
    public Optional<EventPublic> update(EventPublic entityToUpdate) {
        return Optional.empty();
    }

}
