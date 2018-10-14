package tw.elliot.lc.credis.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Bean
	public Map<String, RedisCacheConfiguration> buildCacheConfig() {
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

	@Bean
	public CacheManager buildRedisCacheManager(Map<String, RedisCacheConfiguration> configurationMap) {

		log.info("configurationMap size is [{}]", configurationMap.size());

		RedisCacheManager.RedisCacheManagerBuilder cmb = RedisCacheManager.builder(this.redisConnectionFactory);
		cmb.withInitialCacheConfigurations(configurationMap);

		cmb.disableCreateOnMissingCache();
		return cmb.build();
	}
}
