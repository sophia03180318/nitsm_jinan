package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysFolder;

import java.util.List;

/**
 * 文件夹
 *
 * @author lyp
 */
public interface SysFolderService extends IService<SysFolder> {

    /**
     * 查询文件夹的Pid 列表
     *
     * @param pid 当前目录的上级目录
     * @return pids
     */
    String getPids(String pid);

    /**
     * 创建文件夹
     *
     * @param folder 文件夹
     */
    void createFolder(SysFolder folder) throws Exception;

    /**
     * 根据文件夹类型获取所有文件夹ID
     *
     * @param category 文件夹类型
     * @return id列表
     */
    List<String> getAllFolderId(Byte category);
}
