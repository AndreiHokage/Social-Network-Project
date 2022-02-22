package repository;

import config.ApplicationContext;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class UserRepositorySetterTest extends RepositoryAbstractTest<Long, User> {

    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");

    @Override
    public Long getMaximumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMaximumString = "select max(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMaximumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Long getMinimumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMinimumString = "select min(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMinimumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Long getExistingId() {
        return getMaximumId();
    }

    @Override
    public Long createNotExistingId() {
        return (getMaximumId() + 2);
    }

    @Override
    public List<User> getTestData() {
        return new ArrayList<>(Arrays.asList(

                new User("Baltazar","Baltazar","d1"),
                new User("Bradley","Bradley","d2"),
                new User("Frank","Frank","d3"),
                new User("Johnny","John","d4"),
                new User("Johnny","John","d5")

                ));
    }

    @Override
    public User createValidEntity() {
        return new User("Brutus","Brutus","d6");
    }

}
