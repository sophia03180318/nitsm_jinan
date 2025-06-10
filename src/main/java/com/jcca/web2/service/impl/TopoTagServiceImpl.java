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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

        QueryWrapper<TopoTag> query = Wrappers.query();
        query.eq("ORG_ID", orgId);
        query.eq("CATEGORY", topoTag.getCategory());
        if (!StringUtils.isEmpty(id)) {
            query.ne("ID", id);
        }
        List<TopoTag> list = list(query);
        if (!CollectionUtils.isEmpty(list)) {
            throw new Exception("已存在该类型拓扑图");
        }

        QueryWrapper<TopoTag> otherTag = Wrappers.query();
        otherTag.eq("ORG_ID", orgId);
        otherTag.ge("TAG_SORT", topoTag.getTagSort());
        List<TopoTag> otherList = list(otherTag);

        if(!otherList.isEmpty()){
            int tmp = topoTag.getTagSort();
            for (TopoTag tag : otherList) {
                tmp++;
                tag.setTagSort(tmp);
                updateById(tag);
            }
        }


        if (StringUtils.isEmpty(id)) {
            topoTag.setId(MyIdUtil.getId());
            topoTag.setRemark("前端创建");
            save(topoTag);
        }else{
            updateById(topoTag);
        }

    }

}