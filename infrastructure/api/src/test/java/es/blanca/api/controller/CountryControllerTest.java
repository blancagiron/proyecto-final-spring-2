package es.blanca.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.blanca.api.dto.input.CountryInputDto;
import es.blanca.api.dto.output.CountryOutputDto;
import es.blanca.api.mapper.CountryApiMapper;
import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Country;
import es.blanca.domain.port.CountryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para CountryController
 */
@WebMvcTest(CountryController.class)
class CountryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CountryService countryService;

	@MockBean
	private CountryApiMapper countryApiMapper;

	private Country testCountry;
	private CountryOutputDto countryOutputDto;
	private CountryInputDto countryInputDto;

	@BeforeEach
	void setUp() {
		testCountry = new Country("ES", "Spain");

		countryOutputDto = new CountryOutputDto();
		countryOutputDto.setCode("ES");
		countryOutputDto.setName("Spain");

		countryInputDto = new CountryInputDto();
		countryInputDto.setCode("ES");
		countryInputDto.setName("Spain");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createCountry_shouldReturn201_whenDataIsValid() throws Exception {
		// Arrange
		when(countryApiMapper.toDomain(any(CountryInputDto.class))).thenReturn(testCountry);
		when(countryService.create(any(Country.class))).thenReturn(testCountry);
		when(countryApiMapper.toOutputDto(any(Country.class))).thenReturn(countryOutputDto);

		// Act & Assert
		mockMvc.perform(post("/countries")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(countryInputDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("ES"))
				.andExpect(jsonPath("$.name").value("Spain"));

		verify(countryService, times(1)).create(any(Country.class));
	}

	@Test
	@WithMockUser(roles = "USER")
	void createCountry_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(post("/countries")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(countryInputDto)))
				.andExpect(status().isForbidden());

		verify(countryService, never()).create(any(Country.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createCountry_shouldReturn422_whenCodeIsBlank() throws Exception {
		// Arrange
		countryInputDto.setCode("");

		// Act & Assert
		mockMvc.perform(post("/countries")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(countryInputDto)))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createCountry_shouldReturn422_whenNameIsBlank() throws Exception {
		// Arrange
		countryInputDto.setName("");

		// Act & Assert
		mockMvc.perform(post("/countries")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(countryInputDto)))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@WithMockUser(roles = "USER")
	void getAllCountries_shouldReturnCountries() throws Exception {
		// Arrange
		Country country2 = new Country("FR", "France");
		CountryOutputDto output2 = new CountryOutputDto();
		output2.setCode("FR");
		output2.setName("France");

		when(countryService.findAll()).thenReturn(Arrays.asList(testCountry, country2));
		when(countryApiMapper.toOutputDto(testCountry)).thenReturn(countryOutputDto);
		when(countryApiMapper.toOutputDto(country2)).thenReturn(output2);

		// Act & Assert
		mockMvc.perform(get("/countries")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("ES"))
				.andExpect(jsonPath("$[1].code").value("FR"));

		verify(countryService, times(1)).findAll();
	}

	@Test
	@WithMockUser(roles = "USER")
	void getCountryById_shouldReturnCountry_whenExists() throws Exception {
		// Arrange
		when(countryService.findById("ES")).thenReturn(Optional.of(testCountry));
		when(countryApiMapper.toOutputDto(any(Country.class))).thenReturn(countryOutputDto);

		// Act & Assert
		mockMvc.perform(get("/countries/ES")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("ES"))
				.andExpect(jsonPath("$.name").value("Spain"));

		verify(countryService, times(1)).findById("ES");
	}

	@Test
	@WithMockUser(roles = "USER")
	void getCountryById_shouldReturn404_whenNotExists() throws Exception {
		// Arrange
		when(countryService.findById("XX"))
				.thenThrow(new EntityNotFoundException("Country not found"));

		// Act & Assert
		mockMvc.perform(get("/countries/XX")
						.with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateCountry_shouldReturn200_whenDataIsValid() throws Exception {
		// Arrange
		when(countryApiMapper.toDomain(any(CountryInputDto.class))).thenReturn(testCountry);
		doNothing().when(countryService).update(eq("ES"), any(Country.class));
		when(countryService.findById("ES")).thenReturn(Optional.of(testCountry));
		when(countryApiMapper.toOutputDto(any(Country.class))).thenReturn(countryOutputDto);

		// Act & Assert
		mockMvc.perform(put("/countries/ES")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(countryInputDto)))
				.andExpect(status().isOk());

		verify(countryService, times(1)).update(eq("ES"), any(Country.class));
	}

	@Test
	@WithMockUser(roles = "USER")
	void updateCountry_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(put("/countries/ES")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(countryInputDto)))
				.andExpect(status().isForbidden());

		verify(countryService, never()).update(any(), any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateCountry_shouldReturn404_whenCountryNotFound() throws Exception {
		// Arrange
		when(countryApiMapper.toDomain(any(CountryInputDto.class))).thenReturn(testCountry);
		doThrow(new EntityNotFoundException("Country not found"))
				.when(countryService).update(eq("XX"), any(Country.class));

		// Act & Assert
		mockMvc.perform(put("/countries/XX")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(countryInputDto)))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteCountry_shouldReturn204_whenCountryExists() throws Exception {
		// Arrange
		doNothing().when(countryService).delete("ES");

		// Act & Assert
		mockMvc.perform(delete("/countries/ES")
						.with(csrf()))
				.andExpect(status().isNoContent());

		verify(countryService, times(1)).delete("ES");
	}

	@Test
	@WithMockUser(roles = "USER")
	void deleteCountry_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(delete("/countries/ES")
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(countryService, never()).delete(any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteCountry_shouldReturn404_whenCountryNotFound() throws Exception {
		// Arrange
		doThrow(new EntityNotFoundException("Country not found"))
				.when(countryService).delete("XX");

		// Act & Assert
		mockMvc.perform(delete("/countries/XX")
						.with(csrf()))
				.andExpect(status().isNotFound());
	}
}