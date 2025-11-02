package es.blanca.domain.port;

import es.blanca.domain.model.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User,Long> {
	// specific User methods
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
}
