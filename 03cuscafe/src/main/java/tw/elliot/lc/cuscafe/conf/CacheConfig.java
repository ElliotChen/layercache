package tw.elliot.lc.cuscafe.conf;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public Map<String, CaffeineSpec> buildSpecMap() {
		Map<String, CaffeineSpec> specMap = new HashMap<>();

		String spec1 = "maximumSize=500,expireAfterAccess=10s";
		String spec2 = "maximumSize=500,expireAfterAccess=100s";

		specMap.put("layer01",CaffeineSpec.parse(spec1));
		specMap.put("layer02",CaffeineSpec.parse(spec2));

		return specMap;
	}

	@Bean
	public CacheManager buildCaffeineCacheManager(Map<String, CaffeineSpec> specMap) {

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		List<CaffeineCache> caches = new ArrayList<>();
		for (Map.Entry<String, CaffeineSpec> entry : specMap.entrySet()) {
			Caffeine<Object, Object> caffeine = Caffeine.from(entry.getValue());
			caches.add(new CaffeineCache(entry.getKey(), caffeine.build()));
		}
		cacheManager.setCaches(caches);

		return cacheManager;
	}
}
