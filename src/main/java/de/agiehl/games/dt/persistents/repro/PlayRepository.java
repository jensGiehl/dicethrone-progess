package de.agiehl.games.dt.persistents.repro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.agiehl.games.dt.persistents.model.PlayEntity;

@Repository
public interface PlayRepository extends JpaRepository<PlayEntity, Long> {

	boolean existsByBggPlayId(Long id);

	PlayEntity findByBggPlayId(Long id);

}
