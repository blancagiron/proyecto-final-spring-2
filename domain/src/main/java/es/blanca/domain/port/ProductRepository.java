package es.blanca.domain.port;

import es.blanca.domain.model.Product;

import java.util.List;
import java.util.Map;

public interface ProductRepository extends  CrudRepository<Product,Long> {
	// search using filters
	List<Product> findWithFilters(Map<String, Object> filters);
}
