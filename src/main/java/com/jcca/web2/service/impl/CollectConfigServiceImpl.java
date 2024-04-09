package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.service.PerformanceTargetService;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.SSHUtil;
import com.jcca.common.utils.SnmpUtil;
import com.jcca.common.utils.TelnetUtil;
import com.jcca.common.utils.TestIpUtil;
import com.jcca.web.common.service.OutService;
import com.jcca.web.topo.service.bean.SnmpExecuteResult;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dto.CollectConfigDto;
import com.jcca.web2.enums.TargetCronUnitEnum;
import com.jcca.web2.service.CollectConfigService;
import com.jcca.web2.vo.CollectConfigVo;
import com.jcca.web2.vo.TargetVerifyVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 采集配置
 * @className CollectConfigServiceImpl
 * @date 2024/2/28 14:53
 * @since 2.1.0.0
 */
@Service
public class CollectConfigServiceImpl implements CollectConfigService {

    @Resource
    private SpecDictionaryService specService;
    @Resource
    private PerformanceTargetService targetService;
    @Resource
    private OutService outService;

    /**
     * 查找采集指标和指标字典
     *
     * @param dto 查询参数
     * @return 指标列表和指标字典列表
     */
    @Override
    public List<PerformanceTarget> list(CollectConfigDto dto) {
        QueryWrapper<SpecDictionary> query1 = Wrappers.query();
        QueryWrapper<PerformanceTarget> query2 = Wrappers.query();
        if (Objects.nonNull(dto.getAssetMode())) {
            query1.eq("ASSET_MODE", dto.getAssetMode());
            query2.eq("ASSET_MODE", dto.getAssetMode());
        }
        if (!StringUtils.isEmpty(dto.getAssetImage())) {
            query1.eq("ASSET_IMAGE", dto.getAssetImage());
        }

        List<SpecDictionary> dictionaryList = specService.list(query1);
        if (!CollectionUtils.isEmpty(dictionaryList)) {
            throw new ResultException(ResultEnum.CANNOT_FIND.getCode(), "未查询到数据");
        }

        Set<Integer> collect = dictionaryList.stream().map(SpecDictionary::getSpecId).collect(Collectors.toSet());
        query2.in("SPEC_ID", collect);
        return targetService.list(query2);
    }

    /**
     * 按ID删除指标字典
     *
     * @param specDictId 指标字典数据ID
     */
    @Override
    public void delSpecDict(String specDictId) {
        specService.removeById(specDictId);
    }

    /**
     * 保存采集配置
     *
     * @param vo CollectConfigVo
     */
    @Override
    public void save(CollectConfigVo vo) {
        List<String> targetIds = vo.getTargetIds();
        if (CollectionUtils.isEmpty(targetIds)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "采集指标不能为空");
        }

        List<SpecDictionary> list = new ArrayList<>();
        PerformanceTarget target;
        for (String targetId : targetIds) {
            target = targetService.getById(targetId);
            if (Objects.isNull(target)) {
                continue;
            }
            this.checkSpectDict(vo);

            list.add(specService.getSpecDict(vo, target.getSpecId()));
        }

