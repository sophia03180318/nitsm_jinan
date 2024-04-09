package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysRoleOrg;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-13 10:42:26
 **/
public interface SysRoleOrgMapper extends BaseMapper<SysRoleOrg> {

    @Select("select role_id from sys_role_org where org_id = #{orgId}")
    List<String> findRoleIdsByOrgId(String orgId);

    @Select("select * from sys_role_org where role_id= #{roleId} and org_id = #{orgId}")
    List<SysRoleOrg> getRoleByOrg(String roleId, String orgId);

}
