package es.blanca.api.mapper;

import es.blanca.api.dto.input.OrderInputDto;
import es.blanca.api.dto.output.OrderOutputDto;
import es.blanca.domain.model.Order;
import es.blanca.domain.model.OrderProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderApiMapper {
	@Mapping(source = "user.id", target = "userId")
	OrderOutputDto toOutputDto(Order order);

	OrderInputDto toInputDto(Order order);
	Order toDomain(OrderInputDto inputDto);

	@Mapping(source = "product.id", target = "productId")
	@Mapping(source = "product.name", target = "productName")
	OrderOutputDto.OrderProductOutputDto toOrderProductOutputDto(OrderProduct orderProduct);
}
