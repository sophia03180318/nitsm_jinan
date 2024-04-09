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


    @Select("select * from center_nodes e where (e.NODE_IP1 = #{nodeIp} or e.NODE_IP2 = #{nodeIp})")
    CenterNodes selectNodes(@Param("nodeIp") String nodeIp);
}
