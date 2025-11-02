package es.blanca.application;

import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Country;
import es.blanca.domain.port.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceImplTest {

	@Mock
	private CountryRepository countryRepository;

	@InjectMocks
	private CountryServiceImpl countryService;

	private Country country;

	@BeforeEach
	void setUp() {
		country = new Country("ES", "Spain");
	}

	@Test
	void findAll_shouldReturnAllCountries() {
		// Arrange
		Country country2 = new Country("FR", "France");
		when(countryRepository.findAll()).thenReturn(Arrays.asList(country, country2));

		// Act
		List<Country> result = countryService.findAll();

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(countryRepository, times(1)).findAll();
	}

	@Test
	void findById_shouldReturnCountry_whenExists() {
		// Arrange
		when(countryRepository.findById("ES")).thenReturn(Optional.of(country));

		// Act
		Optional<Country> result = countryService.findById("ES");

		// Assert
		assertTrue(result.isPresent());
		assertEquals("ES", result.get().getCode());
		assertEquals("Spain", result.get().getName());
		verify(countryRepository, times(1)).findById("ES");
	}

	@Test
	void findById_shouldThrowException_whenNotExists() {
		// Arrange
		when(countryRepository.findById("XX")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			countryService.findById("XX");
		});
	}

	@Test
	void create_shouldCreateCountry() {
		// Arrange
		Country newCountry = new Country("IT", "Italy");
		when(countryRepository.save(any(Country.class))).thenReturn(newCountry);

		// Act
		Country result = countryService.create(newCountry);

		// Assert
		assertNotNull(result);
		assertEquals("IT", result.getCode());
		verify(countryRepository, times(1)).save(newCountry);
	}

	@Test
	void update_shouldUpdateCountry_whenExists() {
		// Arrange
		Country existingCountry = new Country("ES", "Spain");
		Country updateData = new Country("ES", "España");

		when(countryRepository.findById("ES")).thenReturn(Optional.of(existingCountry));
		when(countryRepository.save(any(Country.class))).thenReturn(existingCountry);

		// Act
		countryService.update("ES", updateData);

		// Assert
		verify(countryRepository, times(1)).findById("ES");
		verify(countryRepository, times(1)).save(any(Country.class));
	}

	@Test
	void update_shouldUpdateOnlyName_whenOnlyNameProvided() {
		// Arrange
		Country existingCountry = new Country("ES", "Spain");
		Country updateData = new Country();
		updateData.setName("España");

		when(countryRepository.findById("ES")).thenReturn(Optional.of(existingCountry));
		when(countryRepository.save(any(Country.class))).thenReturn(existingCountry);

		// Act
		countryService.update("ES", updateData);

		// Assert
		verify(countryRepository, times(1)).save(any(Country.class));
	}

	@Test
	void update_shouldUpdateOnlyCode_whenOnlyCodeProvided() {
		// Arrange
		Country existingCountry = new Country("ES", "Spain");
		Country updateData = new Country();
		updateData.setCode("ESP");

		when(countryRepository.findById("ES")).thenReturn(Optional.of(existingCountry));
		when(countryRepository.save(any(Country.class))).thenReturn(existingCountry);

		// Act
		countryService.update("ES", updateData);

		// Assert
		verify(countryRepository, times(1)).save(any(Country.class));
	}

	@Test
	void update_shouldThrowException_whenCountryNotFound() {
		// Arrange
		when(countryRepository.findById("XX")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			countryService.update("XX", country);
		});
		verify(countryRepository, never()).save(any());
	}

	@Test
	void delete_shouldDeleteCountry_whenExists() {
		// Arrange
		when(countryRepository.existsById("ES")).thenReturn(true);
		doNothing().when(countryRepository).deleteById("ES");

		// Act
		countryService.delete("ES");

		// Assert
		verify(countryRepository, times(1)).existsById("ES");
		verify(countryRepository, times(1)).deleteById("ES");
	}

	@Test
	void delete_shouldThrowException_whenCountryNotFound() {
		// Arrange
		when(countryRepository.existsById("XX")).thenReturn(false);

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			countryService.delete("XX");
		});
		verify(countryRepository, never()).deleteById(any());
	}
}