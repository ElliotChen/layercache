package tw.elliot.lc.cuscafe.service;

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
	public void test() {
		String id = "id1";
		for (int i = 0; i < 3; i++) {
			log.info("call service for test cache layer01 [{}]", service.findLayer01Data(id));
		}

		id = "id2";
		for (int i = 0; i < 3; i++) {
			log.info("call service for test cache layer02 [{}]", service.findLayer02Data(id));
		}
	}

	@Test
	public void testIncorrectCacheName() {
		String id = "error";
		try {
			service.findIncorrectLayer(id);
			Assert.fail("Should throw exception!");
		} catch (Exception e) {
			log.info("Can't find layer100");
		}
	}

	@Test
	public void testCacheManger() {
		log.info("CacheManager is [{}]", this.cacheManager);
	}
}