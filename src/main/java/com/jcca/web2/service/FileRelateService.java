package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.FileRelate;

/**
 * @author HanHW
 * @description 维护手册2.1
 * @className FileRelateService
 * @date 2025/3/27 16:38
 * @since 2.1.5.0
 */
public interface FileRelateService extends IService<FileRelate> {

    FileRelate getByFileId(String fileId);
}
