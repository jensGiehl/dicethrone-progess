package de.agiehl.games.dt.generator;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import de.agiehl.games.dt.generator.model.PlayableCharacters;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CharacterNameMapping {

	private static final String PREFIX = "character.name.mapping.";
	@Autowired
	private Environment env;

	private final Map<String, PlayableCharacters> mappingTable = new HashMap<>();

	@PostConstruct
	public void loadNameDefinitions() {
		for (PlayableCharacters character : PlayableCharacters.values()) {
			var name = character.name().toLowerCase();
			var key = PREFIX + name;
			var aliasNames = env.getProperty(key);

			// auto mapping
			mappingTable.put(name, character);

			if (nonNull(aliasNames)) {
				asList(aliasNames.split(",")).stream() //
						.map(String::trim) //
						.map(String::toLowerCase) //
						.forEach(n -> mappingTable.put(n, character));
			} else {
				log.warn("No name mapping found for {}!!! Please add {} to your properties!", name, key);
			}
		}
	}

	public Optional<PlayableCharacters> mapCharacter(String name) {
		return Optional.ofNullable(mappingTable.get(name.trim().toLowerCase()));
	}

}
