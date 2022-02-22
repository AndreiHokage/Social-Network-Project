package socialNetwork.repository.paging;

import java.util.stream.Stream;

public class PageImplementation<T> implements Page<T> {
    private Pageable pageable;
    private Stream<T> content;

    public PageImplementation(Pageable pageable, Stream<T> content) {
        this.pageable = pageable;
        this.content = content;
    }

    @Override
    public Pageable getPageable() {
        return this.pageable;
    }

    @Override
    public Pageable nextPageable() {
        return new PageableImplementation(pageable.getPageNumber() + 1,
                pageable.getPageSize() );
    }

    @Override
    public Stream<T> getContent() {
        return this.content;
    }
}
