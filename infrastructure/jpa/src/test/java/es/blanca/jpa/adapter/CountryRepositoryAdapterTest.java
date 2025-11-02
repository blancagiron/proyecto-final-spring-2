package es.blanca.jpa.adapter;

import es.blanca.domain.model.Country;
import es.blanca.jpa.entity.CountryEntity;
import es.blanca.jpa.mapper.CountryPersistenceMapper;
import es.blanca.jpa.repository.CountryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryRepositoryAdapterTest {

	@Mock
	private CountryJpaRepository countryJpaRepository;

	@Mock
	private CountryPersistenceMapper countryPersistenceMapper;

	@InjectMocks
	private CountryRepositoryAdapter countryRepositoryAdapter;

	private Country country;
	private CountryEntity countryEntity;

	@BeforeEach
	void setUp() {
		// Arrange
		country = new Country("ES", "Spain");
		countryEntity = new CountryEntity();
		countryEntity.setCode("ES");
		countryEntity.setName("Spain");
	}

	@Test
	void save_shouldSaveCountry_whenCountryIsValid() {
		// Arrange
		when(countryPersistenceMapper.toEntity(any(Country.class))).thenReturn(countryEntity);
		when(countryJpaRepository.save(any(CountryEntity.class))).thenReturn(countryEntity);
		when(countryPersistenceMapper.toDomain(any(CountryEntity.class))).thenReturn(country);

		// Act
		Country savedCountry = countryRepositoryAdapter.save(country);

		// Assert
		assertNotNull(savedCountry);
		assertEquals("ES", savedCountry.getCode());
		verify(countryJpaRepository, times(1)).save(countryEntity);
	}

	@Test
	void findById_shouldReturnCountry_whenIdExists() {
		// Arrange
		when(countryJpaRepository.findById("ES")).thenReturn(Optional.of(countryEntity));
		when(countryPersistenceMapper.toDomain(any(CountryEntity.class))).thenReturn(country);

		// Act
		Optional<Country> foundCountry = countryRepositoryAdapter.findById("ES");

		// Assert
		assertTrue(foundCountry.isPresent());
		assertEquals("ES", foundCountry.get().getCode());
		verify(countryJpaRepository, times(1)).findById("ES");
	}

	@Test
	void findById_shouldReturnEmpty_whenIdDoesNotExist() {
		// Arrange
		when(countryJpaRepository.findById("XX")).thenReturn(Optional.empty());

		// Act
		Optional<Country> foundCountry = countryRepositoryAdapter.findById("XX");

		// Assert
		assertFalse(foundCountry.isPresent());
		verify(countryJpaRepository, times(1)).findById("XX");
	}

	@Test
	void findAll_shouldReturnAllCountries() {
		// Arrange
		when(countryJpaRepository.findAll()).thenReturn(Collections.singletonList(countryEntity));
		when(countryPersistenceMapper.toDomain(any(CountryEntity.class))).thenReturn(country);

		// Act
		List<Country> countries = countryRepositoryAdapter.findAll();

		// Assert
		assertNotNull(countries);
		assertFalse(countries.isEmpty());
		assertEquals(1, countries.size());
		verify(countryJpaRepository, times(1)).findAll();
	}

	@Test
	void deleteById_shouldDeleteCountry() {
		// Arrange
		doNothing().when(countryJpaRepository).deleteById("ES");

		// Act
		countryRepositoryAdapter.deleteById("ES");

		// Assert
		verify(countryJpaRepository, times(1)).deleteById("ES");
	}

	@Test
	void existsById_shouldReturnTrue_whenIdExists() {
		// Arrange
		when(countryJpaRepository.existsById("ES")).thenReturn(true);

		// Act
		boolean exists = countryRepositoryAdapter.existsById("ES");

		// Assert
		assertTrue(exists);
		verify(countryJpaRepository, times(1)).existsById("ES");
	}
}