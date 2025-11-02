package es.blanca.api.controller;

import es.blanca.api.dto.input.ProductInputDto;
import es.blanca.api.dto.output.ProductOutputDto;
import es.blanca.api.mapper.ProductApiMapper;
import es.blanca.domain.model.Product;
import es.blanca.domain.model.ProductStatus;
import es.blanca.domain.port.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;
	private final ProductApiMapper productApiMapper;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductOutputDto> createProduct(@Valid @RequestBody ProductInputDto dto) {
		log.info("Attempting to create product: {}", dto.getName());
		Product createdProduct = productService.create(productApiMapper.toDomain(dto));
		return new ResponseEntity<>(productApiMapper.toOutputDto(createdProduct), HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<List<ProductOutputDto>> findProducts(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) Double minPrice,
			@RequestParam(required = false) Double maxPrice,
			@RequestParam(required = false) ProductStatus status) {
		log.info("Searching for products with filters");
		List<Product> products = productService.findWithFilters(name, minPrice, maxPrice, status);
		return ResponseEntity.ok(products.stream().map(productApiMapper::toOutputDto).collect(Collectors.toList()));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ProductOutputDto> getProductById(@PathVariable Long id) {
		Product product = productService.findById(id).orElseThrow();
		return ResponseEntity.ok(productApiMapper.toOutputDto(product));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductOutputDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductInputDto dto) {
		log.info("Attempting to update product: {}", id);
		Product productToUpdate = productApiMapper.toDomain(dto);
		productService.update(id, productToUpdate);
		Product updatedProduct = productService.findById(id).orElseThrow();
		return ResponseEntity.ok(productApiMapper.toOutputDto(updatedProduct));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProduct(@PathVariable Long id) {
		log.info("Attempting to delete product: {}", id);
		productService.delete(id);
	}
}