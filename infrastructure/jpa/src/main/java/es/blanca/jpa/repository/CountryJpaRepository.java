package es.blanca.jpa.repository;

import es.blanca.jpa.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryJpaRepository extends JpaRepository<CountryEntity,String> {
}
