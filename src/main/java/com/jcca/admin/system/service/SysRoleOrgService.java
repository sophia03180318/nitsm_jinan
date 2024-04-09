package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysRoleOrg;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-13 10:42:26
 **/
public interface SysRoleOrgService extends IService<SysRoleOrg> {

    /**
     * 更新角色组织关系
     *
     * @param roleId
     * @param orgIds
     * @return
     */
    int updateRoleOrg(String roleId, List<String> orgIds);


    /**
     * 根据org_id获取角色列表
     *
     * @param orgId
     * @return
     */
    List<String> findRoleIdsByOrgId(String orgId);

    boolean getRoleByOrg(String roleId, String orgId);

    List<SysOrg> getRoleOrgListV2(String id);
}
