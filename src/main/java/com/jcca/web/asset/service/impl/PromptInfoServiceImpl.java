package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.dao.PromptInfoMapper;
import com.jcca.web.asset.entity.PromptInfo;
import com.jcca.web.asset.service.PromptInfoService;
import com.jcca.web2.vo.ProcessPlateVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置进程提示信息
 *
 * @author syt
 */
@Service
@Slf4j
public class PromptInfoServiceImpl extends ServiceImpl<PromptInfoMapper, PromptInfo>
        implements PromptInfoService {

    @Resource
    private PromptInfoMapper promptInfoMapper;

    /*
     * 根据字典名称获取提示信息
     * @Param [dictName]
     * @return java.util.List<java.lang.String>
     * @Author syt
     * @Date 2021/10/27 14:50
     */
    @Override
    public List<String> getPromptByDictName(String dictName, String type) {
        return promptInfoMapper.getPromptByDictName(dictName, type);
    }

    /*
     * 查询配置过的进程
     * @Param [key]
     * @return java.util.List<java.lang.String>
     * @Author syt
     * @Date 2021/10/27 17:40
     */
    @Override
    public List<String> getExistPrompt(String key) {
        return promptInfoMapper.getExistPrompt(key);
    }

    /**
     * @description: 添加进程模板
     * @author: HanHW
     * @date: 2023/11/10 15:34
     * @param: [vo]
     * @return: void
     */
    @Override
    public void addPlateV2(ProcessPlateVo vo) {
        String plateName = vo.getPlateName();
        if (StringUtils.isEmpty(plateName)) {
            throw new ResultException(ResultEnum.TEMPLATE_NAME_NULL);
        }

        String softwareTypeId = vo.getSoftwareTypeId();
        if (StringUtils.isEmpty(softwareTypeId)) {
            throw new ResultException(ResultEnum.TEMPLATE_TYPE_NULL);
        }

        List<String> processList = vo.getProcessList();
        if (CollectionUtils.isEmpty(processList)) {
            throw new ResultException(ResultEnum.TEMPLATE_PROCESS_NULL);
        }

        QueryWrapper<PromptInfo> wrapper = Wrappers.query();
        wrapper.eq("PLATE_NAME", plateName);
        List<PromptInfo> list1 = this.list(wrapper);
        if (!CollectionUtils.isEmpty(list1)) {
            throw new ResultException(ResultEnum.TEMPLATE_NAME_EXIST);
        }

        String plateId = vo.getPlateId();
        if (StringUtils.isEmpty(plateId)) {
            plateId = MyIdUtil.getId();
        }
        List<PromptInfo> list = new ArrayList<>();
        for (String name : processList) {
            if (StringUtils.isEmpty(name)) continue;
            PromptInfo info = new PromptInfo();
            info.setId(MyIdUtil.getId());
            info.setPlateName(plateName);
            info.setPlateId(plateId);
            info.setRemark(vo.getRemark());
            info.setValue(name.trim());
            info.setSoftwareTypeId(softwareTypeId);
            list.add(info);
        }

        this.saveBatch(list);
    }

    /**
     * @description: 根据业务类型查找进程模板
     * @author: HanHW
     * @date: 2023/11/13 15:30
     * @param: [softwareTypeId]
     * @return: java.util.List<com.jcca.web2.vo.ProcessPlateVo>
     */
    @Override
    public List<ProcessPlateVo> findBySoftTypeIdV2(String softwareTypeId) {

        List<ProcessPlateVo> resList = new ArrayList<>();
        QueryWrapper<PromptInfo> wrapper = Wrappers.query();
        wrapper.select("PLATE_ID", "MODIFY_TIME");
        wrapper.eq("SOFTWARETYPE_ID", softwareTypeId);
        wrapper.groupBy("PLATE_ID", "MODIFY_TIME");
        wrapper.orderByDesc("MODIFY_TIME");
        List<PromptInfo> list = this.list(wrapper);
        for (PromptInfo type : list) {
            wrapper = Wrappers.query();
            wrapper.eq("PLATE_ID", type.getPlateId());
            wrapper.orderByDesc("MODIFY_TIME");
            List<PromptInfo> promptInfos = this.list(wrapper);
            List<String> names = promptInfos.stream().map(PromptInfo::getValue).collect(Collectors.toList());

            // 模板ID，模板名称，类型ID，进程列表
            PromptInfo plate = promptInfos.get(0);
            ProcessPlateVo vo = new ProcessPlateVo();
            vo.setPlateId(plate.getPlateId());
            vo.setPlateName(plate.getPlateName());
            vo.setSoftwareTypeId(plate.getSoftwareTypeId());
            vo.setProcessList(names);
            resList.add(vo);
        }
        return resList;
    }
}
