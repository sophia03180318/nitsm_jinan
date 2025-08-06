package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web2.dto.PortModelTemp;
import com.jcca.web2.dto.PortTempDto;
import com.jcca.web2.entity.PortTemp;
import com.jcca.web2.service.PortTempService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 端口模板接口
 * @author: sophia
 * @create: 2024/01/08 10:27
 **/
@RestController
@RequestMapping("/api/v2/portTemp")
@Api(tags = "端口模板接口")
@Log4j
public class PortTemplateControllerV2 {

    @Resource
    private PortTempService tempService;
    @Resource
    private AssetService assetService;
    @Resource
    private CollectInterfacesService collectInterfacesService;
    @Resource
    private CollectNetworkCardService collectNetworkCardService;

    @Value("${project.upload.file-path}")
    private String path;


    @PostMapping("/fileToBase64")
    public ResultVo<Object> fileToBase64(@RequestParam("file") MultipartFile multipartFile) {
        try {
            byte[] fileBytes = multipartFile.getBytes();
            // 使用Base64编码器将字节数据编码为Base64字符串
            String base64String = Base64.getEncoder().encodeToString(fileBytes);
            // 添加数据URI前缀，这里假设图片是PNG格式，可以根据实际情况调整
            String imageBase64 = "data:image/png;base64," + base64String;
            return ResultVoUtil.success("成功", imageBase64);
        } catch (IOException e) {
            return ResultVoUtil.error(e.getMessage());
        }
    }


    @GetMapping("getTemplateByAssetId/{assetId}")
    @ApiOperation("通过资产ID获取可使用的模板列表")
    public ResultVo getTemplateByAssetId(@PathVariable String assetId) {
        List<PortTemp> tempList = tempService.getTemplateByAssetId(assetId);
        return ResultVoUtil.success(tempList);
    }

    @GetMapping("/getAssetModelList")
    @ApiOperation("获取网络设备型号列表")
    public ResultVo<Object> getAssetModelList() {
        List<PortModelTemp> collect = tempService.getNetworkDevices();
        return ResultVoUtil.success(collect);
    }


    @GetMapping("/getPortTempList")
    @ApiOperation("获取模板列表")
    public ResultVo<Object> getPortTempList() {
        List<PortModelTemp> modelTemps = tempService.getNetworkDevices();
        Map<String, List<PortTemp>> map = tempService.list().stream().collect(Collectors.groupingBy(PortTemp::getModelId));
        for (PortModelTemp modelTemp : modelTemps) {
            if (map.containsKey(modelTemp.getId())) {
                modelTemp.setPortTempDtoList(map.get(modelTemp.getId()));
            }
        }
        return ResultVoUtil.success(modelTemps);
    }

    @PostMapping("/editPortTemp")
    @ApiOperation("修改端口模板")
    public ResultVo<Object> editPortTemp(@RequestBody PortTempDto portTempDto) {
        String id = portTempDto.getId();
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        PortTemp port = tempService.getById(id);
        //删除
        deletePortTemp(id);

        //新增
        PortTempDto portTempDto2 = PortTempDto.getPortTemp(port);
        portTempDto.setPort1(portTempDto2.getPort1());
        portTempDto.setPort2(portTempDto2.getPort2());
        portTempDto2.setPortSortType(portTempDto.getPortSortType());
        savePortTemp(portTempDto);
        return ResultVoUtil.success("编辑成功");
    }

    @GetMapping("/asset/interface")
    @ApiOperation("获取资产端口列表")
    public ResultVo<Object> getAssetInterface(@RequestParam("assetId") String assetId) {
        Asset asset = assetService.getById(assetId);
        List<CollectInterfaces> list = new ArrayList<>();
        if (asset.getAssetMode() == 183) {
            List<CollectNetworkCard> cardList = collectNetworkCardService.getRealTimeData(assetId);
            for (CollectNetworkCard networkCard : cardList) {
                if (StringUtils.isEmpty(networkCard.getIp()) || "--".equals(networkCard.getIp())) {
                    continue;
                }
                CollectInterfaces vo = new CollectInterfaces();
                vo.setPortName(networkCard.getName());
                vo.setPortIndex(networkCard.getName());
                vo.setLinkIp(networkCard.getIp());
                list.add(vo);
            }
        } else {
            list = collectInterfacesService.filterPort(assetId);
        }
        return ResultVoUtil.success(list);
    }


