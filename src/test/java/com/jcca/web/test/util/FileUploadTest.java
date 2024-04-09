package com.jcca.web.test.util;


import org.junit.Test;

import com.jcca.common.utils.file.FileUpload;

import lombok.extern.slf4j.Slf4j;

/**
 * 文件工具
 * @author lyp
 *
 */
@Slf4j
public class FileUploadTest {
	
	@Test
	public void testMove() {
		try {
			FileUpload.moveFile("C:\\Users\\lyp\\Documents\\1.docx", "C:\\Users\\lyp\\Desktop\\sam项目相关文档");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
	@Test
	public void testPreview() {
		
	}
	
}
