package es.blanca.application;

import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Country;
import es.blanca.domain.port.CountryRepository;
import es.blanca.domain.port.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static es.blanca.application.config.ApplicationConstants.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor

public class CountryServiceImpl implements CountryService {
	private final CountryRepository countryRepository;
	@Override
	public List<Country> findAll() {
		log.info("Finding all countries");
		return countryRepository.findAll();
	}

	@Override
	public Optional<Country> findById(String code) {
		log.info("Finding country by code {}", code);
		return Optional.of(countryRepository.findById(code)
				.orElseThrow(() -> new EntityNotFoundException(String.format(COUNTRY_NOT_FOUND_BY_CODE, code))));
	}

	@Override
	public Country create(Country country) {
		log.info("Creating country {}", country);
		return countryRepository.save(country);
	}

	@Override
	public void update(String code, Country country) {
		log.info("Trying to update country {}", country);
		Country existingCountry = countryRepository.findById(code).orElseThrow(() -> new EntityNotFoundException(String.format(COUNTRY_NOT_FOUND_BY_CODE, code)));
		if(country.getName()!=null) {
			existingCountry.setName(country.getName());
		}
		if(country.getCode()!=null) {
			existingCountry.setCode(country.getCode());
		}
		countryRepository.save(existingCountry);
		log.info("Country updated successfully");
	}

	@Override
	public void delete(String code) {
		log.info("Trying to delete country {}", code);
		if(!countryRepository.existsById(code)) {
			throw new EntityNotFoundException(String.format(COUNTRY_NOT_FOUND_BY_CODE, code));
		}
		countryRepository.deleteById(code);
		log.info("Country deleted successfully");
	}
}
