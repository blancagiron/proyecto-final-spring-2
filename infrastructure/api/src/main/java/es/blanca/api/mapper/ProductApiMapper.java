package es.blanca.api.mapper;

import es.blanca.api.dto.input.ProductInputDto;
import es.blanca.api.dto.output.ProductOutputDto;
import es.blanca.domain.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductApiMapper {
	Product toDomain(ProductInputDto productInputDto);
	ProductOutputDto toOutputDto(Product product);
}