    @PostMapping("/savePortTemp")
    @ApiOperation("保存端口模板")
    public ResultVo<Object> savePortTemp(@RequestBody PortTempDto portTempDto) {
        QueryWrapper<PortTemp> qw = new QueryWrapper<>();
        qw.eq("MODEL_ID", portTempDto.getModelId());
        qw.eq("NAME", portTempDto.getName());
        List<PortTemp> list = tempService.list(qw);
        if (ObjectUtil.isNotNull(list) && !list.isEmpty()) {
            PortTemp portTemp = list.get(0);
            if (!portTemp.getId().equals(portTempDto.getId())) {
                return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "同类型下模板名称已存在");
            }
        }
        PortTemp portTemp = new PortTemp();
        if (StringUtils.isEmpty(portTempDto.getId())) {
            portTemp.setId(MyIdUtil.getId());
        } else {
            portTemp.setId(portTempDto.getId());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("port1", portTempDto.getPort1());
        jsonObject.put("port2", portTempDto.getPort2());

        portTemp.setPortCount(jsonObject.toString());
        portTemp.setName(portTempDto.getName());
        portTemp.setModelId(portTempDto.getModelId());
        portTemp.setPortSortType(portTempDto.getPortSortType());

        String filePath = path + "/portTemp/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath + portTemp.getModelId() + "&&" + portTemp.getName());
            fileWriter.write(portTempDto.getMsg());
            fileWriter.close();
            tempService.save(portTemp);
            return ResultVoUtil.success(portTemp);
        } catch (IOException e) {
            if (ObjectUtil.isNotNull(fileWriter)) {
                try {
                    fileWriter.close();
                } catch (IOException ignored) {
                }
            }
            return ResultVoUtil.error(e.toString());
        }
    }


    @GetMapping("/getPortTemp/{id}")
    @ApiOperation("获取端口模板详情")
    public ResultVo getPortTemp(@PathVariable String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }
        PortTemp portTemp = tempService.getById(id);
        if (ObjectUtil.isNull(portTemp)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "未查询到对应模板信息");
        }
        PortTempDto portTempDto = new PortTempDto();
        portTempDto.setId(portTemp.getId());
        portTempDto.setName(portTemp.getName());
        portTempDto.setModelId(portTemp.getModelId());
        portTempDto.setPortSortType(portTemp.getPortSortType());
        try {
            if (!StringUtils.isEmpty(portTemp.getPortCount())) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(portTemp.getPortCount());
                portTempDto.setPort1(jsonNode.get("port1").asInt());
                portTempDto.setPort2(jsonNode.get("port2").asInt());
            }
        } catch (JsonProcessingException e) {

        }
        String filePath = path + "/portTemp/";
        Reader reader = null;
        try {
            File file = new File(filePath + portTemp.getModelId() + "&&" + portTemp.getName());
            if (!file.exists()) {
                return ResultVoUtil.warning("未找到" + portTemp.getName() + "对应端口模板文件");
            }
            reader = new InputStreamReader(new FileInputStream(file));
            char[] tempchars = new char[50];
            int charread = 0;
            StringBuilder builder = new StringBuilder();
            while ((charread = reader.read(tempchars)) != -1) {
                if ((charread == tempchars.length)) {
                    builder.append(tempchars);
                } else {
                    for (int i = 0; i < charread; i++) {
                        builder.append(tempchars[i]);
                    }
                }
            }
            reader.close();
            portTempDto.setMsg(builder.toString());
            return ResultVoUtil.success(portTempDto);
        } catch (IOException e) {
            if (ObjectUtil.isNotNull(reader)) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
            return ResultVoUtil.error(e.toString());
        }

    }


    @GetMapping("/deletePortTemp/{id}")
    @ApiOperation("删除端口模板")
    public ResultVo deletePortTemp(@PathVariable String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }
        PortTemp portTemp = tempService.getById(id);
        if (ObjectUtil.isNull(portTemp)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "未查询到对应模板信息");
        }
        String filePath = path + "/portTemp/";
        File file = new File(filePath + portTemp.getModelId() + "&&" + portTemp.getName());
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
        }
        tempService.removeById(id);
        return ResultVoUtil.success("删除成功");
    }


    @GetMapping("/downloadPortTemp/{id}")
    @ApiOperation("下载端口模板文件")
    public void downloadPortTemp(@PathVariable String id, HttpServletResponse response) {
        if (StringUtils.isEmpty(id)) {
            return;
        }
        PortTemp portTemp = tempService.getById(id);
        if (ObjectUtil.isNull(portTemp)) {
            return;
        }
        String filePath = path + "/portTemp/";
        FileInputStream inputStream = null;
        try {
            File file = new File(filePath + portTemp.getModelId() + "&&" + portTemp.getName());
            if (!file.exists()) {
                return;
            }
            inputStream = new FileInputStream(file);
            response.reset();
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + portTemp.getName());
            // 循环取出流中的数据
            byte[] b = new byte[100];
            int len;
            while ((len = inputStream.read(b)) > 0)
                response.getOutputStream().write(b, 0, len);
            inputStream.close();
        } catch (IOException e) {
            if (ObjectUtil.isNotNull(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
            log.error(e.toString());
        }
    }


    @PostMapping("/uploadPortTemp")
    @ApiOperation("上传端口模板文件")
    public void uploadPortTemp(@RequestParam("file") MultipartFile multipartFile, HttpServletResponse response) {
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            response.reset();
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + "template");
            // 循环取出流中的数据
            byte[] b = new byte[100];
            int len;
            while ((len = inputStream.read(b)) > 0)
                response.getOutputStream().write(b, 0, len);
            inputStream.close();
        } catch (IOException e) {
            if (ObjectUtil.isNotNull(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
            log.error(e.toString());
        }

    }


    @GetMapping("/getPortTempByModel/{id}")
    @ApiOperation("获取指定型号的模板列表")
    public ResultVo getPortTempByModel(@PathVariable String id) {
        QueryWrapper<PortTemp> qw = new QueryWrapper<>();
        qw.eq("MODEL_ID", id);
        List<PortTemp> list = tempService.list(qw);
        return ResultVoUtil.success(list);
    }
}