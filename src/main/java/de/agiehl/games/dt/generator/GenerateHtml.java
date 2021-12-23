package de.agiehl.games.dt.generator;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import de.agiehl.games.dt.generator.model.PlayableCharacters;
import de.agiehl.games.dt.generator.model.PlayerData;
import de.agiehl.games.dt.generator.model.PlayerData.PlayedCharacter;
import de.agiehl.games.dt.persistents.PersistentsService;
import de.agiehl.games.dt.persistents.model.CharacterCount;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GenerateHtml {

	@Autowired
	private PersistentsService persistens;

	@Value("${output.html.folder}")
	private String outputFolder;

	@Value("${output.html.filename}")
	private String outputFilename;

	@Value("${bgg.user.url}")
	private String bggUserUrl;

	public void generateHtml() throws IOException {

		var context = new Context();

		context.setVariable("bggUserUrl", bggUserUrl);
		context.setVariable("lastUpdate",
				LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));

		var playerWithoutBggUsername = persistens.getDistinctPlayerWithoutBggUsername();
		log.info("Found {} non BGG User Player", playerWithoutBggUsername.size());
		var playerModelWithoutBggUsername = playerWithoutBggUsername.stream()
				.map(player -> toModelForNonBggUser(player)).toList();

		var distinctUsername = persistens.getDistinctPlayerWithBggUsername();
		log.info("Found {} BGG User Player", playerWithoutBggUsername.size());
		var playerModelWithBggUsername = distinctUsername.stream().map(player -> toModel(player)).toList();

		List<PlayerData> allPlayerModels = new ArrayList<>();
		allPlayerModels.addAll(playerModelWithoutBggUsername);
		allPlayerModels.addAll(playerModelWithBggUsername);
		allPlayerModels.sort(comparingInt(PlayerData::getTotalPlays).reversed());
		context.setVariable("player", allPlayerModels);

		var characterPlayCount = persistens.getCharacterPlays();
		for (PlayableCharacters character : characterPlayCount.keySet()) {
			context.setVariable("play_" + character.name(), characterPlayCount.get(character));
		}

		var allPlayableCharacters = asList(PlayableCharacters.values()).stream()//
				.map(PlayableCharacters::name)//
				.sorted() //
				.toList();
		context.setVariable("characters", allPlayableCharacters);

		var templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setSuffix(".html");
		templateResolver.setPrefix("templates/");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding("UTF-8");

		var templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);

		var generatedHtml = templateEngine.process("overview", context);
		Path outputfile = Path.of(outputFolder, outputFilename);
		Files.writeString(outputfile, generatedHtml);
		log.info("File written: {}", outputfile.toAbsolutePath());
	}

	private PlayerData toModel(String bggUsername) {
		var name = persistens.getNamesForBggUsername(bggUsername).stream().sorted().collect(joining(", "));
		var player = new PlayerData(name, bggUsername);

		List<CharacterCount> playedCharacters = persistens.getPlayedCharactersForUsername(bggUsername);
		log.info("Found {} different characters for player with username {}", playedCharacters.size(), bggUsername);
		for (PlayableCharacters pc : PlayableCharacters.values()) {
			var ch = PlayedCharacter.builder()//
					.charachter(pc) //
					.lastPlayedAt(persistens.getLastPlayedDateForUsernameAndCharacter(bggUsername, pc)) //
					.totalPlayed(persistens.countPlayesForUserAndCharacter(bggUsername, pc)) //
					.winCount(persistens.countWinByUsernameAndCharacter(bggUsername, pc)) //
					.build();
			player.addPlayedCharacter(ch);
		}

		player.getPlayedCharacters().sort(comparingInt(PlayedCharacter::getTotalPlayed).reversed()
				.thenComparing(pc -> pc.getCharachter().name()));

		return player;
	}

	private PlayerData toModelForNonBggUser(String playername) {
		var player = new PlayerData(playername, null);

		List<CharacterCount> playedCharactersForPlayername = persistens.getPlayedCharactersForPlayername(playername);
		log.info("Found {} different characters for player {}", playedCharactersForPlayername.size(), playername);
		for (PlayableCharacters pc : PlayableCharacters.values()) {
			var ch = PlayedCharacter.builder()//
					.charachter(pc) //
					.lastPlayedAt(persistens.getLastPlayedDateForPlayernameAndCharacter(playername, pc)) //
					.totalPlayed(persistens.countPlayesForNameAndCharacter(playername, pc)) //
					.winCount(persistens.countWinByNameAndCharacter(playername, pc)) //
					.build();
			player.addPlayedCharacter(ch);
		}

		player.getPlayedCharacters().sort(comparingInt(PlayedCharacter::getTotalPlayed).reversed()
				.thenComparing(pc -> pc.getCharachter().name()));

		return player;
	}

}