        specService.saveBatch(list);
    }

    private void checkSpectDict(CollectConfigVo vo) {
        QueryWrapper<SpecDictionary> query = Wrappers.query();
        query.eq("ASSET_MODE", vo.getAssetMode());
        query.eq("ASSET_IMAGE", vo.getAssetImage());
        query.eq("MANUFACTURER_ID", vo.getManufactureId());
        query.eq("SYSTEM_TYPE", vo.getSystemType());
        List<SpecDictionary> list = specService.list(query);
        if (!CollectionUtils.isEmpty(list)) {
            throw new ResultException(ResultEnum.DUPLICATE.getCode(),
                    "指标字典已存在：资产类型：[" + vo.getAssetMode() + "]，资产型号：[" + vo.getAssetImage() + "]，厂商ID：["
                            + vo.getManufactureId() + "]，系统类型：[" + vo.getSystemType() + "]");
        }
    }


    /**
     * 修改指标采集时间
     *
     * @param targetId 指标ID
     * @param unit     时间单位
     * @param internal 时间间隔
     */
    @Override
    public void modifyCron(String targetId, String unit, Integer internal) {
        String cron = this.getCron(unit, internal);
        if (StringUtils.isEmpty(cron)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "时间单位只有月日时分秒");
        }
        PerformanceTarget one = targetService.getById(targetId);
        if (Objects.isNull(one)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }
        if (!one.getCronExpress().equals(cron)) {
            one.setCronExpress(cron);
            targetService.updateById(one);
            // 指标变动通知采集器
            outService.targetOnChange();
        }
    }

    /**
     * 启停采集指标
     *
     * @param targetId    指标ID
     * @param isAvaliable 0不可用，1可用
     */
    @Override
    public void modifyStatus(String targetId, Integer isAvaliable) {
        PerformanceTarget one = targetService.getById(targetId);
        if (Objects.isNull(one)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }
        if (one.getIsAvailable().intValue() != isAvaliable) {
            one.setIsAvailable(isAvaliable);
            targetService.updateById(one);
            // 指标变动通知采集器
            outService.targetOnChange();
        }
    }

    private final static String STR = "时间间隔不能大于";

    private String getCron(String unit, Integer internal) {
        if (TargetCronUnitEnum.MONTH.name().equals(unit)) {
            if (TargetCronUnitEnum.MONTH.interval < internal) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), STR + TargetCronUnitEnum.MONTH.interval);
            }
            return TargetCronUnitEnum.MONTH.cron.replace(Web2Const.TARGET_INTERVAL, String.valueOf(internal));
        }
        if (TargetCronUnitEnum.DATE.name().equals(unit)) {
            if (TargetCronUnitEnum.DATE.interval < internal) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), STR + TargetCronUnitEnum.DATE.interval);
            }
            return TargetCronUnitEnum.DATE.cron.replace(Web2Const.TARGET_INTERVAL, String.valueOf(internal));
        }
        if (TargetCronUnitEnum.HOUR.name().equals(unit)) {
            if (TargetCronUnitEnum.HOUR.interval < internal) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), STR + TargetCronUnitEnum.HOUR.interval);
            }
            return TargetCronUnitEnum.HOUR.cron.replace(Web2Const.TARGET_INTERVAL, String.valueOf(internal));
        }
        if (TargetCronUnitEnum.MINUTE.name().equals(unit)) {
            if (TargetCronUnitEnum.MINUTE.interval < internal) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), STR + TargetCronUnitEnum.MINUTE.interval);
            }
            return TargetCronUnitEnum.MINUTE.cron.replace(Web2Const.TARGET_INTERVAL, String.valueOf(internal));
        }
        if (TargetCronUnitEnum.SECOND.name().equals(unit)) {
            if (TargetCronUnitEnum.SECOND.interval < internal) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), STR + TargetCronUnitEnum.SECOND.interval);
            }
            return TargetCronUnitEnum.SECOND.cron.replace(Web2Const.TARGET_INTERVAL, String.valueOf(internal));
        }

        return "";
    }

    /**
     * 指标验证
     *
     * @param vo 参数
     * @return 验证结果
     */
    @Override
    public Map<String, Object> verifyTarget(TargetVerifyVo vo) {
        String systemType = vo.getSystemType().name();
        if (StringUtils.isEmpty(systemType)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "测试类型不能为空");
        }
        Map<String, Object> map = new HashMap<>();
        switch (systemType) {
            case "SSH":
                this.verifySSH(vo, map);
                break;
            case "SNMP":
                this.verifySNMP(vo, map);
                break;
            case "TELNET":
                this.verifyTELNET(vo, map);
        }
        return map;
    }


    private void verifyTELNET(TargetVerifyVo vo, Map<String, Object> map) {
        if (!this.ping(vo)) {
            map.put("result", "IP不通");
            return;
        }
        List<String> list = TelnetUtil.exeTelNet(vo.getIp(), vo.getUsername(), vo.getPassword(), vo.getEnablePassword(), vo.getPort(), Collections.singletonList(vo.getCommand()));

        map.put("result", list);
    }

    private void verifySNMP(TargetVerifyVo vo, Map<String, Object> map) {
        if (!this.ping(vo)) {
            map.put("result", "IP不通");
            return;
        }
        List<SnmpExecuteResult> results = SnmpUtil.snmpWalk(vo.getIp(), vo.getUsername(), vo.getPort(), vo.getCommand());
        map.put("result", results);
    }

    private void verifySSH(TargetVerifyVo vo, Map<String, Object> map) {
        String username = vo.getUsername();
        String password = vo.getPassword();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "SSH测试时用户名密码不能为空");
        }
        if (!this.ping(vo)) {
            map.put("result", "IP不通");
            return;
        }

        int port = vo.getPort() == null ? 22 : vo.getPort();
        String command = vo.getCommand();
        SSHUtil ssh = new SSHUtil(username, password, vo.getIp(), port);
        try {
            ssh.connect();
            StringBuilder sb = ssh.execCmd(command);
            map.put("result", sb.toString());
        } catch (Exception e) {
            map.put("result", "执行SSH测试异常：" + e.getMessage());
        }
    }

    private Boolean ping(TargetVerifyVo vo) {
        String ip = vo.getIp();
        try {
            return TestIpUtil.ping(ip, 2);
        } catch (IOException e) {
            return false;
        }
    }
}
