package de.agiehl.games.dt.bggLoader;

import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.agiehl.games.dt.model.Plays;
import de.agiehl.games.dt.model.Plays.Play;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FetchPlays {

	@Value("${bgg.api.baseurl}")
	private String bggBaseUrl;

	@Value("${bgg.api.plays.maxresult:100}")
	private int maxResultsPerPage;

	@Value("${bgg.api.use-deltas}")
	private boolean useDeltas;

	@Value("${bgg.api.delta.grace-period.minutes}")
	private long gracePeriodInMinutes;

	@Autowired
	private BggLoader bggLoader;

	public List<Play> fetchPlays(String bggUsername, LocalDate minDate) {
		log.info("Fetch plays for {}", bggUsername);

		int pageCount = 1;
		int maxPage = 1;

		List<Play> allDiceThronePlays = new ArrayList<>();
		do {
			String url = bggBaseUrl + "?username=" + bggUsername + "&page=" + pageCount;
			if (nonNull(minDate) && useDeltas) {
				LocalDate gracePeriodDate = LocalDateTime.now().minusMinutes(gracePeriodInMinutes).toLocalDate();
				if (gracePeriodDate.isBefore(minDate)) {
					minDate = gracePeriodDate;
				}
				url += "&mindate=" + minDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			}

			log.debug("URL is {}", url);
			Plays plays = bggLoader.fetchData(url, Plays.class);

			maxPage = getTotalPages(plays);
			log.info("Process plays for {}. Page {} / {}", bggUsername, pageCount, maxPage);
			var diceThronePlaysOnThisPage = plays.getPlay().stream().filter(isADiceThroneGame()).filter(isComplete())
					.toList();
			log.debug("Found {} Dice Throne plays on page {} (User: {})", diceThronePlaysOnThisPage.size(), pageCount,
					bggUsername);

			allDiceThronePlays.addAll(diceThronePlaysOnThisPage);

			pageCount++;
		} while (pageCount <= maxPage);

		log.info("Found {} Dice Throne plays for user {} since {}", allDiceThronePlays.size(), bggUsername, minDate);

		return allDiceThronePlays;
	}

	private Predicate<? super Play> isComplete() {
		return play -> "0".equals(play.getIncomplete());
	}

	private Predicate<? super Play> isADiceThroneGame() {
		return play -> play.getItem().getName().startsWith("Dice Throne");
	}

	private int getTotalPages(Plays plays) {
		return new BigDecimal(plays.getTotal()).divide(BigDecimal.valueOf(maxResultsPerPage), RoundingMode.UP)
				.intValue();
	}
}
