package com.jcca.web.ip.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web2.vo.IpVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ip信息
 *
 * @author lyp
 */
@Mapper
public interface IpInfoMapper extends BaseMapper<IpInfo> {

    /**
     * 批量插入
     *
     * @param ipMsgList
     * @return
     */
    @Insert({"<script>",
            "insert into IP_INFO(ID,NET_WORK_ADDRESS_ID,IP,MAC,MASK,GATEWAY,STATUS,REMARK,CREATE_TIME,CREATOR,MODIFY_TIME,MODIFIER) values",
            "<foreach collection='ipMsgList' item='item' index='index' separator=','>",
            "(#{item.id,#item.netWorkAddressId,#{item.ip},#{item.mac},#{item.mask},#{item.gateway},#{item.status},#{item.remark},#{item.createTime},#{item.creator},#{item.modifyTime},#{item.modifier}})",
            "</foreach>", "</script>"})
    Integer insertAll(@Param(value = "ipMsgList") List<IpInfo> ipMsgList);


    /**
     * 查询
     *
     * @param ip
     * @return
     */
    @Select("select * from M_IP_INFO where IP=#{ip}")
    List<IpInfo> selectByIp(@Param("ip") String ip);


    List<IpVo> selectIPVoV2(@Param("netWorkId") String netWorkId);
}
