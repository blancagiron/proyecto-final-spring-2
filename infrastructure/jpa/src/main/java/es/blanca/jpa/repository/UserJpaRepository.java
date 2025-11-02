package es.blanca.jpa.repository;

import es.blanca.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity,Long> {
	Optional<UserEntity>findByEmail(String email);
	boolean existsByEmail(String email);
}
