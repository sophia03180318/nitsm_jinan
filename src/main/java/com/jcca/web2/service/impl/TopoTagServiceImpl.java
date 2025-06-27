package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web2.dao.TopoTagMapper;
import com.jcca.web2.entity.TopoTag;
import com.jcca.web2.service.TopoTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 拓扑图页签
 * @author: sophia
 * @create: 2023/10/24 10:43
 **/
@Service
public class TopoTagServiceImpl extends ServiceImpl<TopoTagMapper, TopoTag> implements TopoTagService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateTag(TopoTag topoTag) {
        String orgId = topoTag.getOrgId();
        String id = topoTag.getId();
        String preId = topoTag.getPreId();
        if (StringUtils.isEmpty(preId)) {
            throw new ResultException(ResultEnum.PARAM_ERROR, "没有选择排序");
        }

        // 检查是否存在相同组织和类别的拓扑图
        QueryWrapper<TopoTag> query = Wrappers.<TopoTag>query()
                .eq("ORG_ID", orgId)
                .orderByAsc("TAG_SORT");
        if (StringUtils.hasText(id)) {
            query.ne("ID", id);
        }
        List<TopoTag> existingTags = this.list(query);
        boolean duplicateExists = existingTags.stream().anyMatch(tag ->
                tag.getCategory().equals(topoTag.getCategory()) &&
                        !tag.getId().equals(id));
        if (duplicateExists) {
            throw new ResultException(ResultEnum.PARAM_ERROR, "已存在该类型拓扑图");
        }

        if (StringUtils.isEmpty(topoTag.getId())) {
            topoTag.setId(MyIdUtil.getId());
            topoTag.setRemark("前端创建");
        }
        int tmp = 0;
        List<TopoTag> nlist = new ArrayList<>();
        if ("0".equals(preId)) {
            tmp++;
            topoTag.setTagSort(tmp);
            nlist.add(topoTag);
        }
        for (TopoTag existingTag : existingTags) {
            tmp++;
            existingTag.setTagSort(tmp);
            nlist.add(existingTag);
            if (existingTag.getId().equals(preId)) {
                tmp++;
                topoTag.setTagSort(tmp);
                nlist.add(topoTag);
            }
        }
        // 批量保存或更新
        this.saveOrUpdateBatch(nlist);
    }
}