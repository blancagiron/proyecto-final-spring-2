package es.blanca.jpa.entity;

import es.blanca.domain.model.Role;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="users")
public class UserEntity {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable=false)
	private String fullName;

	@Column(unique = true, nullable=false)
	private String email;

	@Column(nullable=false)
	private String password;

	@Column(name="created_at", updatable=false)
	private LocalDateTime createdAt;


	@Column(name="is_active")
	private boolean isActive = true;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="country_code")
	private CountryEntity country;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderEntity> orders;

}
