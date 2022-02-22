package socialNetwork.repository.memory;

import socialNetwork.domain.models.Entity;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.Paginator;
import socialNetwork.repository.paging.PagingRepository;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepository<ID, E extends Entity<ID>> implements PagingRepository<ID, E> {
    private Map<ID, E> entitiesMap = new HashMap<ID, E>();

    @Override
    public Optional<E> find(ID idSearchedEntity) {
        if(entitiesMap.containsKey(idSearchedEntity))
            return Optional.of(entitiesMap.get(idSearchedEntity));
        return Optional.empty();
    }

    @Override
    public List<E> getAll() {
        return entitiesMap.values().stream().toList();
    }

//    @Override
//    public Optional<E> save(E entityToSave) {
//        if(entitiesMap.containsKey(entityToSave.getId())){
//            E valueAtKey = entitiesMap.get(entityToSave.getId());
//            return Optional.of(valueAtKey);
//        }
//        entitiesMap.put(entityToSave.getId(), entityToSave);
//        return Optional.empty();
//    }

    @Override
    public Optional<E> save(E entityToSave) {
        entitiesMap.put(entityToSave.getId(), entityToSave);
        return Optional.empty();
    }

    @Override
    public Optional<E> remove(ID idEntity) {
        if(entitiesMap.containsKey(idEntity)){
            E removedValue = entitiesMap.remove(idEntity);
            return Optional.of(removedValue);
        }
        return Optional.empty();
    }

    @Override
    public Optional<E> update(E updatedEntity) {
        if(entitiesMap.containsKey(updatedEntity.getId())){
            E oldValue = entitiesMap.put(updatedEntity.getId(), updatedEntity);
            return Optional.of(oldValue);
        }
        return Optional.empty();
    }

    /**
     * removes all local data
     */
    public void removeAllLocalData() {
        entitiesMap.clear();
    }

    @Override
    public Page<E> getAll(Pageable pageable) {
        Paginator<E> paginator = new Paginator<E>(pageable,getAll());
        return paginator.paginate();
    }
}
