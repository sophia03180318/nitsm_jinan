package com.jcca.web.test.ip;

import java.util.Objects;

import com.jcca.common.utils.EncryptUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jcca.web.asset.dao.AssetMapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.ip.service.NetWorkAddressService;

/**
 * 测试资产
 * @author lyp
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AssetTest {

	@Autowired
	private NetWorkAddressService ipservice; 
	@Autowired
	private AssetMapper assetMapper;


	public static void main(String[] args) {
		System.out.println(EncryptUtil.aesDecryptStr("e50751ccecf2fb0a135321f966e96bc8"));
	}
	
	@Test
	public void testAsset() {
	}
	
	@Test
	public void testIp() {
		Asset selectByIp = assetMapper.selectByIp("192.168.1.1");
		if(Objects.nonNull(selectByIp)) {
			System.out.println("ISNOTNULL");
		}else {
			System.out.println("ISNULL");
		}
	}
}
