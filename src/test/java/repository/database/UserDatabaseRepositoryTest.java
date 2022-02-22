package repository.database;

import config.ApplicationContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import repository.UserRepositorySetterTest;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.repository.paging.PagingRepository;

import java.sql.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
 class UserDataBaseRepositoryTest extends UserRepositorySetterTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    UserDatabaseRepository testRepository;

    @Override
    public PagingRepository<Long, User> getRepository() {
        if(testRepository == null)
            testRepository = new UserDatabaseRepository(url, user, password);
        return testRepository;
    }

    @BeforeEach
    public void setUp(){
        UserDatabaseTableSetter.setUp(getTestData());
    }

    @AfterAll
    public void restoreDataBase(){
        UserDatabaseTableSetter.setUp(getTestData());
    }

}