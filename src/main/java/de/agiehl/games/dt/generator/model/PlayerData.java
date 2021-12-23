package de.agiehl.games.dt.generator.model;

import static java.util.Objects.nonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class PlayerData {

	private final String playerName;

	private final String bggUsername;

	@EqualsAndHashCode.Exclude
	private final List<PlayedCharacter> playedCharacters = new ArrayList<>(PlayableCharacters.values().length);

	public boolean isBggUserPresent() {
		return bggUsername != null && !bggUsername.isBlank();
	}

	public void addPlayedCharacter(PlayedCharacter pc) {
		playedCharacters.add(pc);
	}

	public int getTotalPlays() {
		return playedCharacters.stream().mapToInt(PlayedCharacter::getTotalPlayed).sum();
	}

	public int getTotalWins() {
		return playedCharacters.stream().mapToInt(PlayedCharacter::getWinCount).sum();
	}

	@Data
	@Builder
	public static class PlayedCharacter {

		@EqualsAndHashCode.Include
		private PlayableCharacters charachter;

		private int winCount;

		private int totalPlayed;

		private LocalDate lastPlayedAt;

		public String getLastPlayed() {
			if (nonNull(lastPlayedAt)) {
				return lastPlayedAt.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
			} else {
				return "";
			}
		}

		public String getName() {
			return charachter.name();
		}

		public void incrementTotalPlayed() {
			totalPlayed++;
		}

		public void incrementWinCount() {
			winCount++;
		}

	}

}
