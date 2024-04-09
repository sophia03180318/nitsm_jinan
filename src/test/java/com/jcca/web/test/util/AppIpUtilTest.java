package com.jcca.web.test.util;

import com.jcca.web.ip.util.AppIpUtil;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ip工具测试
 * @author lyp
 *
 */
public class AppIpUtilTest {

	@SneakyThrows
	@Test
	public void testGetIp() throws Exception {
		List<String> ipList = AppIpUtil.getIPList("192.168.0.1", "255.255.255.240");
		System.out.println("ip共计："+ipList.size());
		for (String ip : ipList) {
			System.out.println(ip);
		}
	}
	@Test
	public void testResourceBundle(){

		ResourceBundle rb=ResourceBundle.getBundle("message", Locale.getDefault() );
		System.out.println(rb);

	}
	
	@Test
	public void testGetMask() {
		new Date();
		System.out.println(AppIpUtil.getMaskByMaskBit(24));
	}
	
}
