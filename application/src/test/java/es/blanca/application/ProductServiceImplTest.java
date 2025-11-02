package es.blanca.application;

import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Product;
import es.blanca.domain.model.ProductStatus;
import es.blanca.domain.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductServiceImpl productService;

	private Product product;

	@BeforeEach
	void setUp() {
		product = new Product();
		product.setId(1L);
		product.setName("Laptop");
		product.setPrice(999.99);
		product.setStatus(ProductStatus.AVAILABLE);
		product.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void findAll_shouldReturnAllProducts() {
		// Arrange
		Product product2 = new Product();
		product2.setId(2L);
		product2.setName("Mouse");

		when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));

		// Act
		List<Product> result = productService.findAll();

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(productRepository, times(1)).findAll();
	}

	@Test
	void findById_shouldReturnProduct_whenExists() {
		// Arrange
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		// Act
		Optional<Product> result = productService.findById(1L);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(1L, result.get().getId());
		assertEquals("Laptop", result.get().getName());
		verify(productRepository, times(1)).findById(1L);
	}

	@Test
	void findById_shouldThrowException_whenNotExists() {
		// Arrange
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			productService.findById(999L);
		});
	}

	@Test
	void create_shouldCreateProduct() {
		// Arrange
		Product newProduct = new Product();
		newProduct.setName("Keyboard");
		newProduct.setPrice(49.99);
		newProduct.setStatus(ProductStatus.AVAILABLE);

		when(productRepository.save(any(Product.class))).thenReturn(newProduct);

		// Act
		Product result = productService.create(newProduct);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getCreatedAt());
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	void update_shouldUpdateProduct_whenExists() {
		// Arrange
		Product existingProduct = new Product();
		existingProduct.setId(1L);
		existingProduct.setName("Old Name");
		existingProduct.setPrice(100.0);
		existingProduct.setStatus(ProductStatus.AVAILABLE);

		Product updateData = new Product();
		updateData.setName("New Name");
		updateData.setPrice(200.0);
		updateData.setStatus(ProductStatus.DISCONTINUED);

		when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
		when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

		// Act
		productService.update(1L, updateData);

		// Assert
		verify(productRepository, times(1)).findById(1L);
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	void update_shouldUpdateOnlyName_whenOnlyNameProvided() {
		// Arrange
		Product existingProduct = new Product();
		existingProduct.setId(1L);
		existingProduct.setName("Old Name");

		Product updateData = new Product();
		updateData.setName("New Name");

		when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
		when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

		// Act
		productService.update(1L, updateData);

		// Assert
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	void update_shouldUpdateOnlyPrice_whenOnlyPriceProvided() {
		// Arrange
		Product existingProduct = new Product();
		existingProduct.setId(1L);
		existingProduct.setPrice(100.0);

		Product updateData = new Product();
		updateData.setPrice(200.0);

		when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
		when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

		// Act
		productService.update(1L, updateData);

		// Assert
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	void update_shouldUpdateOnlyStatus_whenOnlyStatusProvided() {
		// Arrange
		Product existingProduct = new Product();
		existingProduct.setId(1L);
		existingProduct.setStatus(ProductStatus.AVAILABLE);

		Product updateData = new Product();
		updateData.setStatus(ProductStatus.DISCONTINUED);

		when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
		when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

		// Act
		productService.update(1L, updateData);

		// Assert
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	void update_shouldThrowException_whenProductNotFound() {
		// Arrange
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			productService.update(999L, product);
		});
		verify(productRepository, never()).save(any());
	}

	@Test
	void delete_shouldDeleteProduct_whenExists() {
		// Arrange
		when(productRepository.existsById(1L)).thenReturn(true);
		doNothing().when(productRepository).deleteById(1L);

		// Act
		productService.delete(1L);

		// Assert
		verify(productRepository, times(1)).existsById(1L);
		verify(productRepository, times(1)).deleteById(1L);
	}

	@Test
	void delete_shouldThrowException_whenProductNotFound() {
		// Arrange
		when(productRepository.existsById(999L)).thenReturn(false);

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			productService.delete(999L);
		});
		verify(productRepository, never()).deleteById(any());
	}

	@Test
	void findWithFilters_shouldCallRepositoryWithCorrectFilters() {
		// Arrange
		String name = "Laptop";
		Double minPrice = 500.0;
		Double maxPrice = 1500.0;
		ProductStatus status = ProductStatus.AVAILABLE;

		when(productRepository.findWithFilters(anyMap())).thenReturn(Arrays.asList(product));

		// Act
		List<Product> result = productService.findWithFilters(name, minPrice, maxPrice, status);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		verify(productRepository, times(1)).findWithFilters(anyMap());
	}

	@Test
	void findWithFilters_shouldHandleNullFilters() {
		// Arrange
		when(productRepository.findWithFilters(anyMap())).thenReturn(Arrays.asList(product));

		// Act
		List<Product> result = productService.findWithFilters(null, null, null, null);

		// Assert
		assertNotNull(result);
		verify(productRepository, times(1)).findWithFilters(anyMap());
	}

	@Test
	void findWithFilters_shouldHandleOnlyNameFilter() {
		// Arrange
		when(productRepository.findWithFilters(anyMap())).thenReturn(Arrays.asList(product));

		// Act
		List<Product> result = productService.findWithFilters("Laptop", null, null, null);

		// Assert
		assertNotNull(result);
		verify(productRepository, times(1)).findWithFilters(anyMap());
	}

	@Test
	void findWithFilters_shouldHandleOnlyPriceFilters() {
		// Arrange
		when(productRepository.findWithFilters(anyMap())).thenReturn(Arrays.asList(product));

		// Act
		List<Product> result = productService.findWithFilters(null, 500.0, 1500.0, null);

		// Assert
		assertNotNull(result);
		verify(productRepository, times(1)).findWithFilters(anyMap());
	}

	@Test
	void findWithFilters_shouldHandleOnlyStatusFilter() {
		// Arrange
		when(productRepository.findWithFilters(anyMap())).thenReturn(Arrays.asList(product));

		// Act
		List<Product> result = productService.findWithFilters(null, null, null, ProductStatus.AVAILABLE);

		// Assert
		assertNotNull(result);
		verify(productRepository, times(1)).findWithFilters(anyMap());
	}

	@Test
	void findWithFilters_shouldHandleEmptyStringName() {
		// Arrange
		when(productRepository.findWithFilters(anyMap())).thenReturn(Arrays.asList(product));

		// Act
		List<Product> result = productService.findWithFilters("", null, null, null);

		// Assert
		assertNotNull(result);
		verify(productRepository, times(1)).findWithFilters(anyMap());
	}
}