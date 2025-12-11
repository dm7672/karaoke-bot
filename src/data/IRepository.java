package data;
import java.util.List;

public interface IRepository<T, ID> {
    List<T> findAll();
    T      findById(ID id);
    boolean existsById(ID id);
    void   save(T entity);
    void   update(T entity);
    void   delete(ID id);
}
