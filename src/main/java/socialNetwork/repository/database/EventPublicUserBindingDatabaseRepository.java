package socialNetwork.repository.database;

import socialNetwork.domain.models.DTOEventPublicUser;
import socialNetwork.domain.models.EventNotification;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.Paginator;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventPublicUserBindingDatabaseRepository
        implements PagingRepository<UnorderedPair<Long,Long>,DTOEventPublicUser> {
    private String url;
    private String user;
    private String password;

    public EventPublicUserBindingDatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Optional<DTOEventPublicUser> find(UnorderedPair<Long,Long> idSearchedEntity) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement findStatement = connection.prepareStatement(
                    "select receiveNotification from eventPublicuser " +
                            "where idUser = ? and idEventPublic = ?");
            ) {
            Long idUser = idSearchedEntity.left;
            Long idEventPublic = idSearchedEntity.right;
            findStatement.setLong(1,idUser);
            findStatement.setLong(2,idEventPublic);
            ResultSet resultSet = findStatement.executeQuery();
            if( resultSet.next() == false )
                return Optional.empty();
            EventNotification receiveNotification = EventNotification.valueOf(resultSet.getString("receiveNotification"));
            DTOEventPublicUser dtoEventPublicUser = new DTOEventPublicUser(idUser,idEventPublic,receiveNotification);
            return Optional.of(dtoEventPublicUser);
        } catch (SQLException throwables) {
            throw  new DatabaseException(throwables.getMessage());
        }

    }

    @Override
    public List<DTOEventPublicUser> getAll() {
        List<DTOEventPublicUser> dtoEventPublicUserList = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement findStatement = connection.prepareStatement(
                    "select idUser,idEventPublic from eventPublicUser");
            ) {
            ResultSet resultSet = findStatement.executeQuery();
            while(resultSet.next()){
                Long idUser = resultSet.getLong("idUser");
                Long idEventPublic = resultSet.getLong("idEventPublic");
                DTOEventPublicUser dtoEventPublicUser = find(new UnorderedPair<Long,Long>(idUser,idEventPublic)).get();
                dtoEventPublicUserList.add(dtoEventPublicUser);
            }
            return  dtoEventPublicUserList;
        } catch (SQLException throwables) {
            throw  new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Page<DTOEventPublicUser> getAll(Pageable pageable) {
        Paginator<DTOEventPublicUser> paginator = new Paginator<>(pageable,getAll());
        return paginator.paginate();
    }

    @Override
    public Optional<DTOEventPublicUser> save(DTOEventPublicUser entityToSave) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement saveStatement = connection.prepareStatement(
                    "insert into eventPublicUser(idUser,idEventPublic,receiveNotification) " +
                            "values (?,?,?) ");
            ) {
            saveStatement.setLong(1,entityToSave.getIdUser());
            saveStatement.setLong(2,entityToSave.getIdEventPublic());
            saveStatement.setString(3 , String.valueOf(entityToSave.getReceivedNotification()) );
            int rows = saveStatement.executeUpdate();
            if( rows == 0 )
                return Optional.of(entityToSave);
            return Optional.empty();
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Optional<DTOEventPublicUser> remove(UnorderedPair<Long,Long> idEntityRemove) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement removeStatement = connection.prepareStatement(
                    "delete from eventPublicUser where idUser = ? and idEventPublic = ?");
            ) {
            Optional<DTOEventPublicUser> findUserEvent = find(idEntityRemove);
            if(findUserEvent.isEmpty())
                return Optional.empty();
            removeStatement.setLong(1,idEntityRemove.left);
            removeStatement.setLong(2,idEntityRemove.right);
            removeStatement.executeUpdate();
            return findUserEvent;
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Optional<DTOEventPublicUser> update(DTOEventPublicUser entityToUpdate) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement updateStatement = connection.prepareStatement(
                    "update eventPublicUser set receiveNotification = ? " +
                            "where idUser = ? and idEventPublic = ?");
            ) {
            Optional<DTOEventPublicUser> findDTOEventUser = find(new UnorderedPair<Long,Long>
                    (entityToUpdate.getIdUser(), entityToUpdate.getIdEventPublic()));
            if( findDTOEventUser.isEmpty() )
                return Optional.empty();
            updateStatement.setString(1,String.valueOf(entityToUpdate.getReceivedNotification()));
            updateStatement.setLong(2,entityToUpdate.getIdUser());
            updateStatement.setLong(3,entityToUpdate.getIdEventPublic());
            updateStatement.executeUpdate();
            return findDTOEventUser;
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

}
