package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.CollectAIXAdapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CollectAIXAdapterMapper extends BaseMapper<CollectAIXAdapter> {

    List<CollectAIXAdapter> selectLatestInfo(String assetId);
}