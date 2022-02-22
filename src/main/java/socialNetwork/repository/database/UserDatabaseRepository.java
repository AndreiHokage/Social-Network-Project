package socialNetwork.repository.database;

import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.Paginator;
import socialNetwork.repository.paging.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * database repository for entity User
 */
public class UserDatabaseRepository implements PagingRepository<Long, User> {
    private String url;
    private String user;
    private String password;

    /**
     * creates a new database with the given connection data
     * @param url - url of database
     * @param user - user of the server
     * @param password - master password of server
     */
    public UserDatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Optional<User> find(Long idSearchedEntity) {
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            PreparedStatement findSql = createFindUserIdStatement(idSearchedEntity, connection);
            ResultSet resultSet = findSql.executeQuery();
            if(resultSet.next())
                return Optional.of(createUserFromResultSet(resultSet));
            return Optional.empty();
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public List<User> getAll() {
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            List<User> users = new ArrayList<>();
            PreparedStatement selectSql = connection.prepareStatement("SELECT * FROM users");
            ResultSet resultSet = selectSql.executeQuery();
            while(resultSet.next())
                users.add(createUserFromResultSet(resultSet));
            return users;
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Page<User> getAll(Pageable pageable) {
        Paginator<User> paginator = new Paginator<User>(pageable,getAll());
        return paginator.paginate();
    }


    @Override
    public Optional<User> save(User entityToSave) {
        try(Connection connection = DriverManager.getConnection(url, user, password)){

            PreparedStatement findSql = createFindUserUsernameStatement(entityToSave.getUsername(), connection);
            ResultSet resultSet = findSql.executeQuery();
            if(resultSet.next())
                return Optional.of(createUserFromResultSet(resultSet));
            else{
                PreparedStatement insertSql = createInsertStatementForUser(entityToSave, connection);
                insertSql.executeUpdate();
                return Optional.empty();
            }
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Optional<User> remove(Long idEntity) {
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            PreparedStatement findSql = createFindUserIdStatement(idEntity, connection);
            ResultSet resultSet = findSql.executeQuery();
            if(resultSet.next()) {
                User oldValue = createUserFromResultSet(resultSet);
                PreparedStatement removeSql = createDeleteStatementForUser(idEntity, connection);
                removeSql.executeUpdate();
                return Optional.of(oldValue);
            }
            return Optional.empty();
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Optional<User> update(User entityToUpdate) {
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            PreparedStatement findSql = createFindUserIdStatement(entityToUpdate.getId(), connection);
            ResultSet resultSet = findSql.executeQuery();
            if(resultSet.next()) {
                User oldValue = createUserFromResultSet(resultSet);
                PreparedStatement updateSql = createUpdateStatementForUser(entityToUpdate, connection);
                updateSql.executeUpdate();
                return Optional.of(oldValue);
            }
            return Optional.empty();

        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * creates a query for id, first name and last name of a user
     * @param id - id of user that will be selected
     * @param connection - Connection to database
     * @return - a PreparedStatement object representing the query for selecting the user from db
     */
    private PreparedStatement createFindUserIdStatement(Long id, Connection connection){
        try{
            String findSqlString = "SELECT id, first_name, last_name, username FROM users WHERE id = ?";
            PreparedStatement findSql = connection.prepareStatement(findSqlString);
            findSql.setLong(1, id);
            return findSql;
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    private PreparedStatement createFindUserUsernameStatement(String username, Connection connection){
        try{
            String findSqlString = "SELECT id, first_name, last_name, username FROM users WHERE username = ?";
            PreparedStatement findSql = connection.prepareStatement(findSqlString);
            findSql.setString(1, username);
            return findSql;
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * creates a PreparedStatement that removes the user with the given id
     * @param id - id of user to be removed
     * @param connection - Connection object to database
     * @return PreparedStatement - query that removes the user with given id
     */
    private PreparedStatement createDeleteStatementForUser(Long id, Connection connection){
        try{
            String deleteSqlString = "DELETE FROM users WHERE id=?";
            PreparedStatement deleteSql = connection.prepareStatement(deleteSqlString);
            deleteSql.setLong(1, id);
            return deleteSql;
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * creates a PreparedStatement that updates the user with the same is as newUser
     * @param newUser - updated User that has a specific id
     * @param connection - Connection object to database
     * @return PreparedStatement - query that updates user
     */
    private PreparedStatement createUpdateStatementForUser(User newUser, Connection connection){
        try{
            String updateSqlString = "UPDATE users SET first_name=?, last_name=?, username=? WHERE id=?";
            PreparedStatement updateSql = connection.prepareStatement(updateSqlString);
            updateSql.setString(1, newUser.getFirstName());
            updateSql.setString(2, newUser.getLastName());
            updateSql.setString(3, newUser.getUsername());
            updateSql.setLong(4, newUser.getId());
            return updateSql;
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * creates an insert statement for the given user on the given connection
     * @param user - user to be inserted
     * @param connection - Connection object to database
     * @return PreparedStatement - query that inserts a new user
     */
    private PreparedStatement createInsertStatementForUser(User user, Connection connection){
        try{
            String insertSqlString = "INSERT INTO users(first_name, last_name, username) values (?,?,?)";
            PreparedStatement insertSql = connection.prepareStatement(insertSqlString);
            insertSql.setString(1, user.getFirstName());
            insertSql.setString(2, user.getLastName());
            insertSql.setString(3,user.getUsername());
            return insertSql;
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * creates a user with the data of the first row from the given result set
     * @param resultSet - contains id,firstName,lastName of user and must point to valid row
     * @return - User with the given data
     */
    private User createUserFromResultSet(ResultSet resultSet){
        try{
            Long id = resultSet.getLong("id");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String username = resultSet.getString("username");
            return new User(id, firstName, lastName ,username);
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }
}
