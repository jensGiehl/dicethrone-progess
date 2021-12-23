package de.agiehl.games.dt.persistents.repro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.agiehl.games.dt.persistents.model.BggUserEntity;

@Repository
public interface BggUserRepository extends JpaRepository<BggUserEntity, Long> {

	BggUserEntity findByUsername(String bggUsername);

}
