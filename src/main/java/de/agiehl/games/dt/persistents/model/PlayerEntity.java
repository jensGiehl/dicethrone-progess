package de.agiehl.games.dt.persistents.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.agiehl.games.dt.generator.model.PlayableCharacters;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "player")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PlayableCharacters character;

	@Column(nullable = true)
	private String username;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Boolean win;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "play_id", referencedColumnName = "id") })
	private PlayEntity play;

}
