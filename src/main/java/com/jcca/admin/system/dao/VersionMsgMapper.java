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

    @Select("select * from M_VERSION_MSG where id = (select max(id) from M_VERSION_MSG)")
    VersionMsg selectMaxVersion();

    /**
     * 查询更新版本文件总大小
     */
    @Select("select f.FILE_SIZE from  M_VERSION_MSG v join SYS_FILE f on v.SYS_FILE_ID=f.ID where v.ID=#{versionId}")
    Integer selectFileSize(String versionId);

    @Select("select * from M_STATION_VERSION_LOG where status in('AWAIT_UPLOADING','UPLOADING','UPDATEING')")
    List<VersionMsg> getStatus();

    @Select("select * from M_VERSION_MSG order by CREATE_DATE DESC")
    List<VersionMsg> queryList();
}
