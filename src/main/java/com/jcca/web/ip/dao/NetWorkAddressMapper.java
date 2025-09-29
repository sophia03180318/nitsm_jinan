package com.jcca.web.ip.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ip.entity.NetWorkAddress;
import com.jcca.web2.vo.StationNetObj;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 网络地址
 *
 * @author lyp
 */
public interface NetWorkAddressMapper extends BaseMapper<NetWorkAddress> {
    List<NetWorkAddress> findAll();

    /**
     * 查询组织下面所有ip段
     *
     * @param id
     * @return
     */
    List<NetWorkAddress> selectIps(@Param("id") String id);

    /**
     * 查询线下所有车站的网络列表信息
     * @param lineOrgId
     * @return
     */
    List<StationNetObj> getLineNetMsgV2(@Param("lineOrgId") String lineOrgId);
}
