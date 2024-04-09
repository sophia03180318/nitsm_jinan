package com.jcca.admin.system.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.web2.enums.TempDownloadEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @description: 文件类模板统一管理
 * @author: HanHW
 * @date: 2023/12/1 11:13
 **/
@Controller
@RequestMapping("/system/template")
public class TemplateController {

    @GetMapping("/getTemp")
    @ApiOperation(value = "下载表格模板")
    @ActionLog(name = "下载表格模板", title = "资产管理", key = LogTypeConstant.DOWNLOAD)
    public void getTemp(HttpServletResponse response, Integer code) {
        if (Objects.isNull(code)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }
        String fileName = TempDownloadEnum.getFileNameByCode(code);
        if (StringUtils.isEmpty(fileName)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        String filePath = "/templates/import/" + fileName;
        InputStream fileInput = TemplateController.class.getResourceAsStream(filePath);
        ExcelReader reader = ExcelUtil.getReader(fileInput);
        ExcelWriter writer = reader.getWriter();
        try {
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
        } catch (IOException e) {
            throw new ResultException(ResultEnum.DOWNLOAD_ERROR);
        }
    }

}
