package repository.csv;

import repository.RepositoryAbstractTest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public abstract class FriendshipCsvFileRepositorySetterTest extends
        RepositoryAbstractTest<UnorderedPair<Long, Long>, Friendship> {

    @Override
    public UnorderedPair<Long, Long> getMinimumId(){
        return new UnorderedPair<>(1L, 2L);
    }

    @Override
    public UnorderedPair<Long, Long> getMaximumId(){
        return new UnorderedPair<>(2L, 3L);
    }

    @Override
    public Friendship createValidEntity() {
        return new Friendship(1234L,5678L,
                LocalDateTime.of(2021,10,20,10,30));
    }

    @Override
    public UnorderedPair<Long, Long> createNotExistingId() {
        return new UnorderedPair<>(5555L,6666L);
    }

    @Override
    public UnorderedPair<Long, Long> getExistingId() {
        return new UnorderedPair<>(2L,1L);
    }

    @Override
    public List<Friendship> getTestData() {
        return Arrays.asList(
                new Friendship(1L,2L, LocalDateTime.of(2021,10,20,10,30)),
                new Friendship(1L,3L, LocalDateTime.of(2021,10,20,10,30)),
                new Friendship(2L,3L, LocalDateTime.of(2021,10,20,10,30))
        );
    }
}
