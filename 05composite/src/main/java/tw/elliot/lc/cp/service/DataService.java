package tw.elliot.lc.cp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataService {

	@Cacheable("layer01")
	public String findLayer01Data(String id) {
		log.info("[{}] - Load data without cache for key[{}]", "layer01", id);
		return "Joe";
	}

	@Cacheable("layer02")
	public String findLayer02Data(String id) {
		log.info("[{}] - Load data without cache for key[{}]", "layer02", id);
		return "Kevin";
	}

	@Cacheable("layer100")
	public String findIncorrectLayer(String id) {
		log.info("[{}] - Load data without cache for key[{}]", "layer100", id);
		return "Bad";
	}
}
