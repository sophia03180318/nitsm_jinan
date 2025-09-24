package com.jcca.web.statistics.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.statistics.entity.HourInterfaces;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 一小时端口统计
 *
 * @author Lvyp
 */
@Mapper
public interface HourInterfacesServiceMapper extends BaseMapper<HourInterfaces> {

    /**
     * 查询最大的创建时间
     *
     * @return
     */
    Date maxCreateDate();


    List<AssetHistoryVo> findLineByDate(String assetId, Date startDate, Date endDate);


}
