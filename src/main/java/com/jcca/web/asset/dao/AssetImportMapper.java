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
    @Delete("delete from asset_import_record")
    void delectAllAsset();

    /*获取所有资产*/
    @Select("select * from asset_import_record")
    List<AssetImportRecord> selectAllAsset();

    /*获取所有资产条数*/
    @Select("select count(*) rom asset_import_record")
    int countAll();

    /*根据名称查询组织是否存在*/
    @Select("select * from sys_org where title = #{orgName} and STATUS=1")
    List<Org> estimateOrg(@Param("orgName") String orgName);

    /*根据名称查询机房是否存在*/
    @Select("select * from room where name = #{roomName}")
    List<Room> estimateRoom(@Param("roomName") String roomName);

    /*根据名称查询机柜是否存在*/
    @Select("select * from cabinet where name = #{cabinetName}")
    List<Cabinet> estimateCabinet(@Param("cabinetName") String cabinetName);

    /*判断组织机房机柜是否有满足从属关系*/
    @Select("select * from CABINET c where c.ROOM_ID =(select id from ROOM where ORG_ID=#{orgID} and name=#{roomName}) and c.name =#{cabinetName}")
    List<Cabinet> estimateTreble(@Param("orgID") String orgID, @Param("roomName") String roomName, @Param("cabinetName") String cabinetName);

    /*判断组织和机房是否满足从属关系*/
    @Select("select * from ROOM  where NAME=#{roomName} and ORG_ID=#{orgID}")
    List<Room> estimateDouble(@Param("orgID") String orgID, @Param("roomName") String roomName);


}
