package es.blanca.jpa.repository;

import es.blanca.jpa.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductEntity,Long> {
}
