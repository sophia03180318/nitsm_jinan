package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.common.enums.StatusEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hanwone
 * @date 2020-04-06 12:13:28
 **/
public interface SysOrgService extends IService<SysOrg> {


    /**
     * 根据组织PID获取父级组织列表
     *
     * @param pid
     * @param notId
     * @return
     */
    List<SysOrg> getListByPid(String pid, String notId);

    /**
     * 根据PID获取最大排序数
     *
     * @param pid
     * @return
     */
    Byte getSortMax(String pid);

    /**
     * 根据ID更新组织状态
     *
     * @param statusEnum
     * @param id
     * @return
     */
    boolean updateStatus(StatusEnum statusEnum, String id);

    /**
     * 根据角色ID获取组织列表
     *
     * @param roleId
     * @return
     */
    List<SysOrg> getOrgsByRoleId(String roleId);

    /**
     * 获取状态正常的组织列表
     *
     * @return
     */
    List<SysOrg> getListBySortOk();

    /**
     * 根据用户ID获取组织列表
     *
     * @param map
     * @return
     */
    List<SysOrg> getOrgsByUserId(Map<String, Object> map);

    /**
     * 查询组织  资产 列表
     * @param userId
     * @param watch 传入会根据此条件筛选 不传不筛选
     * @return
     */
    List<SysOrg> getOrgsAsset(String userId,String watch);

    /**
     * 根据组织ID获取所有子组织
     *
     * @param id
     * @return
     */
    Set<SysOrg> getChildrenById(String id);

    /**
     * 获取第一个有效中心组织
     *
     * @return
     */
    SysOrg getDefaultOrg();

    /**
     * 获取第一个有效中心组织
     *
     * @param flag 做为西宁线路图组织结构是返回路局ID还是中心ID的标记。
     *             0返回中心ID，1返回路局ID
     * @return
     */
    SysOrg getDefaultOrg(Integer flag);

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

    /**
     * 通过组织名称查询组织
     *
     * @param content
     * @return
     */
    List<SysOrg> findAllByName(String content);

    /*
     *  获取所有组织名称
     * */
    List<String> getOrgByName();

    /**
     * 线下所有的站id
     * 此方法不知道谁写的  查出来的是下一级的ID并不是传入线ID返回所有车站ID
     * 慎用！！！
     */
    List<String> getIdByline(String id);

    /**
     * 查询线下所有的车站ID列表
     * @param lineId
     * @return
     */
    List<String> getStationOrgIdByLineId(String lineId);

    /**
     * 根据当前组织结构id获取父组织路名称
     *
     * @param id
     * @return
     */
    String getParentPathName(String id);

    /**
     * 根据组织类型查找组织
     *
     * @param orgType
     * @return 组织列表
     */
    List<SysOrg> getListByOrgType(byte orgType);
}
