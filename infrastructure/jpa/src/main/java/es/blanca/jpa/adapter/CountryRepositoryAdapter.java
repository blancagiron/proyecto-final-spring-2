package es.blanca.jpa.adapter;

import es.blanca.domain.model.Country;
import es.blanca.domain.port.CountryRepository;
import es.blanca.jpa.entity.CountryEntity;
import es.blanca.jpa.mapper.CountryPersistenceMapper;
import es.blanca.jpa.repository.CountryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CountryRepositoryAdapter implements CountryRepository {

	private final CountryJpaRepository countryJpaRepository;
	private final CountryPersistenceMapper countryPersistenceMapper;

	@Override
	public Country save(Country country) {
		CountryEntity countryEntity = countryPersistenceMapper.toEntity(country);
		CountryEntity savedEntity = countryJpaRepository.save(countryEntity);
		return countryPersistenceMapper.toDomain(savedEntity);
	}

	@Override
	public Optional<Country> findById(String id) {
		return countryJpaRepository.findById(id).map(countryPersistenceMapper::toDomain);
	}

	@Override
	public List<Country> findAll() {
		return countryJpaRepository.findAll().stream()
				.map(countryPersistenceMapper::toDomain)
				.collect(Collectors.toList());
	}

	@Override
	public void deleteById(String id) {
		countryJpaRepository.deleteById(id);
	}

	@Override
	public boolean existsById(String id) {
		return countryJpaRepository.existsById(id);
	}
}
