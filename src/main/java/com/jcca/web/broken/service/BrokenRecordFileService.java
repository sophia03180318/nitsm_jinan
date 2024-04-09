package com.jcca.web.broken.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.web.broken.entity.BrokenRecord;
import com.jcca.web.broken.entity.BrokenRecordFile;

/**
 * 维修记录文件
 *
 * @author lyp
 */
public interface BrokenRecordFileService extends IService<BrokenRecordFile> {

    /**
     * 添加维修记录
     *
     * @param sysFile
     * @param broken
     */
    void addFile(SysFile sysFile, BrokenRecord broken) throws Exception;

}
