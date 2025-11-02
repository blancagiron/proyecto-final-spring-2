package es.blanca.domain.port;

import es.blanca.domain.model.Product;
import es.blanca.domain.model.ProductStatus;

import java.util.List;
import java.util.Map;

public interface ProductService extends CrudService<Product,Long> {
	List<Product> findWithFilters(String name, Double minPrice, Double maxPrice, ProductStatus status);
}
