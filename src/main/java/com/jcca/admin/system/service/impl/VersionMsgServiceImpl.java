package com.jcca.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.VersionMsgMapper;
import com.jcca.admin.system.entity.VersionMsg;
import com.jcca.admin.system.service.VersionMsgService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 版本电话
 *
 * @author lyp
 */
@Service
public class VersionMsgServiceImpl extends ServiceImpl<VersionMsgMapper, VersionMsg> implements VersionMsgService {

    @Resource
    private VersionMsgMapper versionMapper;

    @Override
    public VersionMsg queryLastVersion() {
        return versionMapper.selectMaxVersion();
    }

    @Override
    public Integer queryFileSize(String versionId) {
        return versionMapper.selectFileSize(versionId);
    }

    public Integer getStatus() {
        List<VersionMsg> list = versionMapper.getStatus();
        if (list.isEmpty()) {
            return 0;
        } else {
            return 1;
        }
    }

    ;

}
