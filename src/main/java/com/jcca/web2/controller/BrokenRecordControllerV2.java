package com.jcca.web2.controller;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.broken.service.BrokenRecordService;
import com.jcca.web2.vo.DialogsAlarmListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HanHW
 * @description 故障记录
 * @className BrokenRecordControllerV2
 * @date 2024/1/9 17:15
 * @since 2.1.0.0
 */

@RestController
@RequestMapping("/api/v2/br")
@Api(tags = "故障记录V2")
public class BrokenRecordControllerV2 {

    @Resource
    private BrokenRecordService brokenRecordService;

    @GetMapping("/list/{alarmId}")
    @ApiOperation("相关事件")
    public ResultVo<Object> list(@PathVariable String alarmId) {

        List<DialogsAlarmListVo> voList = brokenRecordService.listByAlarmId(alarmId);

        return ResultVoUtil.success(voList);
    }
}
