package data;

import java.util.*;
import java.util.function.Function;

public class InMemoryRepository<T, ID> implements IRepository<T, ID> {
    private final Map<ID, T> store;
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.store       = new HashMap<>();
        this.idExtractor = idExtractor;
    }

    protected ID extractId(T entity) {
        return idExtractor.apply(entity);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public T findById(ID id) {
        return store.get(id);
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id) != null;
    }

    @Override
    public void save(T entity) {
        ID id = extractId(entity);
        if (id == null) {
            throw new IllegalArgumentException("ID должен быть установлен перед сохранением");
        }
        store.put(id, entity);
    }

    @Override
    public void update(T entity) {
        ID id = extractId(entity);
        if (!store.containsKey(id)) {
            throw new NoSuchElementException("Объект не найден: " + id);
        }
        store.put(id, entity);
    }

    @Override
    public void delete(ID id) {
        store.remove(id);
    }
}
