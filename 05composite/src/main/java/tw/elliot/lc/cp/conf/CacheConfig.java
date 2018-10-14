package tw.elliot.lc.cp.conf;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {
	@Autowired
	private RedisConnectionFactory redisConnectionFactory;


	protected Map<String, CaffeineSpec> buildCaffeineSpecMap() {
		Map<String, CaffeineSpec> specMap = new HashMap<>();

		String spec1 = "maximumSize=500,expireAfterAccess=10s";
		String spec2 = "maximumSize=500,expireAfterAccess=100s";

		specMap.put("layer01",CaffeineSpec.parse(spec1));
		specMap.put("layer02",CaffeineSpec.parse(spec2));

		return specMap;
	}

	protected SimpleCacheManager buildCaffeineCacheManager() {
		Map<String, CaffeineSpec> specMap = this.buildCaffeineSpecMap();
		log.info("Caffeine specMap size is [{}]", specMap.size());

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		List<CaffeineCache> caches = new ArrayList<>();
		for (Map.Entry<String, CaffeineSpec> entry : specMap.entrySet()) {
			Caffeine<Object, Object> caffeine = Caffeine.from(entry.getValue());
			caches.add(new CaffeineCache(entry.getKey(), caffeine.build()));
		}
		cacheManager.setCaches(caches);

		return cacheManager;
	}

	protected Map<String, RedisCacheConfiguration> buildRedisCacheConfigMap() {
		Map<String, RedisCacheConfiguration> configurations = new HashMap<>();

		RedisCacheConfiguration layer01 = RedisCacheConfiguration.defaultCacheConfig();
		layer01 = layer01.entryTtl(Duration.ofMillis(10000));
		layer01 = layer01.prefixKeysWith("ppp");


		RedisCacheConfiguration layer02 = RedisCacheConfiguration.defaultCacheConfig();
		layer02 = layer02.entryTtl(Duration.ofMillis(20000));


		configurations.put("layer01", layer01);
		configurations.put("layer02", layer02);

		return configurations;
	}

	protected RedisCacheManager buildRedisCacheManager() {
		Map<String, RedisCacheConfiguration> configurationMap = this.buildRedisCacheConfigMap();
		log.info("Redis configurationMap size is [{}]", configurationMap.size());

		RedisCacheManager.RedisCacheManagerBuilder cmb = RedisCacheManager.builder(this.redisConnectionFactory);

		cmb.withInitialCacheConfigurations(configurationMap);
		cmb.disableCreateOnMissingCache();

		RedisCacheManager rcm = cmb.build();
		rcm.initializeCaches();

		return rcm;
	}

	@Bean
	public CacheManager createCacheManager() {

		CompositeCacheManager cacheManager = new CompositeCacheManager(this.buildCaffeineCacheManager(), this.buildRedisCacheManager());
		cacheManager.setFallbackToNoOpCache(Boolean.FALSE);
		cacheManager.afterPropertiesSet();

		return  cacheManager;
	}
}
