package de.agiehl.games.dt.bggLoader;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableRetry
public class BggLoaderConfiguration {

	@Bean
	public RetryTemplate retryTemplate(@Value("${bgg.api.retry.backOffPeriod}") long backOffPeriode,
			@Value("${bgg.api.retry.maxAttempts}") int maxAttempts) {
		var retryTemplate = new RetryTemplate();

		var fixedBackOffPolicy = new FixedBackOffPolicy();
		fixedBackOffPolicy.setBackOffPeriod(backOffPeriode);
		retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

		var retryPolicy = new SimpleRetryPolicy();
		retryPolicy.setMaxAttempts(maxAttempts);
		retryTemplate.setRetryPolicy(retryPolicy);

		return retryTemplate;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.errorHandler(bggErrorHandler()).build();
	}

	/**
	 * ResponseErrorHandler which handle all != 200 as "error". BGG returns status
	 * code 202 (Accepted) if the request must be prepared.
	 * 
	 * @return
	 */
	protected ResponseErrorHandler bggErrorHandler() {
		return new DefaultResponseErrorHandler() {

			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				int rawStatusCode = response.getRawStatusCode();
				HttpStatus statusCode = HttpStatus.resolve(rawStatusCode);

				return !statusCode.equals(HttpStatus.OK);
			}
		};
	}

}
