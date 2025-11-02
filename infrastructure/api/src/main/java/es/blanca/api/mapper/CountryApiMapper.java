package es.blanca.api.mapper;

import es.blanca.api.dto.input.CountryInputDto;
import es.blanca.api.dto.output.CountryOutputDto;
import es.blanca.domain.model.Country;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CountryApiMapper {
	Country toDomain(CountryInputDto countryInputDto);
	CountryOutputDto toOutputDto(Country country);
}
