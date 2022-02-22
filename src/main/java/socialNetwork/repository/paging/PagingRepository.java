package socialNetwork.repository.paging;

import socialNetwork.domain.models.Entity;
import socialNetwork.repository.RepositoryInterface;


public interface PagingRepository<ID,E extends Entity<ID>>
        extends RepositoryInterface<ID,E> {

    Page<E> getAll(Pageable pageable);
}
