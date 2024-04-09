package com.jcca.web.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.AppListUtils;
import com.jcca.web.common.dao.BizManageMapper;
import com.jcca.web.common.entity.BizManage;
import com.jcca.web.common.service.BizManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 业务模块公共服务
 *
 * @author hanwone
 * @date 2020-05-29 10:11
 **/
@Service
public class BizManageServiceImpl extends ServiceImpl<BizManageMapper, BizManage> implements BizManageService {

    /**
     * 保存biz 和当前用户组织
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveByBizAndOrg(String bizId, Integer bizType) {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        List<BizManage> bizManages = new ArrayList<BizManage>();
        for (String orgId : orgIds) {
            if ("x".equals(orgId)) {
                continue;
            }
            BizManage bizManage = new BizManage();
            bizManage.setManageId(orgId);
            bizManage.setBizId(bizId);
            bizManage.setBizType(bizType);

            bizManages.add(bizManage);
        }
        return this.saveBatch(bizManages);
    }

    /**
     * 保存组织-业务ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveByBizAndOrg(String orgId, String bizId, Integer bizType) {
        BizManage bizManage = new BizManage();
        bizManage.setManageId(orgId);
        bizManage.setBizId(bizId);
        bizManage.setBizType(bizType);

        return this.save(bizManage);
    }

    /**
     * 保存数据
     *
     * @param bizId   业务数据ID
     * @param bizType 业务类型
     * @return 是否保存成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(String bizId, int bizType) {
        List<String> roleIds = ShiroUtil.getSubjectRoleIds();

        List<BizManage> bizManages = new ArrayList<>();
        for (String roleId : roleIds) {
            if ("x".equals(roleId)) {
                continue;
            }
            BizManage bizManage = new BizManage();
            bizManage.setManageId(roleId);
            bizManage.setBizId(bizId);
            bizManage.setBizType(bizType);

            bizManages.add(bizManage);
        }

        return this.saveBatch(bizManages);
    }

    /**
     * 根据业务ID和业务类型删除数据
     *
     * @param bizId   业务数据ID
     * @param bizType 业务类型
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(String bizId, int bizType) {
        QueryWrapper<BizManage> query = Wrappers.query();
        query.eq("biz_id", bizId);
        query.eq("biz_type", bizType);
        return this.remove(query);
    }

    /**
     * 根据业务类型查询数据
     *
     * @param bizType 业务类型
     * @return 业务ID列表
     */
    @Override
    public List<String> list(int bizType) {
        List<String> roleIds = ShiroUtil.getSubjectRoleIds();
        roleIds.add("x");

        QueryWrapper<BizManage> query = Wrappers.query();
        query.eq("biz_type", bizType);

        List<List<String>> inSplit = AppListUtils.inSplit(roleIds, 900);

        boolean onces = true;
        Consumer<QueryWrapper<BizManage>> consumer = null;
        for (List<String> list : inSplit) {
            if (onces) {
                consumer = wrapper -> wrapper.in("manage_id", list);
                onces = false;
            } else {
                Consumer<? super QueryWrapper<BizManage>> after = wrapper -> wrapper.or().in("manage_id", list);
                consumer = consumer.andThen(after);
            }
        }

        if (Objects.nonNull(consumer)) {
            query.and(consumer);
        }

        List<BizManage> bizManages = this.list(query);
        List<String> collect = bizManages.stream().map(BizManage::getBizId).collect(Collectors.toList());
        collect.add("x");
        return collect;
    }

    /**
     * 业务类型和业务ID查询
     */
    @Override
    public List<String> listByBizId(String bizId, Integer bizType) {
        List<String> roleIds = ShiroUtil.getSubjectRoleIds();
        QueryWrapper<BizManage> query = Wrappers.query();
        query.eq("BIZ_ID", bizId);
        query.eq("biz_type", bizType);

        List<List<String>> inSplit = AppListUtils.inSplit(roleIds, 900);

        boolean onces = true;
        Consumer<QueryWrapper<BizManage>> consumer = null;
        for (List<String> list : inSplit) {
            if (onces) {
                consumer = wrapper -> wrapper.in("manage_id", list);
                onces = false;
            } else {
                Consumer<? super QueryWrapper<BizManage>> after = wrapper -> wrapper.or().in("manage_id", list);
                consumer = consumer.andThen(after);
            }
        }

        if (Objects.nonNull(consumer)) {
            query.and(consumer);
        }

        List<BizManage> bizManages = this.list(query);
        List<String> collect = bizManages.stream().map(BizManage::getBizId).collect(Collectors.toList());
        return collect;
    }

    /**
     * 业务类型和业务ID查询
     */
    @Override
    public List<BizManage> listObjByBizId(String bizId, Integer bizType) {
        List<String> roleIds = ShiroUtil.getSubjectRoleIds();
        QueryWrapper<BizManage> query = Wrappers.query();
        query.eq("BIZ_ID", bizId);
        query.eq("biz_type", bizType);

        List<List<String>> inSplit = AppListUtils.inSplit(roleIds, 900);

        boolean onces = true;
        for (List<String> list : inSplit) {
            if (onces) {
                query.in("manage_id", list);
                onces = false;
            } else {
                query.or().in("manage_id", list);
            }
        }

        List<BizManage> bizManages = this.list(query);
        return bizManages;
    }

    @Override
    public List<String> listByBizAndOrg(Integer bizType) {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        orgIds.add("x");
        QueryWrapper<BizManage> query = Wrappers.query();
        query.eq("biz_type", bizType);
        query.in("manage_id", orgIds);
        List<BizManage> bizManages = this.list(query);
        List<String> collect = bizManages.stream().map(BizManage::getBizId).collect(Collectors.toList());
        collect.add("x");
        return collect;
    }
}