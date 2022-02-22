package socialNetwork.repository.database;

import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.InvitationStage;
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

public class FriendRequestDatabaseRepository implements
        PagingRepository<UnorderedPair<Long,Long>, FriendRequest> {

    private String url;
    private String user;
    private String password;
    private static final String FIND_FRIENDREQUEST_BY_ID_SQL_STRING =
            "SELECT * FROM friendrequests WHERE fromUserID=? " +
                    "AND toUserID=? OR toUserID=? AND fromUserID=?";

    public FriendRequestDatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Optional<FriendRequest> find(UnorderedPair<Long, Long> idSearchedEntity) {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            PreparedStatement findFriendshipStatement = createFindStatementForFriendRequestId(idSearchedEntity,connection);
            ResultSet resultSet = findFriendshipStatement.executeQuery();
            if(resultSet.next())
                return Optional.of(createFriendRequestFromResultSet(resultSet));
            return Optional.empty();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public List<FriendRequest> getAll() {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            List<FriendRequest> friendrequests = new ArrayList<>();
            PreparedStatement selectAllStatement = connection.prepareStatement("SELECT * FROM friendrequests");
            ResultSet resultSet = selectAllStatement.executeQuery();
            while(resultSet.next())
                friendrequests.add(createFriendRequestFromResultSet(resultSet));
            return friendrequests;
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Page<FriendRequest> getAll(Pageable pageable){
        Paginator<FriendRequest> paginator = new Paginator<FriendRequest>(pageable,getAll());
        return paginator.paginate();
    }

    @Override
    public Optional<FriendRequest> save(FriendRequest friendRequest) {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            PreparedStatement findStatement = createFindStatementForFriendRequestId(friendRequest.getId(), connection);
            ResultSet resultSet = findStatement.executeQuery();
            if(resultSet.next()) {
                return Optional.of(createFriendRequestFromResultSet(resultSet));
            }
            PreparedStatement insertStatement = createInsertStatementForFriendRequest(friendRequest, connection);
            insertStatement.executeUpdate();
            return Optional.empty();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Optional<FriendRequest> remove(UnorderedPair<Long, Long> idEntity) {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            PreparedStatement findFriendshipStatement = createFindStatementForFriendRequestId(idEntity, connection);
            ResultSet resultSet = findFriendshipStatement.executeQuery();
            if(resultSet.next()){
                PreparedStatement deleteStatement = createDeleteStatementForFriendRequest(idEntity, connection);
                deleteStatement.executeUpdate();
                return Optional.of(createFriendRequestFromResultSet(resultSet));
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Optional<FriendRequest> update(FriendRequest friendRequest) {
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            PreparedStatement findSql = createFindStatementForFriendRequestId(friendRequest.getId(), connection);
            ResultSet resultSet = findSql.executeQuery();
            if(resultSet.next()){
                var oldValue = createFriendRequestFromResultSet(resultSet);
                PreparedStatement updateSql = createUpdateStatementForFriendship(connection, friendRequest);
                updateSql.executeUpdate();
                return Optional.of(oldValue);
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    private PreparedStatement createFindStatementForFriendRequestId(UnorderedPair<Long, Long> id, Connection connection){
        try{
            return getPreparedStatement(id, connection, FIND_FRIENDREQUEST_BY_ID_SQL_STRING);
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    private PreparedStatement getPreparedStatement(UnorderedPair<Long, Long> id, Connection connection,
                                                   String findFriendshipByIdSqlString) throws SQLException {
        PreparedStatement findStatement = connection.prepareStatement(findFriendshipByIdSqlString);
        findStatement.setLong(1,id.left);
        findStatement.setLong(2,id.right);
        findStatement.setLong(3,id.left);
        findStatement.setLong(4,id.right);
        return findStatement;
    }

    private FriendRequest createFriendRequestFromResultSet(ResultSet resultSet){
        try{
            Long id1 = resultSet.getLong("fromUserID");
            Long id2 = resultSet.getLong("toUserID");
            LocalDateTime dateWhenFriendRequestWasCreated = resultSet.getTimestamp("dateRequest").toLocalDateTime();
            InvitationStage invitationStage = InvitationStage.valueOf(resultSet.getString("status"));
            FriendRequest friendRequest = new FriendRequest(id1,id2,invitationStage,dateWhenFriendRequestWasCreated);
            return friendRequest;
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    private PreparedStatement createInsertStatementForFriendRequest(FriendRequest friendRequest, Connection connection){
        try{
            String insertStringStatement = "INSERT INTO friendrequests(fromUserID, toUserID, status, dateRequest)" +
                                            " VALUES (?,?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStringStatement);
            insertStatement.setLong(1,friendRequest.getFromUserID());
            insertStatement.setLong(2,friendRequest.getToUserID());
            insertStatement.setString(3,String.valueOf(friendRequest.getInvitationStage()));
            insertStatement.setTimestamp(4,Timestamp.valueOf(friendRequest.getDateRequest()));
            return insertStatement;
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    private PreparedStatement createDeleteStatementForFriendRequest(UnorderedPair<Long, Long> id, Connection connection){
        try{
            String deleteStringStatement = "DELETE FROM friendrequests WHERE fromUserID=? AND toUserID=? OR" +
                    " toUserID=? AND fromUserID=?";
            return getPreparedStatement(id, connection, deleteStringStatement);
        }catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    private PreparedStatement createUpdateStatementForFriendship(Connection connection, FriendRequest newValue) {
        try{
            String updateSqlStr = "UPDATE friendrequests SET status=?,dateRequest=? " +
                    "WHERE fromUserID=? AND toUserID=? OR " +
                    "toUserID=? AND fromUserID=?";
            PreparedStatement updateSql = connection.prepareStatement(updateSqlStr);
            updateSql.setString(1, newValue.getInvitationStage().toString() );
            updateSql.setTimestamp(2,Timestamp.valueOf(newValue.getDateRequest()));
            updateSql.setLong(3, newValue.getFromUserID());
            updateSql.setLong(4, newValue.getToUserID());
            updateSql.setLong(5, newValue.getFromUserID());
            updateSql.setLong(6, newValue.getToUserID());
            return updateSql;
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }
}
