package es.blanca.domain.port;

import java.util.List;
import java.util.Optional;

public interface CrudService <T, ID>{
	List<T> findAll();
	Optional<T> findById(ID id);
	T create(T entity);
	void update(ID id, T entity);
	void delete(ID id);
}
