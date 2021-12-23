package de.agiehl.games.dt.persistents.repro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.agiehl.games.dt.generator.model.PlayableCharacters;
import de.agiehl.games.dt.persistents.model.CharacterCount;
import de.agiehl.games.dt.persistents.model.PlayerEntity;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

	List<PlayerEntity> findByUsername(String username);

	@Query("SELECT DISTINCT username FROM PlayerEntity WHERE username is not null AND username <> ''")
	List<String> findDistinctBggUsernames();

	@Query("SELECT DISTINCT name FROM PlayerEntity WHERE username is null OR username = ''")
	List<String> findDistinctNames();

	@Query(value = "SELECT character, count(*) as count FROM player where name = ?1 group by character", nativeQuery = true)
	List<CharacterCount> findAllCharactersByPlayername(String name);

	@Query("SELECT max(pl.playDate) FROM PlayEntity pl JOIN PlayerEntity p ON p.play = pl.id WHERE p.name = ?1 AND p.character = ?2")
	LocalDate getLastPlayedDateForPlayernameAndCharacter(String playername, PlayableCharacters character);

	@Query("SELECT count(*) FROM PlayerEntity WHERE name = ?1 AND character = ?2 AND win = true")
	int countWinByName(String playername, PlayableCharacters character);

	@Query(value = "SELECT character, count(*) as count FROM player where username = ?1 group by character", nativeQuery = true)
	List<CharacterCount> findAllCharactersByUsername(String bggUsername);

	@Query("SELECT max(pl.playDate) FROM PlayEntity pl JOIN PlayerEntity p ON p.play = pl.id WHERE p.username = ?1 AND p.character = ?2")
	LocalDate getLastPlayedDateForUsernameAndCharacter(String bggUsername, PlayableCharacters character);

	@Query("SELECT count(*) FROM PlayerEntity WHERE username = ?1 AND character = ?2 AND win = true")
	int countWinByUsername(String bggUsername, PlayableCharacters character);

	@Query("SELECT count(*) FROM PlayerEntity WHERE name = ?1 AND character = ?2")
	int countPlayedByName(String name, PlayableCharacters character);

	@Query("SELECT count(*) FROM PlayerEntity WHERE username = ?1 AND character = ?2")
	int countPlayedByUsername(String bggUsername, PlayableCharacters character);
}
