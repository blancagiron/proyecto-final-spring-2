package es.blanca.domain.port;

import es.blanca.domain.model.User;

import java.util.Optional;

public interface UserService extends CrudService<User,Long> {
	Optional<User> assignCountry(Long userId, String countryCode);
}
