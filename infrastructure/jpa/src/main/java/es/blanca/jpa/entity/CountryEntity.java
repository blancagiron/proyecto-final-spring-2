package es.blanca.jpa.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="countries")
public class CountryEntity {
	@Id
	private String code;

	@Column(nullable = false, unique = true)
	private String name;

	@OneToMany(mappedBy = "country")
	private List<UserEntity> users;

}
