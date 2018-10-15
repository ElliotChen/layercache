package tw.elliot.lc.theonex.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DataServiceTest {

	@Autowired
	private DataService service;

	@Autowired
	private CacheManager cacheManager;
	@Test
	public void testCacheable() {
		String id = "id1";
		for (int i = 0; i < 3; i++) {
			log.info("call service for test cache layer01 [{}]", service.findLevel1Data(id));
		}

		id = "id2";
		for (int i = 0; i < 3; i++) {
			log.info("call service for test cache layer02 [{}]", service.findLevel2Data(id));
		}
	}

	@Test
	public void testIncorrectCacheName() {
		String id = "error";
		try {
			service.findIncorrectLevel(id);
			Assert.fail("Should throw exception!");
		} catch (Exception e) {
			log.info("Can't find layer100");
		}
	}

	@Test
	public void testCacheManger() {
		log.info("CacheManager is [{}]", this.cacheManager);
	}


	@Test
	public void testCleanCache() {
		service.cleanLevel1Cache();
	}
}