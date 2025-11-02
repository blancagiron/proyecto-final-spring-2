package es.blanca.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.blanca.api.dto.input.ProductInputDto;
import es.blanca.api.dto.output.ProductOutputDto;
import es.blanca.api.mapper.ProductApiMapper;
import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Product;
import es.blanca.domain.model.ProductStatus;
import es.blanca.domain.port.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para ProductController
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ProductService productService;

	@MockBean
	private ProductApiMapper productApiMapper;

	private Product testProduct;
	private ProductOutputDto productOutputDto;
	private ProductInputDto productInputDto;

	@BeforeEach
	void setUp() {
		testProduct = new Product();
		testProduct.setId(1L);
		testProduct.setName("Test Product");
		testProduct.setPrice(99.99);
		testProduct.setStatus(ProductStatus.AVAILABLE);
		testProduct.setCreatedAt(LocalDateTime.now());

		productOutputDto = new ProductOutputDto();
		productOutputDto.setId(1L);
		productOutputDto.setName("Test Product");
		productOutputDto.setPrice(99.99);
		productOutputDto.setStatus(ProductStatus.AVAILABLE);

		productInputDto = new ProductInputDto();
		productInputDto.setName("New Product");
		productInputDto.setPrice(49.99);
		productInputDto.setStatus(ProductStatus.AVAILABLE);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createProduct_shouldReturn201_whenDataIsValid() throws Exception {
		// Arrange
		when(productApiMapper.toDomain(any(ProductInputDto.class))).thenReturn(testProduct);
		when(productService.create(any(Product.class))).thenReturn(testProduct);
		when(productApiMapper.toOutputDto(any(Product.class))).thenReturn(productOutputDto);

		// Act & Assert
		mockMvc.perform(post("/products")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productInputDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Test Product"));

		verify(productService, times(1)).create(any(Product.class));
	}

	@Test
	@WithMockUser(roles = "USER")
	void createProduct_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(post("/products")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productInputDto)))
				.andExpect(status().isForbidden());

		verify(productService, never()).create(any(Product.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createProduct_shouldReturn422_whenNameIsBlank() throws Exception {
		// Arrange
		productInputDto.setName("");

		// Act & Assert
		mockMvc.perform(post("/products")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productInputDto)))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createProduct_shouldReturn422_whenPriceIsNegative() throws Exception {
		// Arrange
		productInputDto.setPrice(-10.0);

		// Act & Assert
		mockMvc.perform(post("/products")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productInputDto)))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@WithMockUser(roles = "USER")
	void findProducts_shouldReturnProducts_withoutFilters() throws Exception {
		// Arrange
		Product product2 = new Product();
		product2.setId(2L);
		product2.setName("Product 2");

		ProductOutputDto output2 = new ProductOutputDto();
		output2.setId(2L);
		output2.setName("Product 2");

		when(productService.findWithFilters(null, null, null, null))
				.thenReturn(Arrays.asList(testProduct, product2));
		when(productApiMapper.toOutputDto(testProduct)).thenReturn(productOutputDto);
		when(productApiMapper.toOutputDto(product2)).thenReturn(output2);

		// Act & Assert
		mockMvc.perform(get("/products")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2));

		verify(productService, times(1)).findWithFilters(null, null, null, null);
	}

	@Test
	@WithMockUser(roles = "USER")
	void findProducts_shouldReturnProducts_withFilters() throws Exception {
		// Arrange
		when(productService.findWithFilters("Test", 50.0, 100.0, ProductStatus.AVAILABLE))
				.thenReturn(Arrays.asList(testProduct));
		when(productApiMapper.toOutputDto(testProduct)).thenReturn(productOutputDto);

		// Act & Assert
		mockMvc.perform(get("/products")
						.with(csrf())
						.param("name", "Test")
						.param("minPrice", "50.0")
						.param("maxPrice", "100.0")
						.param("status", "AVAILABLE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1));

		verify(productService, times(1))
				.findWithFilters("Test", 50.0, 100.0, ProductStatus.AVAILABLE);
	}

	@Test
	@WithMockUser(roles = "USER")
	void getProductById_shouldReturnProduct_whenExists() throws Exception {
		// Arrange
		when(productService.findById(1L)).thenReturn(Optional.of(testProduct));
		when(productApiMapper.toOutputDto(any(Product.class))).thenReturn(productOutputDto);

		// Act & Assert
		mockMvc.perform(get("/products/1")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Test Product"));

		verify(productService, times(1)).findById(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void getProductById_shouldReturn404_whenNotExists() throws Exception {
		// Arrange
		when(productService.findById(999L))
				.thenThrow(new EntityNotFoundException("Product not found"));

		// Act & Assert
		mockMvc.perform(get("/products/999")
						.with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateProduct_shouldReturn200_whenDataIsValid() throws Exception {
		// Arrange
		when(productApiMapper.toDomain(any(ProductInputDto.class))).thenReturn(testProduct);
		doNothing().when(productService).update(eq(1L), any(Product.class));
		when(productService.findById(1L)).thenReturn(Optional.of(testProduct));
		when(productApiMapper.toOutputDto(any(Product.class))).thenReturn(productOutputDto);

		// Act & Assert
		mockMvc.perform(put("/products/1")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productInputDto)))
				.andExpect(status().isOk());

		verify(productService, times(1)).update(eq(1L), any(Product.class));
	}

	@Test
	@WithMockUser(roles = "USER")
	void updateProduct_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(put("/products/1")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productInputDto)))
				.andExpect(status().isForbidden());

		verify(productService, never()).update(any(), any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateProduct_shouldReturn404_whenProductNotFound() throws Exception {
		// Arrange
		when(productApiMapper.toDomain(any(ProductInputDto.class))).thenReturn(testProduct);
		doThrow(new EntityNotFoundException("Product not found"))
				.when(productService).update(eq(999L), any(Product.class));

		// Act & Assert
		mockMvc.perform(put("/products/999")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productInputDto)))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteProduct_shouldReturn204_whenProductExists() throws Exception {
		// Arrange
		doNothing().when(productService).delete(1L);

		// Act & Assert
		mockMvc.perform(delete("/products/1")
						.with(csrf()))
				.andExpect(status().isNoContent());

		verify(productService, times(1)).delete(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void deleteProduct_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(delete("/products/1")
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(productService, never()).delete(any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteProduct_shouldReturn404_whenProductNotFound() throws Exception {
		// Arrange
		doThrow(new EntityNotFoundException("Product not found"))
				.when(productService).delete(999L);

		// Act & Assert
		mockMvc.perform(delete("/products/999")
						.with(csrf()))
				.andExpect(status().isNotFound());
	}
}