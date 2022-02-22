package repository;

import socialNetwork.domain.models.Entity;
import socialNetwork.exceptions.InvalidEntityException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import socialNetwork.repository.paging.PagingRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class RepositoryAbstractTest<ID, E extends Entity<ID>> {
    public abstract E createValidEntity();
    public abstract ID createNotExistingId();
    public abstract ID getExistingId();
    public abstract ID getMinimumId();
    public abstract ID getMaximumId();
    public abstract PagingRepository<ID, E> getRepository();
    public abstract List<E> getTestData();

    @Test
    void findReturnsEmptyOptional(){
        var optionalEntity = getRepository().find(createNotExistingId());
        Assertions.assertTrue(optionalEntity.isEmpty());
    }

    @Test
    void findReturnsNotEmpty(){
        var id = getExistingId();
        var foundEntityById = getRepository().find(id);
        Assertions.assertTrue(foundEntityById.isPresent());
        var foundEntity = foundEntityById.get();
        Predicate<E> equalEntities = e -> e.equals(foundEntity);
        List<E> rez= getTestData();
        boolean isEntityInInformationForTesting = getTestData()
                .stream()
                .anyMatch(equalEntities);
        Assertions.assertTrue(isEntityInInformationForTesting);
    }

    @Test
    void saveAddsTheEntity(){
        E entityIsValid = createValidEntity();
        Optional<E> optionalEntity = getRepository().save(entityIsValid);
        Assertions.assertTrue(optionalEntity.isEmpty());
    }


    @Test
    void testGetAll(){
        List<E> expectedEntities = getTestData();
        List<E> actualEntities = getRepository().getAll();
        Assertions.assertEquals(expectedEntities.size(), actualEntities.size());
        for(E entity: actualEntities)
            Assertions.assertTrue(expectedEntities.contains(entity));
    }

    @Test
    void updateReturnsEmptyOptional(){
        var updatedEntity = createValidEntity();
        updatedEntity.setIdEntity(createNotExistingId());
        var optionalEntity = getRepository().update(updatedEntity);
        Assertions.assertTrue(optionalEntity.isEmpty());
    }

    @Test
    void updateReturnsOldValue(){
        var newEntity = createValidEntity();
        List<E> testInformation = getTestData();
        newEntity.setIdEntity(getMinimumId());
        var oldValue = testInformation.get(0);
        var entityOptional = getRepository().update(newEntity);
        Assertions.assertTrue(entityOptional.isPresent());
        Assertions.assertEquals(oldValue, entityOptional.get());
        var updatedEntityFromRepo = getRepository().find(newEntity.getId());
        Assertions.assertTrue(updatedEntityFromRepo.isPresent());
        Assertions.assertEquals(newEntity, updatedEntityFromRepo.get());
    }


    @Test
    void removeDeletesExistingEntity(){
        var removedEntityOptional = getRepository().remove(getExistingId());
        Assertions.assertTrue(removedEntityOptional.isPresent());
        var removedEntity = removedEntityOptional.get();
        var informationTest = getTestData();
        Predicate<E> isEntityEqualTo = e -> e.equals(removedEntity);
        var isRemovedEntityInInformationTest = informationTest
                .stream()
                .anyMatch(isEntityEqualTo);
        Assertions.assertTrue(isRemovedEntityInInformationTest);
    }

    @Test
    void removeNoDeletionOfEntity(){
        var removedEntityOptional = getRepository().remove(createNotExistingId());
        Assertions.assertTrue(removedEntityOptional.isEmpty());
    }
}
