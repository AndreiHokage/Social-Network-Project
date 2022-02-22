package socialNetwork.repository.database;

import socialNetwork.domain.models.Autentification;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.Paginator;
import socialNetwork.repository.paging.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutentificationDatabaseRepository implements
        PagingRepository<String, Autentification> {

    private String url;
    private String user;
    private String password;

    public AutentificationDatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Optional<Autentification> find(String idSearchedEntity) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement findAutentification = connection.prepareStatement(
                    "select * from autentifications where username = ?"
            )) {
            findAutentification.setString(1,idSearchedEntity);
            ResultSet findSpecificAuthentification = findAutentification.executeQuery();
            if(findSpecificAuthentification.next() == false)
                return Optional.empty();

            String username = findSpecificAuthentification.getString("username");
            String passwordText = findSpecificAuthentification.getString("passwordText");
            Autentification autentification = new Autentification(username,passwordText);
            autentification.setIdEntity(username);
            return Optional.of(autentification);
        } catch (SQLException throwables) {
           throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public List<Autentification> getAll() {
        List<Autentification> autentificationList = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement allAut = connection.prepareStatement(
                    "select * from autentifications"
            )) {
            ResultSet getAllAut = allAut.executeQuery();
            while (getAllAut.next()){
                String username = getAllAut.getString("username");
                String passwordText = getAllAut.getString("passwordText");
                Autentification autentification = new Autentification(username,passwordText);
                autentification.setIdEntity(username);
                autentificationList.add(autentification);
            }
            return autentificationList;
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Page<Autentification> getAll(Pageable pageable){
        Paginator<Autentification> paginator = new Paginator<Autentification>(pageable,getAll());
        return paginator.paginate();
    }

    @Override
    public Optional<Autentification> save(Autentification autentification) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement findUsernameInTable = connection.prepareStatement(
                "select username from autentifications where username = ?"
        )) {
            findUsernameInTable.setString(1,autentification.getUsername());
            ResultSet allAuthentificationsWithSpecificUsername = findUsernameInTable.executeQuery();
            if(allAuthentificationsWithSpecificUsername.next())
                return Optional.of(autentification);
            PreparedStatement saveStatement = connection.prepareStatement(
                    "insert into autentifications(username,passwordText) values (?,?)");
            saveStatement.setString(1,autentification.getUsername());
            saveStatement.setString(2,autentification.getPassword());
            saveStatement.executeUpdate();
            return Optional.empty();

        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Optional<Autentification> remove(String idEntity) {
        return Optional.empty();
    }

    @Override
    public Optional<Autentification> update(Autentification entityToUpdate) {
        try (Connection connection = DriverManager.getConnection(url,user,password);
             PreparedStatement preparedStatement = connection.prepareStatement(
                    "update autentifications set passwordText = ? where username = ? ")) {
            preparedStatement.setString(1,entityToUpdate.getPassword());
            preparedStatement.setString(2,entityToUpdate.getUsername());
            preparedStatement.executeUpdate();
            return Optional.empty();
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }
}
