package es.blanca.jpa.adapter;

import es.blanca.domain.model.Product;
import es.blanca.domain.model.ProductStatus;
import es.blanca.jpa.entity.ProductEntity;
import es.blanca.jpa.mapper.ProductPersistenceMapper;
import es.blanca.jpa.repository.ProductJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

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
		// Solo inicializamos los objetos, no los mocks
		product = new Product(1L, "Laptop", 999.99, ProductStatus.AVAILABLE, LocalDateTime.now());
		productEntity = new ProductEntity();
		productEntity.setId(1L);
		productEntity.setName("Laptop");
		productEntity.setPrice(999.99);
		productEntity.setStatus(ProductStatus.AVAILABLE);
		productEntity.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void save_shouldSaveProduct() {
		when(productPersistenceMapper.toEntity(any(Product.class))).thenReturn(productEntity);
		when(productJpaRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
		when(productPersistenceMapper.toDomain(any(ProductEntity.class))).thenReturn(product);
		Product savedProduct = productRepositoryAdapter.save(product);
		assertNotNull(savedProduct);
		assertEquals("Laptop", savedProduct.getName());
	}

	@Test
	void findById_shouldReturnProduct_whenExists() {
		when(productJpaRepository.findById(1L)).thenReturn(Optional.of(productEntity));
		when(productPersistenceMapper.toDomain(any(ProductEntity.class))).thenReturn(product);
		Optional<Product> result = productRepositoryAdapter.findById(1L);
		assertTrue(result.isPresent());
		assertEquals("Laptop", result.get().getName());
	}

	@Test
	void findAll_shouldReturnAllProducts() {
		when(productJpaRepository.findAll()).thenReturn(Collections.singletonList(productEntity));
		when(productPersistenceMapper.toDomain(any(ProductEntity.class))).thenReturn(product);
		List<Product> result = productRepositoryAdapter.findAll();
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
	}

	// --- Tests para findWithFilters (CORREGIDOS) ---

	private void setupCriteriaMocks() {
		// Esta configuración es común para todos los tests de filtros.
		when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(ProductEntity.class)).thenReturn(criteriaQuery);
		when(criteriaQuery.from(ProductEntity.class)).thenReturn(root);
		when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
	}

	@Test
	void findWithFilters_shouldApplyFilters_whenFiltersAreProvided() {
		// Arrange
		setupCriteriaMocks();
		Map<String, Object> filters = new HashMap<>();
		filters.put("status", ProductStatus.AVAILABLE);

		// ✅ ARREGLO: Mockeamos la llamada a `root.get()` para que devuelva un Path (que puede ser el mismo root)
		when(root.get("status")).thenReturn((Path) root);
		when(criteriaBuilder.equal(root, ProductStatus.AVAILABLE)).thenReturn(mockPredicate);
		when(criteriaBuilder.and(mockPredicate)).thenReturn(mockPredicate);
		when(typedQuery.getResultList()).thenReturn(Collections.singletonList(productEntity));
		when(productPersistenceMapper.toDomain(productEntity)).thenReturn(product);

		// Act
		List<Product> result = productRepositoryAdapter.findWithFilters(filters);

		// Assert
		assertFalse(result.isEmpty());
		verify(criteriaQuery, times(1)).where(mockPredicate);
	}

	@Test
	void findWithFilters_shouldNotApplyFilters_whenNoFiltersAreProvided() {
		// Arrange
		setupCriteriaMocks();
		Map<String, Object> filters = new HashMap<>(); // Sin filtros

		when(typedQuery.getResultList()).thenReturn(Collections.singletonList(productEntity));
		when(productPersistenceMapper.toDomain(productEntity)).thenReturn(product);

		// Act
		List<Product> result = productRepositoryAdapter.findWithFilters(filters);

		// Assert
		assertFalse(result.isEmpty());
		// Verificamos que .where() NUNCA FUE llamado.
		verify(criteriaQuery, never()).where(any(Predicate.class));
	}
}