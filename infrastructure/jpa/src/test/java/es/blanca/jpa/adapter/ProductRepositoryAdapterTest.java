package es.blanca.jpa.adapter;

import es.blanca.domain.model.Product;
import es.blanca.domain.model.ProductStatus;
import es.blanca.jpa.entity.ProductEntity;
import es.blanca.jpa.mapper.ProductPersistenceMapper;
import es.blanca.jpa.repository.ProductJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

	@Mock
	private ProductJpaRepository productJpaRepository;

	@Mock
	private ProductPersistenceMapper productPersistenceMapper;

	@Mock
	private EntityManager entityManager;

	@Mock
	private CriteriaBuilder criteriaBuilder;

	@Mock
	private CriteriaQuery<ProductEntity> criteriaQuery;

	@Mock
	private Root<ProductEntity> root;

	// add mock for the predicate
	@Mock
	private Predicate mockPredicate;

	@Mock
	private TypedQuery<ProductEntity> typedQuery;

	@InjectMocks
	private ProductRepositoryAdapter productRepositoryAdapter;

	private Product product;
	private ProductEntity productEntity;

	@BeforeEach
	void setUp() {
		// Arrange
		product = new Product();
		product.setId(1L);
		product.setName("Laptop");
		product.setStatus(ProductStatus.AVAILABLE);

		productEntity = new ProductEntity();
		productEntity.setId(1L);
		productEntity.setName("Laptop");
	}

	@Test
	void save_shouldSaveProduct() {
		// Arrange
		when(productPersistenceMapper.toEntity(any(Product.class))).thenReturn(productEntity);
		when(productJpaRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
		when(productPersistenceMapper.toDomain(any(ProductEntity.class))).thenReturn(product);

		// Act
		Product savedProduct = productRepositoryAdapter.save(product);

		// Assert
		assertNotNull(savedProduct);
		assertEquals("Laptop", savedProduct.getName());
	}

	@Test
	void findById_shouldReturnProduct_whenExists() {
		// Arrange
		when(productJpaRepository.findById(1L)).thenReturn(Optional.of(productEntity));
		when(productPersistenceMapper.toDomain(any(ProductEntity.class))).thenReturn(product);

		// Act
		Optional<Product> result = productRepositoryAdapter.findById(1L);

		// Assert
		assertTrue(result.isPresent());
		assertEquals("Laptop", result.get().getName());
	}

	@Test
	void findWithFilters_shouldReturnFilteredProducts() {
		// Arrange
		Map<String, Object> filters = new HashMap<>();
		filters.put("status", ProductStatus.AVAILABLE);

		// Simular la cadena de llamadas de Criteria API
		when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(ProductEntity.class)).thenReturn(criteriaQuery);
		when(criteriaQuery.from(ProductEntity.class)).thenReturn(root);

		// ✅ SOLUCIÓN 2: Simular la creación del predicado
		when(criteriaBuilder.equal(any(), any(ProductStatus.class))).thenReturn(mockPredicate);
		when(criteriaBuilder.and(any(Predicate.class))).thenReturn(mockPredicate);
		when(criteriaQuery.where(mockPredicate)).thenReturn(criteriaQuery);

		when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(Collections.singletonList(productEntity));
		when(productPersistenceMapper.toDomain(productEntity)).thenReturn(product);

		// Act
		List<Product> result = productRepositoryAdapter.findWithFilters(filters);

		// Assert
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
		assertEquals(ProductStatus.AVAILABLE, result.get(0).getStatus());
	}
}