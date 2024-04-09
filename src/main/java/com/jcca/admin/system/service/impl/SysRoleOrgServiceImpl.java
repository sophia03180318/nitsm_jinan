package com.jcca.admin.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysRoleOrgMapper;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysRoleOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.SysRoleOrgService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-13 10:42
 **/
@Service
public class SysRoleOrgServiceImpl extends ServiceImpl<SysRoleOrgMapper, SysRoleOrg> implements SysRoleOrgService {

    @Resource
    private SysRoleOrgMapper roleOrgMapper;
    @Resource
    private SysOrgService orgService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateRoleOrg(String roleId, List<String> orgIds) {
        QueryWrapper<SysRoleOrg> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        roleOrgMapper.delete(wrapper);

        if (orgIds != null) {
            orgIds.forEach(orgId -> {
                SysRoleOrg ro = new SysRoleOrg();
                ro.setRoleId(roleId);
                ro.setOrgId(orgId);
                roleOrgMapper.insert(ro);
            });
            return orgIds.size();
        }
        return 0;
    }

    /**
     * 根据org_id获取角色列表
     *
     * @param orgId
     * @return
     */
    @Override
    public List<String> findRoleIdsByOrgId(String orgId) {
        return roleOrgMapper.findRoleIdsByOrgId(orgId);
    }

    @Override
    public boolean getRoleByOrg(String roleId, String orgId) {
        List<SysRoleOrg> roleByOrg = roleOrgMapper.getRoleByOrg(roleId, orgId);
        return (ObjectUtil.isNotNull(roleByOrg) && !roleByOrg.isEmpty());
    }

    @Override
    public List<SysOrg> getRoleOrgListV2(String id) {
        // 获取指定角色组织资源
        List<SysOrg> orgList = orgService.getOrgsByRoleId(id);
        // 获取全部菜单列表
        List<SysOrg> list = orgService.getListBySortOk();
        // 融合两项数据
        List<SysOrg> resultList = new ArrayList<>();
        for (SysOrg org : list) {
            SysOrg o = new SysOrg();
            BeanUtils.copyProperties(org, o);
            for (SysOrg auth : orgList) {
                o.setRemark("");
                if (org.getId().equals(auth.getId())) {
                    o.setRemark("auth:true");
                    break;
                }
            }
            resultList.add(o);
        }
        return resultList;
    }
}