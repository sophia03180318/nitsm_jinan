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
    Integer insertAll(@Param(value = "ipMsgList") List<IpInfo> ipMsgList);


    /**
     * 查询
     *
     * @param ip
     * @return
     */
    List<IpInfo> selectByIp(@Param("ip") String ip);


    List<IpVo> selectIPVoV2(@Param("netWorkId") String netWorkId);
}
