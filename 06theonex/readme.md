# Theonex

## Configuration 读取

### 读取自订的Caffeine设定

```
spring:
  cache:
    caffeine:
      levels:
      - key: level1
        spec: initialCapacity=5,maximumSize=100,expireAfterWrite=3s
        available: true

```

使用Spring 的 ConfigurationProperties，自订properties class

```
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

```

### 读取自订的Redis设定

```
spring:
  cache:
    redis:
      levels:
        - key: level1
          expire: 3
          delay: 2600
          available: true

```

使用Spring 的 ConfigurationProperties，自订properties class

```
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

```

此部份建议改用Spring内定的Redis Configuration
```
/**
* Entry expiration. By default the entries never expire.
*/
private Duration timeToLive;

/**
* Allow caching null values.
*/
private boolean cacheNullValues = true;

/**
* Key prefix.
*/
private String keyPrefix;

/**
* Whether to use the key prefix when writing to Redis.
*/
private boolean useKeyPrefix = true;
```

### EnableConfigurationProperties

使用Spring 的 ```@EnableConfigurationProperties``` 来注入configuration

```
@EnableConfigurationProperties({CacheConfig.CaffeineLevel.class, CacheConfig.RedisLevel.class})
public class CacheConfig {
	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	public CaffeineLevel caffeineLevel;

	@Autowired
	public RedisLevel redisLevel;
}
```

## 产生CacheManager

### SimpleCacheManager for Caffeine

由于[CaffeineCacheManager](https://github.com/spring-projects/spring-framework/blob/master/spring-context-support/src/main/java/org/springframework/cache/caffeine/CaffeineCacheManager.java)中，虽有```ConcurrentMap<String, Cache> cacheMap```可用来存于cache实例，但并未对外开放可以存入。
所以改以[SimpleCacheManager](https://github.com/spring-projects/spring-framework/blob/master/spring-context/src/main/java/org/springframework/cache/support/SimpleCacheManager.java)

主要由下列两部部份完成

```
/**
* 依application.yml中的设定产生CaffeineSpec
* @return
*/
@Bean
public Map<String, CaffeineSpec> buildCaffeineSpecMap() {}
	
/**
* 依传入的Map<key, CaffeineSpec>来产生Caffeine，并存放于SimpleCacheManager中
* @return
*/
@Bean
public SimpleCacheManager buildCaffeineCacheManager(Map<String, CaffeineSpec> specMap) {}
	
```

### RedisCacheManager for Redis
[RedisCacheManager](https://github.com/spring-projects/spring-data-redis/blob/master/src/main/java/org/springframework/data/redis/cache/RedisCacheManager.java)内有```Map<String, RedisCacheConfiguration> initialCacheConfiguration```可用来协助建立cache。
而其父类别[AbstractCacheManager] 则有```ConcurrentMap<String, Cache> cacheMap```存放Cache实例

所以从```RedisCacheManager```入手，由下列三部份处理

```

@Autowired
private RedisConnectionFactory redisConnectionFactory;

/**
* 依application.yml中的设定产生RedisCacheConfiguration
* @return
*/
@Bean
public Map<String, RedisCacheConfiguration> buildRedisCacheConfigMap() {}

/**
* 依传入的Map<key, RedisCacheConfiguration>来产生RedisCacheManager
* @return
*/
@Bean
public RedisCacheManager buildRedisCacheManager(Map<String, RedisCacheConfiguration> configurationMap) {}
```

### CompositeCacheManager as primary CacheManager

由于Spring 预设仅能有一个CacheManager，但我们前面已产生了```SimpleCacheManager```及```RedisCacheManager```.
所以必需加注```@Primary```.

```
	@Bean
	@Primary
	public CacheManager createCacheManager(SimpleCacheManager simpleCacheManager, RedisCacheManager redisCacheManager) {
		CompositeCacheManager cacheManager = new CompositeCacheManager(simpleCacheManager, redisCacheManager);
		cacheManager.setFallbackToNoOpCache(Boolean.FALSE);
		cacheManager.afterPropertiesSet();

		return  cacheManager;
	}
```

自此依Spring 的规范产生```CacheManager```,所以相关annotation如```@Cacheable```,```@CachePut```,```@CacheEvict```皆可正常使用。