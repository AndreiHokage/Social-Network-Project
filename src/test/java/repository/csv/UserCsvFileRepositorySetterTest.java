package repository.csv;

import repository.RepositoryAbstractTest;
import socialNetwork.domain.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class UserCsvFileRepositorySetterTest extends RepositoryAbstractTest<Long, User> {

    @Override
    public Long getMinimumId(){return 1L;}

    @Override
    public Long getMaximumId(){return 5678L;}

    @Override
    public Long getExistingId() {
        return 1L;
    }

    @Override
    public Long createNotExistingId() {
        return 4L;
    }

    @Override
    public List<User> getTestData() {
        return new ArrayList<>(Arrays.asList(
                new User(1L,"Baltazar","Baltazar","g1"),
                new User(2L, "Bradley","Bradley","g2"),
                new User(3L,"Frank","Frank","g4"),
                new User(1234L,"Johnny","John","g5"),
                new User(5678L, "Johnny","John","g6")
        ));
    }

    @Override
    public User createValidEntity() {
        return new User(10L,"Brutus","Brutus","g10");
    }

}
