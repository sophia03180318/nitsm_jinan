package com.jcca.web.broken.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysFileMapper;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.web.broken.dao.BrokenRecordFileMapper;
import com.jcca.web.broken.entity.BrokenRecord;
import com.jcca.web.broken.entity.BrokenRecordFile;
import com.jcca.web.broken.service.BrokenRecordFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 故障记录文件
 *
 * @author lyp
 */
@Service
public class BrokenRecordFileServiceImpl extends ServiceImpl<BrokenRecordFileMapper, BrokenRecordFile> implements BrokenRecordFileService {

    @Resource
    private SysFileMapper fileMapper;
    @Resource
    private BrokenRecordFileMapper brokenFileMapper;


    @Transactional
    @Override
    public void addFile(SysFile sysFile, BrokenRecord broken) throws Exception {
        fileMapper.insert(sysFile);
        BrokenRecordFile file = new BrokenRecordFile();
        file.setBrokenRecordId(broken.getId());
        file.setSysFileId(sysFile.getId());
        brokenFileMapper.insert(file);

    }

}
