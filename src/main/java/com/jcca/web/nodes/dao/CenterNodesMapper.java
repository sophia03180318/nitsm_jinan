package com.jcca.web.nodes.dao;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.nodes.entity.CenterNodes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


/**
 * 中心采集器节点
 */
@Mapper
public interface CenterNodesMapper extends BaseMapper<CenterNodes> {


    CenterNodes selectNodes(@Param("nodeIp") String nodeIp);
}
