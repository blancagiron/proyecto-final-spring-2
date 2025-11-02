package es.blanca.jpa.mapper;

import es.blanca.domain.model.Product;
import es.blanca.jpa.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductPersistenceMapper {

	Product toDomain(ProductEntity productEntity);

	@Mapping(target = "orderProducts", ignore = true)
	ProductEntity toEntity(Product product);
}
