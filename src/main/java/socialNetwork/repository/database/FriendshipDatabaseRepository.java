package socialNetwork.repository.database;

import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.Paginator;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendshipDatabaseRepository
        implements PagingRepository<UnorderedPair<Long, Long>, Friendship> {
    private String url;
    private String user;
    private String password;
    private static final String FIND_FRIENDSHIP_BY_ID_SQL_STRING =
            "SELECT * FROM friendships WHERE id_first_user=? " +
                    "AND id_second_user=? OR id_second_user=? AND id_first_user=?";

    /**
     * creates a new database with the given connection data
     * @param url - url of database
     * @param user - user of connection
     * @param password - master password for server
     */
    public FriendshipDatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Optional<Friendship> find(UnorderedPair<Long, Long> idSearchedEntity) {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            PreparedStatement findFriendshipStatement = createFindStatementForFriendshipId(idSearchedEntity,connection);
            ResultSet resultSet = findFriendshipStatement.executeQuery();
            if(resultSet.next())
                return Optional.of(createFriendshipFromResultSet(resultSet));
            return Optional.empty();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public List<Friendship> getAll() {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            List<Friendship> friendships = new ArrayList<>();
            PreparedStatement selectAllStatement = connection.prepareStatement("SELECT * FROM friendships");
            ResultSet resultSet = selectAllStatement.executeQuery();
            while(resultSet.next())
                friendships.add(createFriendshipFromResultSet(resultSet));
            return friendships;
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Page<Friendship> getAll(Pageable pageable) {
        Paginator<Friendship> paginator = new Paginator<Friendship>(pageable,getAll());
        return paginator.paginate();
    }

    @Override
    public Optional<Friendship> save(Friendship friendship) {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            PreparedStatement findStatement = createFindStatementForFriendshipId(friendship.getId(), connection);
            ResultSet resultSet = findStatement.executeQuery();
            if(resultSet.next()) {
                return Optional.of(createFriendshipFromResultSet(resultSet));
            }
            PreparedStatement insertStatement = createInsertStatementForFriendship(friendship, connection);
            insertStatement.executeUpdate();
            return Optional.empty();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Optional<Friendship> remove(UnorderedPair<Long, Long> idEntity) {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            PreparedStatement findFriendshipStatement = createFindStatementForFriendshipId(idEntity, connection);
            ResultSet resultSet = findFriendshipStatement.executeQuery();
            if(resultSet.next()){
                PreparedStatement deleteStatement = createDeleteStatementForFriendship(idEntity, connection);
                deleteStatement.executeUpdate();
                return Optional.of(createFriendshipFromResultSet(resultSet));
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Optional<Friendship> update(Friendship newValue) {
        return Optional.empty();
    }

    /**
     * create a new friendship object with the data of the first row of resultSet
     * @param resultSet - contains friendship in format (id_first_user, id_second_user)
     * @return - a Friendship with the given data
     */
    private Friendship createFriendshipFromResultSet(ResultSet resultSet){
        try{
            Long id1 = resultSet.getLong("id_first_user");
            Long id2 = resultSet.getLong("id_second_user");
            LocalDateTime dateWhenFriendshipWasCreated = resultSet.getTimestamp("date").toLocalDateTime();
            Friendship friendship = new Friendship(id1, id2, dateWhenFriendshipWasCreated);
            return  friendship;
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * creates a query for inserting a friendship
     * @param friendship - friendship to be added
     * @param connection - Connection to database
     * @return - PreparedStatement object representing the query for inserting the friendship in database
     */
    private PreparedStatement createInsertStatementForFriendship(Friendship friendship, Connection connection){
        try{
            String insertStringStatement = "INSERT INTO friendships(id_first_user, id_second_user, date) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStringStatement);
            insertStatement.setLong(1,friendship.getId().left);
            insertStatement.setLong(2,friendship.getId().right);
            insertStatement.setTimestamp(3,Timestamp.valueOf(friendship.getDate()));
            return insertStatement;
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * creates a query for removing a friendship
     * @param id - id of friendship to be added
     * @param connection - Connection to database
     * @return - PreparedStatement object - query for removing friendship from database
     */
    private PreparedStatement createDeleteStatementForFriendship(UnorderedPair<Long, Long> id, Connection connection){
        try{
            String deleteStringStatement = "DELETE FROM friendships WHERE id_first_user=? AND id_second_user=? OR" +
                    " id_second_user=? AND id_first_user=?";
            return getPreparedStatement(id, connection, deleteStringStatement);
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * creates query for selecting a friendship
     * @param id - id of friendship to be selected
     * @param connection - Connection to database
     * @return - PreparedStatement
     */
    private PreparedStatement createFindStatementForFriendshipId(UnorderedPair<Long, Long> id, Connection connection){
        try{
            return getPreparedStatement(id, connection, FIND_FRIENDSHIP_BY_ID_SQL_STRING);
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    /**
     * gets a PreparedStatement - creates query for finding a friendship
     * @param id - id of friendship to be found
     * @param connection - Connection to database
     * @param findFriendshipByIdSqlString - String
     * @return PreparedStatement
     */
    private PreparedStatement getPreparedStatement(UnorderedPair<Long, Long> id, Connection connection,
                                                   String findFriendshipByIdSqlString) throws SQLException {
        PreparedStatement findStatement = connection.prepareStatement(findFriendshipByIdSqlString);
        findStatement.setLong(1,id.left);
        findStatement.setLong(2,id.right);
        findStatement.setLong(3,id.left);
        findStatement.setLong(4,id.right);
        return findStatement;
    }
}
