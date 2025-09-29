package com.jcca.web.asset.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.AssetImportRecord;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.entity.Org;
import com.jcca.web.asset.entity.Room;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:25 2021/7/8
 * @ Description:
 */

public interface AssetImportMapper extends BaseMapper<AssetImportRecord> {
    /*删除所有数据*/
    void delectAllAsset();

    /*获取所有资产*/
    List<AssetImportRecord> selectAllAsset();

    /*获取所有资产条数*/
    int countAll();

    /*根据名称查询组织是否存在*/
    List<Org> estimateOrg(@Param("orgName") String orgName);

    /*根据名称查询机房是否存在*/
    List<Room> estimateRoom(@Param("roomName") String roomName);

    /*根据名称查询机柜是否存在*/
    List<Cabinet> estimateCabinet(@Param("cabinetName") String cabinetName);

    /*判断组织机房机柜是否有满足从属关系*/
    List<Cabinet> estimateTreble(@Param("orgID") String orgID, @Param("roomName") String roomName, @Param("cabinetName") String cabinetName);

    /*判断组织和机房是否满足从属关系*/
    List<Room> estimateDouble(@Param("orgID") String orgID, @Param("roomName") String roomName);


}
