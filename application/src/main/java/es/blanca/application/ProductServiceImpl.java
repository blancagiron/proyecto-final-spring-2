package es.blanca.application;

import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Product;
import es.blanca.domain.model.ProductStatus;
import es.blanca.domain.port.ProductRepository;
import es.blanca.domain.port.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static es.blanca.application.config.ApplicationConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor

public class ProductServiceImpl implements ProductService {
	private final ProductRepository productRepository;
	@Override
	public List<Product> findWithFilters(String name, Double minPrice, Double maxPrice, ProductStatus status) {
		log.info("Searching for products with filters - Name: [{}], MinPrice: [{}], MaxPrice: [{}], Status: [{}]", name, minPrice, maxPrice, status);
		Map<String, Object> filters = new HashMap<>();

		// not null filters
		if (name != null && !name.isEmpty()) filters.put("name", name);
		if (minPrice != null) filters.put("minPrice", minPrice);
		if (maxPrice != null) filters.put("maxPrice", maxPrice);
		if (status != null) filters.put("status", status);

		return productRepository.findWithFilters(filters);
	}

	@Override
	public List<Product> findAll() {
		log.info("Finding all products");
		return productRepository.findAll();
	}

	@Override
	public Optional<Product> findById(Long productId) {
		log.info("Finding product with id: {}", productId);
		return Optional.of(productRepository.findById(productId)
				.orElseThrow(() -> new EntityNotFoundException(String.format(PRODUCT_NOT_FOUND_BY_ID, productId))));
	}

	@Override
	public Product create(Product product) {
		log.info("Trying to create product with name: {}", product.getName());
		product.setCreatedAt(LocalDateTime.now());
		return productRepository.save(product);
	}

	@Override
	public void update(Long productId, Product product) {
		log.info("Attempting to update product: {}", productId);
		Product existingProduct = findById(productId).orElseThrow(() -> new EntityNotFoundException(String.format(PRODUCT_NOT_FOUND_BY_ID, productId)));

		if(product.getName()!=null){
			existingProduct.setName(product.getName());
		}
		if(product.getPrice()!=null){
			existingProduct.setPrice(product.getPrice());
		}
		if(product.getStatus()!=null){
			existingProduct.setStatus(product.getStatus());
		}
		productRepository.save(existingProduct);
		log.info("Product with id: {} updated", productId);
	}

	@Override
	public void delete(Long productId) {
		log.info("Attempting to delete product: {}", productId);
		if(!productRepository.existsById(productId)) {
			throw new EntityNotFoundException(String.format(PRODUCT_NOT_FOUND_BY_ID, productId));
		}
		productRepository.deleteById(productId);
	}
}
