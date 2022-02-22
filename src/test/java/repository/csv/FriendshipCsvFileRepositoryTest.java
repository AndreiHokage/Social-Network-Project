package repository.csv;

import config.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import repository.FriendshipRepositorySetterTest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.repository.csv.FriendshipCsvFileRepository;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FriendshipCsvFileRepositoryTest extends FriendshipCsvFileRepositorySetterTest {
    String TEST_FILE_PATH = ApplicationContext.getProperty("repository.csv.friendships.test");
    FriendshipCsvFileRepository testRepository;

    @Override
    public PagingRepository<UnorderedPair<Long, Long>, Friendship> getRepository() {
        if(testRepository == null)
            testRepository = new FriendshipCsvFileRepository(TEST_FILE_PATH);
        return testRepository;
    }

    void tearDown(){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE_PATH))) {
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp(){
        tearDown();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE_PATH))) {
            for(Friendship friendship : getTestData()){
                String line = "" + friendship.getId().left + "," +
                        friendship.getId().right + "," +
                        friendship.getDate().toString();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
