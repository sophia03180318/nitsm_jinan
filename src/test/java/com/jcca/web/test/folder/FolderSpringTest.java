package com.jcca.web.test.folder;


import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jcca.admin.system.service.SysFolderService;

/**
 * 文件夹测试
 * @author lyp
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FolderSpringTest {

	@Resource
	private SysFolderService folderServ;
	
	@Test
	public void test() {
		System.out.println(folderServ.getPids("1262986848062566401"));
	}
	
	
}
