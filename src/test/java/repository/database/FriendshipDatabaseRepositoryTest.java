package repository.database;

import config.ApplicationContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import repository.FriendshipRepositorySetterTest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendshipDataBaseRepositoryTest extends FriendshipRepositorySetterTest {
    FriendshipDatabaseRepository testRepository;
    private String url = ApplicationContext.getProperty("network.database.url");
    private String user = ApplicationContext.getProperty("network.database.user");
    private String password = ApplicationContext.getProperty("network.database.password");

    @Override
    public PagingRepository<UnorderedPair<Long, Long>, Friendship> getRepository() {
        if(testRepository == null)
            testRepository = new FriendshipDatabaseRepository(url, user, password);
        return testRepository;
    }

    @BeforeEach
    public void setUp(){
        var userTest = new UserDataBaseRepositoryTest();
        userTest.setUp();
        FriendshipDatabaseTableSetter.setUp(getTestData());
    }

    @AfterAll
    void restoreDataBase(){
        setUp();
    }
}