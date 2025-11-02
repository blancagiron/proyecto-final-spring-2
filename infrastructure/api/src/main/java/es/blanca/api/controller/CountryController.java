package es.blanca.api.controller;

import es.blanca.api.dto.input.CountryInputDto;
import es.blanca.api.dto.output.CountryOutputDto;
import es.blanca.api.mapper.CountryApiMapper;
import es.blanca.domain.model.Country;
import es.blanca.domain.port.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

	private final CountryService countryService;
	private final CountryApiMapper countryApiMapper;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CountryOutputDto> createCountry(@Valid @RequestBody CountryInputDto dto) {
		log.info("Attempting to create country: {}", dto.getCode());
		Country createdCountry = countryService.create(countryApiMapper.toDomain(dto));
		return new ResponseEntity<>(countryApiMapper.toOutputDto(createdCountry), HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<List<CountryOutputDto>> getAllCountries() {
		log.info("Request to fetch all countries.");
		return ResponseEntity.ok(countryService.findAll().stream()
				.map(countryApiMapper::toOutputDto)
				.collect(Collectors.toList()));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<CountryOutputDto> getCountryById(@PathVariable String id) {
		Country country = countryService.findById(id).orElseThrow();
		return ResponseEntity.ok(countryApiMapper.toOutputDto(country));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CountryOutputDto> updateCountry(@PathVariable String id, @Valid @RequestBody CountryInputDto dto) {
		Country countryToUpdate = countryApiMapper.toDomain(dto);
		countryService.update(id, countryToUpdate);
		Country updatedCountry = countryService.findById(id).orElseThrow();
		return ResponseEntity.ok(countryApiMapper.toOutputDto(updatedCountry));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCountry(@PathVariable String id) {
		countryService.delete(id);
	}
}