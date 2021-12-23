package de.agiehl.games.dt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

import de.agiehl.games.dt.generator.GenerateHtml;
import de.agiehl.games.dt.services.FetchAndGenerateHtmlService;

@SpringBootApplication
@EnableCaching
public class DiceThroneProgressApplication implements CommandLineRunner {

	@Autowired
	private FetchAndGenerateHtmlService service;

	@Autowired
	private GenerateHtml generateHtml;

	public static void main(String[] args) {
		new SpringApplicationBuilder(DiceThroneProgressApplication.class).run(args).close();
	}

	@Override
	public void run(String... args) throws Exception {
		service.fetchAndPersitsAll();

		generateHtml.generateHtml();
	}

}
