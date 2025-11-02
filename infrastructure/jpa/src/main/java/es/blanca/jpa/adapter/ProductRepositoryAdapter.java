package es.blanca.jpa.adapter;

import es.blanca.domain.model.Product;
import es.blanca.domain.model.ProductStatus;
import es.blanca.domain.port.ProductRepository;
import es.blanca.jpa.entity.ProductEntity;
import es.blanca.jpa.mapper.ProductPersistenceMapper;
import es.blanca.jpa.repository.ProductJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {


	private final ProductJpaRepository productJpaRepository;
	private final ProductPersistenceMapper productPersistenceMapper;
	private final EntityManager entityManager;

	@Override
	public Product save(Product product) {
		ProductEntity productEntity = productPersistenceMapper.toEntity(product) ;
		ProductEntity productEntitySaved = productJpaRepository.save(productEntity);
		return productPersistenceMapper.toDomain(productEntitySaved);
	}

	@Override
	public Optional<Product> findById(Long id) {
		return productJpaRepository.findById(id).map(productPersistenceMapper::toDomain) ;
	}

	@Override
	public List<Product> findAll() {
		return productJpaRepository.findAll().stream()
				.map(productPersistenceMapper::toDomain)
				.collect(Collectors.toList());
	}

	@Override
	public void deleteById(Long id) {
		productJpaRepository.deleteById(id);
	}

	@Override
	public boolean existsById(Long id) {
		return productJpaRepository.existsById(id);
	}

	@Override
	public List<Product> findWithFilters(Map<String, Object> filters) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ProductEntity> query = cb.createQuery(ProductEntity.class);
		Root<ProductEntity> root = query.from(ProductEntity.class); // "FROM products"

		// 2. create a list to store the conditions (the WHERE clause)
		List<Predicate> predicates = new ArrayList<>();

		filters.forEach((field, value) -> {
			switch (field) {
				case "name":
					predicates.add(cb.like(cb.lower(root.get("name")), "%" + ((String) value).toLowerCase() + "%"));
					break;
				case "minPrice":
					predicates.add(cb.greaterThanOrEqualTo(root.get("price"), (Double) value));
					break;
				case "maxPrice":
					predicates.add(cb.lessThanOrEqualTo(root.get("price"), (Double) value));
					break;
				case "status":
					predicates.add(cb.equal(root.get("status"), (ProductStatus) value));
					break;
			}
		});

		// 4. apply WHERE to the query
		query.where(cb.and(predicates.toArray(new Predicate[0])));

		// 5. create and execute the final query
		TypedQuery<ProductEntity> typedQuery = entityManager.createQuery(query);
		List<ProductEntity> resultEntities = typedQuery.getResultList();

		// 6. map the results from Entity to Domain and return them
		return resultEntities.stream()
				.map(productPersistenceMapper::toDomain)
				.collect(Collectors.toList());
	}
}
