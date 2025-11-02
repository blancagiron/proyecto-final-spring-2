//package es.blanca.jpa.mapper;
//
//import es.blanca.domain.model.OrderProduct;
//import es.blanca.jpa.entity.OrderProductEntity;
//import es.blanca.jpa.entity.OrderProductId;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring", uses = {OrderPersistenceMapper.class, ProductPersistenceMapper.class})
//public interface OrderProductPersistenceMapper {
//
//	/**
//	 * When converting from Entity to Domain, we ignore the 'id' field from the entity
//	 * because the domain model does not have an 'OrderProductId'.
//	 */
//	@Mapping(target = "order", source = "order")
//	@Mapping(target = "product", source = "product")
//	@Mapping(target = "amount", source = "amount")
//	OrderProduct toDomain(OrderProductEntity orderProductEntity);
//
//	/**
//	 * When converting from Domain to Entity, we need to explicitly build the 'OrderProductId'.
//	 * MapStruct cannot infer this, so we do it manually.
//	 */
//	default OrderProductEntity toEntity(OrderProduct orderProduct) {
//		if (orderProduct == null) {
//			return null;
//		}
//
//		OrderProductEntity orderProductEntity = new OrderProductEntity();
//
//		// build key
//		OrderProductId id = new OrderProductId();
//		id.setOrderId(orderProduct.getOrder().getId());
//		id.setProductId(orderProduct.getProduct().getId());
//
//		orderProductEntity.setId(id);
//		orderProductEntity.setOrder(new OrderPersistenceMapperImpl().toEntity(orderProduct.getOrder()));
//		orderProductEntity.setProduct(new ProductPersistenceMapperImpl().toEntity(orderProduct.getProduct()));
//		orderProductEntity.setAmount(orderProduct.getAmount());
//
//		return orderProductEntity;
//	}
//}

package es.blanca.jpa.mapper;

import es.blanca.domain.model.OrderProduct;
import es.blanca.jpa.entity.OrderProductEntity;
import es.blanca.jpa.entity.OrderProductId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // Importa @Lazy

@Mapper(componentModel = "spring")
public abstract class OrderProductPersistenceMapper {

	@Autowired
	@Lazy // <--- ¡Añade esta anotación!
	protected OrderPersistenceMapper orderPersistenceMapper;
	@Autowired
	protected ProductPersistenceMapper productPersistenceMapper;

	@Mapping(target = "order", source = "order")
	@Mapping(target = "product", source = "product")
	@Mapping(target = "amount", source = "amount")
	public abstract OrderProduct toDomain(OrderProductEntity orderProductEntity);

	public OrderProductEntity toEntity(OrderProduct orderProduct) {
		if (orderProduct == null) {
			return null;
		}

		OrderProductEntity orderProductEntity = new OrderProductEntity();

		OrderProductId id = new OrderProductId();
		id.setOrderId(orderProduct.getOrder().getId());
		id.setProductId(orderProduct.getProduct().getId());
		orderProductEntity.setId(id);

		orderProductEntity.setOrder(orderPersistenceMapper.toEntity(orderProduct.getOrder()));
		orderProductEntity.setProduct(productPersistenceMapper.toEntity(orderProduct.getProduct()));
		orderProductEntity.setAmount(orderProduct.getAmount());

		return orderProductEntity;
	}
}