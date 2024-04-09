package com.jcca.web.common.controller;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.common.service.ThreeDService;
import com.jcca.web.common.service.bean.ThreeDResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 3D机房接口
 * @author: sophia
 * @create: 2023/12/27 11:33
 **/
@RestController
@RequestMapping("/api/v2/threeD")
@Api(tags = "3D机房接口")
public class ThreeDController {

    @Resource
    private ThreeDService threeDService;

    @GetMapping("/syncAssetByRoom")
    @ApiOperation("同步3D机房信息")
    public ResultVo syncAssetByRoom() {
        ThreeDResult threeDResult = threeDService.syncAssetByRoom();
        if (threeDResult.isStatus()){
            return ResultVoUtil.success("同步成功~");
        }
        return ResultVoUtil.error(threeDResult.getLog());
    }

}