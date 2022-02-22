package repository;

import config.ApplicationContext;
import socialNetwork.domain.models.Friendship;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public abstract class FriendshipRepositorySetterTest extends
        RepositoryAbstractTest<UnorderedPair<Long, Long>, Friendship> {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");

    @Override
    public Friendship createValidEntity() {
        return new Friendship(getMinimumId().left, getMinimumId().left + 3,
                LocalDateTime.of(2021,10,20,10,30));
    }

    @Override
    public UnorderedPair<Long, Long> getMinimumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMinimumString = "select min(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMinimumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            long minimumId = resultSet.getLong(1);
            return new UnorderedPair<>(minimumId, minimumId+ 1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public UnorderedPair<Long, Long> getMaximumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMaximumString = "select max(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMaximumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            long maximumId = resultSet.getLong(1);
            return new UnorderedPair<>(maximumId - 1, maximumId);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public UnorderedPair<Long, Long> createNotExistingId() {
        return new UnorderedPair<>(getMaximumId().right + 5, getMaximumId().right + 8);
    }

    @Override
    public UnorderedPair<Long, Long> getExistingId() {
        return getMinimumId();
    }

    @Override
    public List<Friendship> getTestData() {
        return Arrays.asList(
                new Friendship(getMinimumId().left,getMinimumId().left + 1, LocalDateTime.of(2021,10,20,10,30)),
                new Friendship(getMinimumId().left,getMinimumId().left + 2, LocalDateTime.of(2021,10,20,10,30)),
                new Friendship(getMinimumId().left + 1,getMinimumId().left + 2, LocalDateTime.of(2021,10,20,10,30))
        );
    }
}
