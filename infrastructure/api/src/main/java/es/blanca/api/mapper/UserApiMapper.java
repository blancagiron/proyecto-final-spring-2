package es.blanca.api.mapper;

import es.blanca.api.dto.input.UserCreateInputDto;
import es.blanca.api.dto.input.UserUpdateDto;
import es.blanca.api.dto.output.UserOutputDto;
import es.blanca.domain.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.context.annotation.Bean;

@Mapper(componentModel = "spring", uses = {CountryApiMapper.class, OrderApiMapper.class})
public interface UserApiMapper {
	User toDomain(UserCreateInputDto userCreateInputDto);
	UserOutputDto toOutputDto(User user);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateDomainFromInputDto(UserUpdateDto  userUpdateDto, @MappingTarget User User);
}
