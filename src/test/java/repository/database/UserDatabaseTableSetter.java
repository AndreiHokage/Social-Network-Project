package repository.database;


import config.ApplicationContext;
import repository.UserRepositorySetterTest;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;

import java.sql.*;
import java.util.List;

public class UserDatabaseTableSetter{
    static String url = ApplicationContext.getProperty("network.database.url");
    static String user = ApplicationContext.getProperty("network.database.user");
    static String password = ApplicationContext.getProperty("network.database.password");

    public static void tearDown(){
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            var deleteStatement = connection.prepareStatement("DELETE FROM users");
            deleteStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void setUp(List<User> testData){
        tearDown();

        try(Connection connection = DriverManager.getConnection(url, user, password)) {

            String insertStatementString = "INSERT INTO users(first_name, last_name ,username) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);

            for(User user : testData){
                insertStatement.setString(1, user.getFirstName());
                insertStatement.setString(2, user.getLastName());
                insertStatement.setString(3, user.getUsername());
                insertStatement.executeUpdate();
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

}
