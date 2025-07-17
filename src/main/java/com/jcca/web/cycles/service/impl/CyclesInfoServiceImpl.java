package com.jcca.web.cycles.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.utils.AppListUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.cycles.dao.CyclesInfoMapper;
import com.jcca.web.cycles.dao.CyclesOrgMapper;
import com.jcca.web.cycles.dao.CyclesTimesMapper;
import com.jcca.web.cycles.entity.CyclesInfo;
import com.jcca.web.cycles.entity.CyclesOrg;
import com.jcca.web.cycles.entity.CyclesTimes;
import com.jcca.web.cycles.service.CyclesInfoService;
import com.jcca.web.cycles.service.bean.CyclesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description: 周期信息
 * @author: Lvyp
 * @create: 2024/11/20 10:05
 */
@Service
public class CyclesInfoServiceImpl extends ServiceImpl<CyclesInfoMapper, CyclesInfo> implements CyclesInfoService {

    @Resource
    private CyclesInfoMapper cyclesInfoMapper;
    @Resource
    private CyclesTimesMapper cyclesTimesMapper;
    @Resource
    private CyclesOrgMapper cyclesOrgMapper;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateInfo(CyclesInfo info) throws CyclesException {
        String id = info.getId();
        if (StrUtil.isEmpty(id)) {
            throw new CyclesException("缺少ID");
        }
        cyclesInfoMapper.deleteById(id);
        QueryWrapper<CyclesTimes> timesWrapper = new QueryWrapper<>();
        timesWrapper.eq("cycles_info_id", id);
        cyclesTimesMapper.delete(timesWrapper);
        QueryWrapper<CyclesOrg> orgWrapper = new QueryWrapper<>();
        orgWrapper.eq("cycles_info_id", id);
        cyclesOrgMapper.delete(orgWrapper);

        saveInfo(info);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteById(String id) throws CyclesException {
        if (StrUtil.isEmpty(id)) {
            throw new CyclesException("缺少ID");
        }
        cyclesInfoMapper.deleteById(id);
        QueryWrapper<CyclesTimes> timesWrapper = new QueryWrapper<>();
        timesWrapper.eq("cycles_info_id", id);
        cyclesTimesMapper.delete(timesWrapper);
        QueryWrapper<CyclesOrg> orgWrapper = new QueryWrapper<>();
        orgWrapper.eq("cycles_info_id", id);
        cyclesOrgMapper.delete(orgWrapper);
    }

    @Override
    public boolean verifyIsBlank(Asset asset, Date occurTime) {
        if (Objects.isNull(asset) || Objects.isNull(occurTime)) {
            return false;
        }
        String orgId = asset.getOrgId();
        Week weekEnum = DateUtil.dayOfWeekEnum(occurTime);
        int week = 0;
        if (weekEnum == Week.SUNDAY) {
            week = 7;
        } else {
            week = weekEnum.getValue() - 1;
        }


        String time = DateUtil.format(occurTime, "HHmmss");
        List<CyclesInfo> cyclesInfo = cyclesInfoMapper.selectByOrgIdAndTime(orgId, week, Integer.valueOf(time));


        return !cyclesInfo.isEmpty();
    }

    @Override
    public CyclesInfo queryInfoById(String id) {
        return cyclesInfoMapper.selectInfoById(id);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveInfo(CyclesInfo info) throws CyclesException {
        if (StrUtil.isEmpty(info.getCyclesName())) {
            throw new CyclesException("缺少计划名称");
        }
        List<CyclesOrg> orgList = info.getOrgList();
        if (Objects.isNull(orgList) || orgList.isEmpty()) {
            throw new CyclesException("缺少计划影响组织");
        }
        String infoId = MyIdUtil.getId();
        info.setId(infoId);
        info.setStatus(1);
        info.setCreateTime(new Date());

        for (CyclesOrg cyclesOrgItem : orgList) {
            if (StrUtil.isEmpty(cyclesOrgItem.getOrgId())) {
                throw new CyclesException("缺少组织ID");
            }
            if (StrUtil.isEmpty(cyclesOrgItem.getOrgPid())) {
                throw new CyclesException("缺少组织PID");
            }
            String cyclesOrgId = MyIdUtil.getId();
            cyclesOrgItem.setCyclesInfoId(infoId);
            cyclesOrgItem.setId(cyclesOrgId);
        }

        List<CyclesTimes> timeReqList = info.getTimesList();
        if (Objects.isNull(timeReqList) || timeReqList.isEmpty()) {
            throw new CyclesException("缺少计划时间");
        }
        for (CyclesTimes times : timeReqList) {
            if (Objects.isNull(times.getStartTime())) {
                throw new CyclesException("缺少开始时间");
            }
            if (Objects.isNull(times.getEndTime())) {
                throw new CyclesException("缺少结束时间");
            }
            if (Objects.isNull(times.getWeeks())) {
                throw new CyclesException("缺少日期");
            }

            times.setId(MyIdUtil.getId());
            times.setCyclesInfoId(infoId);
        }


        cyclesInfoMapper.insert(info);
        if (!orgList.isEmpty()) {
            cyclesOrgMapper.saveBatch(orgList);
        }
        if (!timeReqList.isEmpty()) {
            cyclesTimesMapper.saveBatch(timeReqList);
        }

    }

    @Override
    public IPage pageQuery(Integer page, Integer size, String name) {
        List<CyclesInfo> cyclesInfos = cyclesInfoMapper.queryAll(name);
        return AppListUtils.pageList(cyclesInfos, page, size);
    }


}
