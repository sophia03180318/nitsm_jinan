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

    List<String> findRoleIdsByOrgId(String orgId);

    List<SysRoleOrg> getRoleByOrg(String roleId, String orgId);

}
