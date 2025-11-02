//package es.blanca.jpa.mapper;
//
//import es.blanca.domain.model.Order;
//import es.blanca.jpa.entity.OrderEntity;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration;
//
//import java.util.List;
//
//@Mapper(componentModel = "spring", uses = {
//		UserPersistenceMapper.class, ProductPersistenceMapper.class, OrderProductPersistenceMapper.class
//})
//public interface OrderPersistenceMapper {
//	Order toDomain(OrderEntity orderEntity);
//	OrderEntity toEntity(Order order);
//	List<Order> toDomainList(List<OrderEntity> orderEntities);
//}

package es.blanca.jpa.mapper;

import es.blanca.domain.model.Order;
import es.blanca.jpa.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class OrderPersistenceMapper {

	@Autowired
	@Lazy
	protected UserPersistenceMapper userPersistenceMapper;

	@Autowired
	@Lazy
	protected OrderProductPersistenceMapper orderProductPersistenceMapper;

	public abstract Order toDomain(OrderEntity orderEntity);

	public abstract OrderEntity toEntity(Order order);

	public abstract List<Order> toDomainList(List<OrderEntity> orderEntities);
}