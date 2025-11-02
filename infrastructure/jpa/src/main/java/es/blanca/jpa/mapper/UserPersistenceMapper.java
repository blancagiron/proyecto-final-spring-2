package es.blanca.jpa.mapper;

import es.blanca.domain.model.User;
import es.blanca.jpa.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

//@Mapper(componentModel = "spring", uses = {CountryPersistenceMapper.class, OrderPersistenceMapper.class})
//public interface UserPersistenceMapper {
//	User toDomain(UserEntity user);
//
//	@Mapping(target = "orders", ignore = true)
//	UserEntity toEntity(User domain);
//}

@Mapper(componentModel = "spring", uses={CountryPersistenceMapper.class})
public abstract  class UserPersistenceMapper{
	@Autowired
	@Lazy
	protected OrderPersistenceMapper orderPersistenceMapper;

	@Mapping(target = "orders", expression = "java(orderPersistenceMapper.toDomainList(user.getOrders()))")
	public abstract User toDomain(UserEntity user);

	@Mapping(target = "orders", ignore = true)
	public abstract UserEntity toEntity(User domain);

}
