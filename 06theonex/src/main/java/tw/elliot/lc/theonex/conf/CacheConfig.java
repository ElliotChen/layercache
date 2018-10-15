package tw.elliot.lc.theonex.conf;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
@EnableConfigurationProperties({CacheConfig.CaffeineLevel.class, CacheConfig.RedisLevel.class})
public class CacheConfig {
	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	public CaffeineLevel caffeineLevel;

	@Autowired
	public RedisLevel redisLevel;

	/**
	 * 依application.yml中的设定产生CaffeineSpec
	 * @return
	 */
	@Bean
	public Map<String, CaffeineSpec> buildCaffeineSpecMap() {
		Map<String, CaffeineSpec> specMap = new HashMap<>();

		log.info("CaffeineLevel config size is - [{}]",caffeineLevel.getLevels().size());

		for (CaffeineConfig config : caffeineLevel.getLevels()) {
			if (config.getAvailable()) {
				specMap.put(config.getKey(),CaffeineSpec.parse(config.getSpec()));
			}
		}

		return specMap;
	}

	/**
	 * 依传入的Map<key, CaffeineSpec>来产生Caffeine，并存放于SimpleCacheManager中
	 * @return
	 */
	@Bean
	public SimpleCacheManager buildCaffeineCacheManager(Map<String, CaffeineSpec> specMap) {
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

	/**
	 * 依application.yml中的设定产生RedisCacheConfiguration
	 * @return
	 */
	@Bean
	public Map<String, RedisCacheConfiguration> buildRedisCacheConfigMap() {
		Map<String, RedisCacheConfiguration> configurations = new HashMap<>();

		log.info("RedisLevel config size is - [{}]",redisLevel.getLevels().size());

		for (RedisConfig config : redisLevel.getLevels()) {
			log.info("RedisConfig is [{}]", config);
			if (config.getAvailable()) {
				RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig();
				cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(config.getExpire()));
				// config.getDelay();
				configurations.put(config.getKey(), cacheConfig);
			}
		}

		return configurations;
	}

	/**
	 * 依传入的Map<key, RedisCacheConfiguration>来产生RedisCacheManager
	 * @return
	 */
	@Bean
	public RedisCacheManager buildRedisCacheManager(Map<String, RedisCacheConfiguration> configurationMap) {
		log.info("Redis configurationMap size is [{}]", configurationMap.size());

		RedisCacheManager.RedisCacheManagerBuilder cmb = RedisCacheManager.builder(this.redisConnectionFactory);

		cmb.withInitialCacheConfigurations(configurationMap);
		cmb.disableCreateOnMissingCache();

		RedisCacheManager rcm = cmb.build();
		rcm.initializeCaches();

		return rcm;
	}

	@Bean
	@Primary
	public CacheManager createCacheManager(SimpleCacheManager simpleCacheManager, RedisCacheManager redisCacheManager) {
		CompositeCacheManager cacheManager = new CompositeCacheManager(simpleCacheManager, redisCacheManager);
		cacheManager.setFallbackToNoOpCache(Boolean.FALSE);
		cacheManager.afterPropertiesSet();

		return  cacheManager;
	}

	@ConfigurationProperties(prefix="spring.cache.caffeine")
	@Data
	static class CaffeineLevel {

		private List<CaffeineConfig> levels;
	}

	@Data
	static class CaffeineConfig {
		private String key;
		private String spec;
		private Boolean available;
	}

	@ConfigurationProperties(prefix="spring.cache.redis")
	@Data
	static class RedisLevel {
		private List<RedisConfig> levels;
	}

	@Data
	static class RedisConfig {
		private String key;
		private Integer expire;
		private Integer delay;
		private Boolean available;
	}
}


