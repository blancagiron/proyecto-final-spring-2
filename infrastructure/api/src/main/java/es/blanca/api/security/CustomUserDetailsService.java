package es.blanca.api.security;

import es.blanca.domain.model.User;
import es.blanca.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		log.debug("Loading user by email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

		if (!user.isActive()) {
			throw new UsernameNotFoundException("User is not active: " + email);
		}

		return new org.springframework.security.core.userdetails.User(
				user.getEmail(),
				user.getPassword(),
				getAuthorities(user)
		);
	}

	private Collection<? extends GrantedAuthority> getAuthorities(User user) {
		return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
	}
}