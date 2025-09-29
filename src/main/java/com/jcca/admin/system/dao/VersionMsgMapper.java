package com.jcca.admin.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.VersionMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 版本
 *
 * @author lyp
 */
@Mapper
public interface VersionMsgMapper extends BaseMapper<VersionMsg> {

    VersionMsg selectMaxVersion();

    /**
     * 查询更新版本文件总大小
     */
    Integer selectFileSize(String versionId);

    List<VersionMsg> getStatus();

    List<VersionMsg> queryList();
}
