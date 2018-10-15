package tw.elliot.lc.theonex.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataService {

	@Cacheable("level1")
	public String findLevel1Data(String id) {
		log.info("[{}] - Load data without cache for key[{}]", "level1", id);
		return "Joe";
	}

	@Cacheable("level2")
	public String findLevel2Data(String id) {
		log.info("[{}] - Load data without cache for key[{}]", "level2", id);
		return "Kevin";
	}

	@Cacheable("level100")
	public String findIncorrectLevel(String id) {
		log.info("[{}] - Load data without cache for key[{}]", "level100", id);
		return "Bad";
	}

	@CacheEvict(value = "level1", allEntries = true, beforeInvocation = true)
	public void cleanLevel1Cache() {
		log.info("Clean ");
	}
}
