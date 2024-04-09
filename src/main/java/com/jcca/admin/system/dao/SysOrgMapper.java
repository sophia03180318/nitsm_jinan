package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.common.bean.constant.StatusConst;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2020-04-06 12:13:28
 **/
public interface SysOrgMapper extends BaseMapper<SysOrg> {


    /**
     * 获取最大的排序数
     *
     * @return
     */
    @Select(value = "select max(sort) from sys_menu m where m.pid = #{pid} and m.status <> " + StatusConst.DELETE)
    Byte getSortMax(String pid);

    /**
     * 根据角色ID获取组织列表
     *
     * @param roleId
     * @return
     */
    List<SysOrg> getOrgsByRoleId(String roleId);

    /**
     * 根据用户ID获取组织列表
     *
     * @param map
     * @return
     */
    List<SysOrg> getOrgsByUserId(Map<String, Object> map);

    /**
     * 查询用户组织资产
     * @param userId 用户ID
     * @param watch 传入会根据此条件过滤  不传则不过滤
     * @return
     */
    List<SysOrg> getOrgsAsset(String userId,String watch);

    /**
     * 获取组织
     * @param userId  用户ID
     * @param watch  传入会根据此条件过滤  不传则不过滤
     * @return
     */
    List<SysOrg> getRootOrgsAsset(String userId,String  watch);

    /**
     * 根据资产ID获取 资产所属组织信息
     *
     * @param assetId
     * @return
     */
    SysOrg getByAssetId(String assetId);

    /**
     * 根据用户名获取用户所管理的所有组织
     *
     * @param username
     * @return
     */
    List<SysOrg> getOrgsByUsername(String username);

    /*
     * 组织下
     * */
    List<String> getTitleByOrgId(String pId);

    @Select(value = "select TITLE  from SYS_ORG where status=1")
    List<String> getOrgByName();

    @Select(value = "select id from sys_org where pid =#{id}")
    List<String> getIdByline(String id);

    /**
     * 查询线下
     * @param lineId
     * @return
     */
    @Select(value = "select id from sys_org where pid = #{id} and TYPE = 4")
    List<String> getStationOrgIdByLineId(String lineId);
}
