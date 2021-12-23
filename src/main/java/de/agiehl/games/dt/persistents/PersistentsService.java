package de.agiehl.games.dt.persistents;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.agiehl.games.dt.generator.CharacterNameMapping;
import de.agiehl.games.dt.generator.model.PlayableCharacters;
import de.agiehl.games.dt.model.Plays.Play;
import de.agiehl.games.dt.model.Plays.Play.Players.Player;
import de.agiehl.games.dt.persistents.model.BggUserEntity;
import de.agiehl.games.dt.persistents.model.CharacterCount;
import de.agiehl.games.dt.persistents.model.PlayEntity;
import de.agiehl.games.dt.persistents.model.PlayerEntity;
import de.agiehl.games.dt.persistents.repro.BggUserRepository;
import de.agiehl.games.dt.persistents.repro.PlayRepository;
import de.agiehl.games.dt.persistents.repro.PlayerRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PersistentsService {

	@Autowired
	private BggUserRepository bggUserRepro;

	@Autowired
	private PlayerRepository playerRepro;

	@Autowired
	private PlayRepository playRepro;

	@Autowired
	private CharacterNameMapping characterNameMapping;

	public Map<PlayableCharacters, Long> getCharacterPlays() {
		return playerRepro.findAll().stream().collect(groupingBy(PlayerEntity::getCharacter, counting()));
	}

	public List<String> getDistinctPlayerWithBggUsername() {
		return playerRepro.findDistinctBggUsernames();
	}

	public List<String> getDistinctPlayerWithoutBggUsername() {
		return playerRepro.findDistinctNames();
	}

	public List<String> getNamesForBggUsername(String username) {
		return playerRepro.findByUsername(username).stream().map(PlayerEntity::getName).distinct().toList();
	}

	public void setLastUpdateDate(String bggUsername) {
		BggUserEntity entity = bggUserRepro.findByUsername(bggUsername);
		if (isNull(entity)) {
			log.info("Added new BGG username to database: {}", bggUsername);
			entity = new BggUserEntity();
			entity.setUsername(bggUsername);
		}

		entity.setLastUpdate(LocalDate.now());
		bggUserRepro.save(entity);
		log.debug("Updated last updated for user {}", bggUsername);
	}

	public Optional<LocalDate> getLastUpdate(String bggUsername) {
		BggUserEntity entity = bggUserRepro.findByUsername(bggUsername);
		if (nonNull(entity)) {
			return Optional.ofNullable(entity.getLastUpdate());
		} else {
			return empty();
		}
	}

	public void savePlay(Play play) {
		Long bggId = Long.parseLong(play.getId());

		final PlayEntity entity = getOrCreatePlayEntity(bggId);

		entity.setPlayDate(convertDate(play.getDate()));
		if (nonNull(entity.getPlayers()) && !entity.getPlayers().isEmpty()) {
			entity.getPlayers().clear();
		}

		var players = getPlayerWhichContainsCharacterName(play);
		entity.setPlayers(players.stream().map(p -> toEntity(p, entity, play)).toList());

		playRepro.save(entity);
	}

	private PlayEntity getOrCreatePlayEntity(Long bggId) {
		PlayEntity entity = playRepro.findByBggPlayId(bggId);
		if (isNull(entity)) {
			log.info("Added new play to database: {}", bggId);
			entity = new PlayEntity();
			entity.setBggPlayId(bggId);
		}
		return entity;
	}

	private PlayerEntity toEntity(Player player, PlayEntity entity, Play play) {
		return PlayerEntity.builder() //
				.username(player.getUsername())//
				.character(getCharakter(player, play)) //
				.name(player.getName())//
				.win(isWinner(player))//
				.play(entity) //
				.build();
	}

	private PlayableCharacters getCharakter(Player player, Play play) {
		var character = characterNameMapping.mapCharacter(player.getColor());
		if (character.isPresent()) {
			return character.get();
		} else {
			log.warn("'{}' can't be mapped as character! Play-Id: {}  Name: {}", player.getColor(), play.getId(),
					player.getName());
			return null;
		}
	}

	private Boolean isWinner(Player player) {
		return "1".equals(player.getWin());
	}

	/**
	 * Somewhat unfortunate from the BGG API: Team/color is simply given as color in
	 * the API.
	 * 
	 * As team name (or color) the name of the character is expected, therefore this
	 * method filters out all players without reference to the played character.
	 * 
	 * @param play
	 * @return
	 */
	private List<Player> getPlayerWhichContainsCharacterName(Play play) {
		if (isNull(play.getPlayers()) || isNull(play.getPlayers().getPlayer())) {
			log.warn("No players found for play {}", play.getId());
			return Collections.emptyList();
		}
		return play.getPlayers().getPlayer().stream().filter(filterAndLogNonNull(play))
				.filter(filterNonMappableCharacters(play)).toList();
	}

	private Predicate<? super Player> filterNonMappableCharacters(Play play) {
		return p -> {
			var mappedCharacter = getCharakter(p, play);

			if (nonNull(mappedCharacter)) {
				return true;
			} else {
				log.warn("Player {} in Play #{} ({}) as an invalid 'Team/color': {}", p.getName(), play.getId(),
						play.getDate(), p.getColor());
				return false;
			}
		};
	}

	private Predicate<? super Player> filterAndLogNonNull(Play play) {
		return p -> {
			if (nonNull(p.getColor()) && !p.getColor().isBlank()) {
				return true;
			} else {
				log.warn(
						"Player {} in Play #{} ({}) as no 'Team/color'. Thus no assignment to the played character is possible",
						p.getName(), play.getId(), play.getDate());
				return false;
			}
		};
	}

	private LocalDate convertDate(XMLGregorianCalendar date) {
		return date.toGregorianCalendar().toZonedDateTime().toLocalDate();
	}

	public List<CharacterCount> getPlayedCharactersForPlayername(String name) {
		return playerRepro.findAllCharactersByPlayername(name);
	}

	public LocalDate getLastPlayedDateForPlayernameAndCharacter(String name, PlayableCharacters character) {
		return playerRepro.getLastPlayedDateForPlayernameAndCharacter(name, character);
	}

	public int countPlayesForNameAndCharacter(String name, PlayableCharacters character) {
		return playerRepro.countPlayedByName(name, character);
	}

	public int countWinByNameAndCharacter(String name, PlayableCharacters character) {
		return playerRepro.countWinByName(name, character);
	}

	public List<CharacterCount> getPlayedCharactersForUsername(String bggUsername) {
		return playerRepro.findAllCharactersByUsername(bggUsername);
	}

	public LocalDate getLastPlayedDateForUsernameAndCharacter(String bggUsername, PlayableCharacters character) {
		return playerRepro.getLastPlayedDateForUsernameAndCharacter(bggUsername, character);
	}

	public int countWinByUsernameAndCharacter(String bggUsername, PlayableCharacters character) {
		return playerRepro.countWinByUsername(bggUsername, character);
	}

	public int countPlayesForUserAndCharacter(String bggUsername, PlayableCharacters character) {
		return playerRepro.countPlayedByUsername(bggUsername, character);
	}

}
