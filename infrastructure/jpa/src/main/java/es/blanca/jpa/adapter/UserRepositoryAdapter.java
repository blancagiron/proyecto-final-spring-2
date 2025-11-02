package es.blanca.jpa.adapter;

import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.User;
import es.blanca.domain.port.UserRepository;
import es.blanca.jpa.entity.UserEntity;
import es.blanca.jpa.mapper.UserPersistenceMapper;
import es.blanca.jpa.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final UserJpaRepository userJpaRepository;
	private final UserPersistenceMapper userPersistenceMapper;

	@Override
	public void deleteById(Long userId) {
		userJpaRepository.deleteById(userId);
	}

	@Override
	public boolean existsById(Long userId) {
		return userJpaRepository.existsById(userId);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userJpaRepository.findByEmail(email).map(userPersistenceMapper::toDomain);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmail(email);
	}

	@Override
	public User save(User user) {
		UserEntity userEntity = userPersistenceMapper.toEntity(user);

		// 2. Si el usuario ya existe (es una actualización), debemos preservar la colección de 'orders'.
		if (user.getId() != null) {
			// 2.1. Recupera la entidad original de la base de datos, que sí tiene la lista de 'orders'.
			UserEntity existingEntity = userJpaRepository.findById(user.getId()).orElseThrow(
					() -> new EntityNotFoundException("No se puede actualizar el usuario porque no se encontró el ID: " + user.getId())
			);

			// 2.2. Asigna la colección de 'orders' existente a la nueva entidad que vamos a guardar.
			//      Con esto, evitamos que Hibernate piense que la colección ha sido eliminada.
			userEntity.setOrders(existingEntity.getOrders());
		}

		// 3. Guarda la entidad (ya sea nueva o actualizada con sus 'orders' preservados).
		UserEntity savedEntity = userJpaRepository.save(userEntity);

		// 4. Mapea de vuelta al dominio y retorna.
		return userPersistenceMapper.toDomain(savedEntity);
	}

	@Override
	public Optional<User> findById(Long id) {
		return userJpaRepository.findById(id).map(userPersistenceMapper::toDomain);
	}

	@Override
	public List<User> findAll() {
		return userJpaRepository.findAll().stream()
				.map(userPersistenceMapper::toDomain)
				.collect(Collectors.toList());
	}


}
