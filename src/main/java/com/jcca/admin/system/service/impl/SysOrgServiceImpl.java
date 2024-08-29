package com.jcca.admin.system.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.biz.service.StationService;
import com.jcca.admin.system.dao.SysOrgMapper;
import com.jcca.admin.system.dao.SysRoleOrgMapper;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysRoleOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.shiro.util.ShiroUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hanwone
 * @date 2020-04-06 12:13
 **/
@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements SysOrgService {

    @Resource
    private SysOrgMapper sysOrgMapper;
    @Resource
    private SysRoleOrgMapper roleOrgMapper;
    @Resource
    private StationService stationService;

    /**
     * 根据父级组织ID获取本级全部组织
     *
     * @param pid   父组织ID
     * @param notId 需要排除的组织ID
     * @return 组织列表
     */
    @Override
    public List<SysOrg> getListByPid(String pid, String notId) {
        QueryWrapper<SysOrg> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", pid);
        if (!"0".equals(pid)) {
            wrapper.ne("id", notId);
        }
        wrapper.ne("status", StatusEnum.DELETE.getCode());
        wrapper.orderByAsc("sort");
        return sysOrgMapper.selectList(wrapper);
    }

    @Override
    public Byte getSortMax(String pid) {
        return sysOrgMapper.getSortMax(pid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(StatusEnum statusEnum, String id) {
        SysOrg org = sysOrgMapper.selectById(id);
        if (Objects.isNull(org)) {
            throw new ResultException(ResultEnum.ORG_NULL);
        }

        if (statusEnum == StatusEnum.DELETE) {
            // 删除角色组织关联关系
            QueryWrapper<SysRoleOrg> wrapper = new QueryWrapper<>();
            wrapper.eq("org_id", id);
            roleOrgMapper.delete(wrapper);

            // 如果是车站 删除车站配置
            if (org.getType() == OrgTypeConst.STATION) {
                stationService.removeById(id);
            }
        }
        org.setStatus(statusEnum.getCode());
        sysOrgMapper.updateById(org);
        return true;
    }

    @Override
    public List<SysOrg> getOrgsByRoleId(String roleId) {
        return sysOrgMapper.getOrgsByRoleId(roleId);
    }

    @Override
    public List<SysOrg> getListBySortOk() {
        QueryWrapper<SysOrg> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort");
        wrapper.eq("status", StatusEnum.OK.getCode());
        return sysOrgMapper.selectList(wrapper);
    }

    /**
     * 根据用户ID获取组织列表
     *
     * @param map
     * @return
     */
    @Override
    public List<SysOrg> getOrgsByUserId(Map<String, Object> map) {
        return sysOrgMapper.getOrgsByUserId(map);
    }

    @Override
    public List<SysOrg> getOrgsAsset(String userId, String watch) {
        if (userId.equals("1")) {
            return sysOrgMapper.getRootOrgsAsset(userId, watch);
        }
        return sysOrgMapper.getOrgsAsset(userId, watch);
    }

    /**
     * 根据组织ID获取所有子组织
     *
     * @param id
     * @return
     */
    @Override
    public Set<SysOrg> getChildrenById(String id) {
        QueryWrapper<SysOrg> wrapper = new QueryWrapper<>();
        wrapper.like("pids", "[" + id + "]");
        List<SysOrg> sysOrgs = sysOrgMapper.selectList(wrapper);
        return new HashSet<>(sysOrgs);
    }

    /**
     * 获取第一个有效中心组织
     *
     * @return
     */
    @Override
    public SysOrg getDefaultOrg() {
        List<SysOrg> orgs = ShiroUtil.getSubjectOrgs();
        if (Objects.isNull(orgs) || orgs.isEmpty()) {
            return null;
        }

        // 如果有中心 直接返回第一个
        for (SysOrg org : orgs) {
            if (org.getType() == OrgTypeConst.CENTER) {
                return org;
            }
        }

        // 如果没有中心 ,先根据最高级组织进行排序,然后返回排序第一的组织下属的车站 syt 2021/8/26

        //根据父级排序
        orgs = orgs.stream().sorted(Comparator.comparingInt(SysOrg::getType)).collect(Collectors.toList());
        // 若第一个父级组织直接是车站,那直接返回该组数据中sort第一的车站
        if (orgs.get(0).getType() == OrgTypeConst.STATION) {
            return orgs.stream().sorted(Comparator.comparing(SysOrg::getSort)).collect(Collectors.toList()).get(0);
        }

        for (int i = 0; i < orgs.size(); i++) {
            // 取到第一个父级组织
            String firstId1 = orgs.get(i).getId();

            // 根据取到的第一个父级组织(在这里应该是线一级别的组织)去获取其子集中的第一个组织
            List<SysOrg> orgChild = orgs.stream().filter(os -> os.getPid().equals(firstId1)).collect(Collectors.toList());

            if (CollectionUtil.isNotEmpty(orgChild)) {
                orgChild = orgChild.stream()
                        .filter(os -> os.getPid().equals(firstId1))
                        .sorted(Comparator.comparing(SysOrg::getSort))
                        .collect(Collectors.toList());
                for (SysOrg org : orgChild) {
                    if (org.getType() == OrgTypeConst.STATION) {
                        return org;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public SysOrg getDefaultOrg(Integer flag) {
        List<SysOrg> orgs = ShiroUtil.getSubjectOrgs();
        if (Objects.isNull(orgs) || orgs.isEmpty()) {
            return null;
        }

        // 如果需要返回路局
        if (flag != null && flag == OrgTypeConst.GROUP) {
            for (SysOrg org : orgs) {
                if (org.getType() == OrgTypeConst.GROUP) {
                    return org;
                }
            }
        }

        // 如果有中心 直接返回第一个
        for (SysOrg org : orgs) {
            if (org.getType() == OrgTypeConst.CENTER) {
                return org;
            }
        }

        // 如果没有中心 ,先根据最高级组织进行排序,然后返回排序第一的组织下属的车站 syt 2021/8/26

        //根据父级排序
        orgs = orgs.stream().sorted(Comparator.comparingInt(SysOrg::getType)).collect(Collectors.toList());
        // 若第一个父级组织直接是车站,那直接返回该组数据中sort第一的车站
        if (orgs.get(0).getType() == OrgTypeConst.STATION) {
            return orgs.stream().sorted(Comparator.comparing(SysOrg::getSort)).collect(Collectors.toList()).get(0);
        }

        for (int i = 0; i < orgs.size(); i++) {
            // 取到第一个父级组织
            String firstId1 = orgs.get(i).getId();

            // 根据取到的第一个父级组织(在这里应该是线一级别的组织)去获取其子集中的第一个组织
            List<SysOrg> orgChild = orgs.stream().filter(os -> os.getPid().equals(firstId1)).collect(Collectors.toList());

            if (CollectionUtil.isNotEmpty(orgChild)) {
                orgChild = orgChild.stream()
                        .filter(os -> os.getPid().equals(firstId1))
                        .sorted(Comparator.comparing(SysOrg::getSort))
                        .collect(Collectors.toList());
                for (SysOrg org : orgChild) {
                    if (org.getType() == OrgTypeConst.STATION) {
                        return org;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据资产ID获取 资产所属组织信息
     *
     * @param assetId
     * @return
     */
    @Override
    public SysOrg getByAssetId(String assetId) {

        return sysOrgMapper.getByAssetId(assetId);
    }

    /**
     * 根据用户名获取用户所管理的所有组织
     *
     * @param username
     * @return
     */
    @Override
    public List<SysOrg> getOrgsByUsername(String username) {
        return sysOrgMapper.getOrgsByUsername(username);
    }

    @Override
    public List<SysOrg> findAllByName(String content) {
        QueryWrapper<SysOrg> queryWrapper = new QueryWrapper<SysOrg>();
        queryWrapper.eq("title", content);
        queryWrapper.eq("status", 1);
        return sysOrgMapper.selectList(queryWrapper);
    }

    @Override
    public List<String> getOrgByName() {
        return sysOrgMapper.getOrgByName();
    }

    @Override
    public List<String> getIdByline(String id) {
        return sysOrgMapper.getIdByline(id);
    }

    @Override
    public List<String> getStationOrgIdByLineId(String lineId) {
        if (StrUtil.isEmpty(lineId)) {
            return new ArrayList<>();
        }
        return sysOrgMapper.getStationOrgIdByLineId(lineId);
    }

    @Override
    public String getParentPathName(String id) {
        SysOrg sysOrg = this.getById(id);
        if (sysOrg == null) {
            return null;
        }
        String pids = sysOrg.getPids();
        pids = pids.replace("[", "");
        pids = pids.replace("]", "");
        List<String> pidList = StrUtil.split(pids, ',');
        QueryWrapper<SysOrg> qw = new QueryWrapper<>();
        qw.in("ID", pidList).orderByAsc("ID");
        List<SysOrg> orgList = this.list(qw);
        if (CollUtil.isEmpty(orgList)) {
            return null;
        }
        StringBuilder orgPathName = new StringBuilder();
        orgList.forEach(e -> {
            orgPathName.append(e.getTitle()).append("-");
        });

        return StrUtil.removeSuffix(orgPathName.toString(), "-");
    }

    /**
     * 根据组织类型查找组织
     *
     * @param orgType 组织类型
     * @return 组织列表
     */
    @Override
    public List<SysOrg> getListByOrgType(byte orgType) {
        QueryWrapper<SysOrg> queryWrapper = Wrappers.query();
        queryWrapper.eq("type", orgType);
        queryWrapper.eq("status", StatusEnum.OK.getCode());

        return sysOrgMapper.selectList(queryWrapper);
    }
}