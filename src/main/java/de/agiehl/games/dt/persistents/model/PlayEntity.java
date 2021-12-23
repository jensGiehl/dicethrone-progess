package de.agiehl.games.dt.persistents.model;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "play")
@Entity
@Data
@NoArgsConstructor
public class PlayEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "bgg_id", nullable = false)
	private Long bggPlayId;

	@Column(name = "play_date", nullable = false)
	private LocalDate playDate;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "play", orphanRemoval = true, fetch = FetchType.EAGER)
	private List<PlayerEntity> players;
}
