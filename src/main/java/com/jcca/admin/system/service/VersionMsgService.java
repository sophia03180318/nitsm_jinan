package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.VersionMsg;


/**
 * 版本信息
 *
 * @author lyp
 */
public interface VersionMsgService extends IService<VersionMsg> {

    /**
     * 查询当前后上传的版本
     *
     * @return
     */
    VersionMsg queryLastVersion();

    /**
     * 查询更新版本的文件总大小
     */
    Integer queryFileSize(String versionId);

    Integer getStatus();

}
