package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysFile;

/**
 * @author hanwone
 * @date 2020-04-06 12:14:23
 **/
public interface SysFileService extends IService<SysFile> {


    /**
     * 根据文件SHA1获取文件信息
     *
     * @param fileSha1
     * @return
     */
    SysFile getBySha1(String fileSha1);

    /**
     * 根据文件ID获取网盘文件所在路径
     *
     * @param id     网盘文件ID
     * @param rootId 根文件夹ID
     * @return 网盘文件路径
     */
    String getPathById(String id, String rootId);
}
