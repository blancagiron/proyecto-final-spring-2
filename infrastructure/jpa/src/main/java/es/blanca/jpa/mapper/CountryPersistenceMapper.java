package es.blanca.jpa.mapper;

import es.blanca.domain.model.Country;
import es.blanca.jpa.entity.CountryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CountryPersistenceMapper {

	Country toDomain(CountryEntity countryEntity);

	@Mapping(target = "users", ignore = true)
	CountryEntity toEntity(Country country);
}

