package de.agiehl.games.dt.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.agiehl.games.dt.bggLoader.FetchPlays;
import de.agiehl.games.dt.model.Plays.Play;
import de.agiehl.games.dt.persistents.PersistentsService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FetchAndGenerateHtmlService {

	@Autowired
	private PersistentsService persistens;

	@Autowired
	private FetchPlays playFetcher;

	@Value("#{'${bgg.api.usernames}'.split(',')}")
	private List<String> bggUsernames;

	public void fetchAndPersitsAll() {
		for (String username : bggUsernames) {
			if (username.isBlank()) {
				continue;
			}

			username = username.trim();

			log.info("Process BGG user {}", username);

			Optional<LocalDate> lastUpdateForUsername = persistens.getLastUpdate(username);
			List<Play> playsForUsername = playFetcher.fetchPlays(username, lastUpdateForUsername.orElse(null));
			log.info("Found {} plays for user {}", playsForUsername.size(), username);

			playsForUsername.forEach(play -> persistens.savePlay(play));

			persistens.setLastUpdateDate(username);
		}
	}

}
