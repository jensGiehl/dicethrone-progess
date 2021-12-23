package de.agiehl.games.dt.generator.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class CharacterPlays {

	@EqualsAndHashCode.Include
	private final PlayableCharacters character;

	private int numberOfPlays;

	private List<PlayerWhoPlayed> playersWhoPlayedThisCharacter;

	@Data
	@Builder
	public static class PlayerWhoPlayed {

		private String name;

		private String bggName;

		private int count;

	}

}
