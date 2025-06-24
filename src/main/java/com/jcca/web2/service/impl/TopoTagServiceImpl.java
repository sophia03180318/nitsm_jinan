package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    public void saveOrUpdateTag(TopoTag topoTag) throws Exception {
        String orgId = topoTag.getOrgId();
        String id = topoTag.getId();
        int sort = topoTag.getTagSort();

        // 检查是否存在相同组织和类别的拓扑图
        QueryWrapper<TopoTag> query = Wrappers.<TopoTag>query()
                .eq("ORG_ID", orgId)
                .eq("CATEGORY", topoTag.getCategory());
        if (StringUtils.hasText(id)) {
            query.ne("ID", id);
        }
        if (this.count(query) > 0) {
            throw new Exception("已存在该类型拓扑图");
        }

        // 获取当前组织下的所有标签并按排序字段升序排列
        QueryWrapper<TopoTag> otherTag = Wrappers.<TopoTag>query()
                .eq("ORG_ID", orgId)
                .orderByAsc("TAG_SORT");
        if (StringUtils.hasText(id)) {
            otherTag.ne("ID", id);
        }
        List<TopoTag> existingTags = this.list(otherTag);
        if (sort >= existingTags.size()) {
            existingTags.add(topoTag);
        } else {
            existingTags.add(sort - 1, topoTag);
        }
        int tmp = 1;
        List<TopoTag> nlist = new ArrayList<>();
        for (TopoTag tag : existingTags) {
            if (StringUtils.isEmpty(tag.getId())) {
                tag.setId(MyIdUtil.getId());
                tag.setRemark("前端创建");
            }
            tag.setTagSort(tmp);
            tmp++;
            nlist.add(tag);
        }
        // 批量保存或更新
        this.saveOrUpdateBatch(nlist);
    }
}