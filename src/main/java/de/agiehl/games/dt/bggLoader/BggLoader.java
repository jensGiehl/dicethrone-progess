package de.agiehl.games.dt.bggLoader;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BggLoader {

	private final RestTemplate template;

	private final RetryTemplate retryTemplate;

	/**
	 * Fetches data from URI and map it to the target class
	 * 
	 * @param <T>
	 * @param uriString
	 * @param targetClass
	 * @return
	 */
	public <T> T fetchData(String uriString, Class<T> targetClass) {
		return retryTemplate.execute(new RetryCallback<T, UnknownHttpStatusCodeException>() {
			@Override
			public T doWithRetry(RetryContext context) throws UnknownHttpStatusCodeException {
				log.info("Fetching data from {} (Retry count: {})", uriString, context.getRetryCount());
				return template.getForEntity(uriString, targetClass).getBody();
			}
		});
	}

}
