package com.jcca.web.test.tmp;

import lombok.extern.slf4j.Slf4j;

import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ValidatorUtils;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.asset.controller.bean.ThresholdProcessAddReq;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.topo.service.NetworkAssetMapService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * 测试资产
 *
 * @author lyp
 *
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ThreholdTest {

	@Resource
	private RedisService redisService;
	@Value("${app.tmp.ui:*}")
	private String ui;
	@Value("${server.port}")
	private String port;
	@Resource
	private NetworkAssetMapService mapServ;

	@Resource(name = "redisTemplate")
	private RedisTemplate redisTemplate;
	@Resource(name = "redisTransactionTemplate")
	private RedisTemplate redisTransactionTemplate;

	@Resource
	private AlarmRepositoryService alarmRepoServ;

	@Test
	public void dbChange() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				redisTransactionTemplate.multi();
				ValueOperations<Serializable, Object> operations = redisTransactionTemplate.opsForValue();
				operations.set("A", "A");
				System.out.println("执行A结束");
			}
		};

		Thread thread2 = new Thread() {
			@Override
			public void run() {
				redisTransactionTemplate.exec();
				System.out.println("执行Thread2结束");
			}
		};
		thread.start();
		thread2.start();
	}

	@Test
	public void testAsset() {
		ThresholdProcessAddReq req = new ThresholdProcessAddReq();
		req.setThresholdCpu("0");
		req.setThresholdMemory("0.0");
		req.setAssetId("1211212121");
		req.setProcessId("sjaixjia");
		req.setProcessName("xxxxx");

		String validateReq = ValidatorUtils.validateReq(req);
		System.out.println("输出校验结果" + validateReq);

	}

	@Test
	public void testThread() {
		redisService.set("COLLECT_MASTER_URL", "192.168.53.22:9999");
		BusinessGetSnmpResultReq req = new BusinessGetSnmpResultReq();
		req.setIp("192.168.62.2");
		req.setCommunity("cisco");
		try {
			mapServ.beginSeach(req);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		};
	}

	@Test
	public void dictTest() {
		String keyValue = DictUtil.keyValue("ALARM_LEVEL", "1");
		System.out.println(keyValue);
	}
	
}
