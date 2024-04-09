package com.jcca.web.statistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.graph.vo.StatisticsInfoVo;
import com.jcca.web.statistics.dao.HourInterfacesItemMapper;
import com.jcca.web.statistics.dao.HourInterfacesServiceMapper;
import com.jcca.web.statistics.entity.HourInterfaces;
import com.jcca.web.statistics.service.HourInterfacesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 一小时统计
 *
 * @author Lvyp
 */
@Service
public class HourInterfacesServiceImpl extends ServiceImpl<HourInterfacesServiceMapper, HourInterfaces> implements HourInterfacesService {

    @Resource
    private HourInterfacesItemMapper hourInterfacesItemMapper;
    @Resource
    private HourInterfacesServiceMapper hourInterfaceMapper;


    @Override
    public Date lastCreateTime() {
        return hourInterfaceMapper.maxCreateDate();
    }

    /**
     * 按时间区间查询资产端口流量折线数据
     *
     * @param assetId
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<AssetHistoryVo> findLineByDate(String assetId, Date startDate, Date endDate) {
        return hourInterfaceMapper.findLineByDate(assetId, startDate, endDate);
    }

    @Override
    public List<StatisticsInfoVo> portIns(String assetId, String portName) {
        return hourInterfacesItemMapper.portIns(assetId, portName);
    }

    @Override
    public List<StatisticsInfoVo> portOuts(String assetId, String portName) {
        return hourInterfacesItemMapper.portOuts(assetId, portName);
    }

    @Override
    public List<StatisticsInfoVo> discardPackageIns(String assetId, String portName) {
        return hourInterfacesItemMapper.discardPackageIns(assetId, portName);
    }

    @Override
    public List<StatisticsInfoVo> discardPackageOuts(String assetId, String portName) {
        return hourInterfacesItemMapper.discardPackageOuts(assetId, portName);
    }

    @Override
    public List<StatisticsInfoVo> errorCodeIns(String assetId, String portName) {
        return hourInterfacesItemMapper.errorCodeIns(assetId, portName);
    }

    @Override
    public List<StatisticsInfoVo> errorCodeOuts(String assetId, String portName) {
        return hourInterfacesItemMapper.errorCodeOuts(assetId, portName);
    }

}
