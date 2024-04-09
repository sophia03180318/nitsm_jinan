package com.jcca.web2.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.service.PerformanceTargetService;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.file.FileUpload;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web2.dto.AssetModelDto;
import com.jcca.web2.entity.AssetCommand;
import com.jcca.web2.entity.AssetManufacturer;
import com.jcca.web2.entity.AssetMode;
import com.jcca.web2.entity.AssetModel;
import com.jcca.web2.service.AssetCommandService;
import com.jcca.web2.service.AssetManufacturerService;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.AssetModelService;
import com.jcca.web2.vo.AssetModelVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @description: 资产型号
 * @author: sophia
 * @create: 2023/11/02 14:43
 **/

@RestController
@RequestMapping("/api/v2/assetModel")
@Api(tags = "资产型号管理V2")
public class AssetModelControllerV2 {

    @Value("${project.upload.file-path}")
    private String filePath;
    @Value("${project.upload.static-url}")
    private String staticUrl;
    @Value("${project.upload.static-path}")
    private String staticPath;
    @Resource
    private AssetModelService modelService;
    @Resource
    private AssetService assetService;
    @Resource
    private AssetManufacturerService manufacturerService;
    @Resource
    private AssetModeService modeService;
    @Resource
    private SysFileService uploadService;
    @Resource
    private PerformanceTargetService targetService;
    @Resource
    private SpecDictionaryService specDictionaryService;
    @Resource
    private AssetCommandService commandService;


    /**
     * 由厂商或类型找型号
     *
     * @param dto 类型ID, 厂商ID
     * @return AssetModelVo
     */
    @PostMapping("/index")
    @ApiOperation("获取型号列表")
    public ResultVo<Object> index(@RequestBody AssetModelDto dto) {
        QueryWrapper<AssetModel> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(dto.getModel())) {
            queryWrapper.like("MODEL", dto.getModel());
        }
        if (!StringUtils.isEmpty(dto.getModeId())) {
            queryWrapper.eq("ASSET_MODE_ID", dto.getModeId());
        }
        if (!StringUtils.isEmpty(dto.getManufacturerId())) {
            queryWrapper.eq("MANUFACTURER_ID", dto.getManufacturerId());
        }

        queryWrapper.orderByDesc("MODIFY_TIME");
        List<AssetModel> list = modelService.list(queryWrapper);
        List<AssetModelVo> resList = new ArrayList<>();
        for (AssetModel assetModel : list) {
            AssetModelVo vo = new AssetModelVo();
            BeanUtils.copyProperties(assetModel, vo);
            resList.add(vo);
        }

        return ResultVoUtil.success(resList);
    }


    @GetMapping("/delete/{id}")
    @ApiOperation("删除型号")
    @RequiresPermissions("api:assetModel:delete")
    @ActionLog(name = "删除型号", title = "资产型号管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> delete(@PathVariable String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }
        AssetModel model = modelService.getById(id);
        if (ObjectUtil.isNull(model)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "未找到对应数据");
        }
        QueryWrapper<Asset> qw = new QueryWrapper<>();
        qw.eq("ASSET_IMAGE", model.getModel());
        qw.eq("IS_DEL", StatusConst.OK);
        List<Asset> list = assetService.list(qw);
        if (list.isEmpty()) {
            modelService.removeById(id);
            //删除关联文件
            if (!StringUtils.isEmpty(model.getPath())) {
                File file = new File(model.getPath());
                // 路径为文件且不为空则进行删除
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
            return ResultVoUtil.success("删除型号成功");
        } else {
            return ResultVoUtil.warning("已有该型号资产:[" + list.get(0).getName() + "]");
        }
    }


    @PostMapping("/add")
    @ApiOperation("新增型号")
    @RequiresPermissions("api:assetModel:add")
    @ActionLog(name = "新增型号", title = "资产型号管理", key = LogTypeConstant.ADD)
    public ResultVo<Object> add(@RequestBody @Validated AssetModel model) {
        AssetMode mode = this.getMode(model);
        Boolean f = this.checkName(model);
        if (!f) {
            throw new ResultException(ResultEnum.DUPLICATE.getCode(), "型号名称不能重复");
        }

        model.setAssetModeId(mode.getId());
        modelService.save(model);

        // 保存型号采集命令
//        Integer assetMode = mode.getAmode();
//        this.saveImagePerformanceTarget(assetMode, model);

        return ResultVoUtil.success("保存成功");
    }

    private void saveImagePerformanceTarget(Integer assetMode, AssetModel model) {
        String assetImage = model.getModel();
        Integer collectionType = model.getCollectionType();
        Long manufacturerId = model.getManufacturerId();
        QueryWrapper<SpecDictionary> dquery = Wrappers.query();
        dquery.eq("ASSET_MODE", assetMode);
        dquery.eq("ASSET_IMAGE", assetImage);
        dquery.eq("MANUFACTURER_ID", manufacturerId);
        dquery.eq("SYSTEM_TYPE", collectionType);
        List<SpecDictionary> dlist = specDictionaryService.list(dquery);
        if (CollectionUtils.isEmpty(dlist)) {
            dquery = Wrappers.query();
            dquery.eq("ASSET_MODE", assetMode);
            dquery.eq("ASSET_IMAGE", "PUB");
            dquery.eq("MANUFACTURER_ID", manufacturerId);
            dquery.eq("SYSTEM_TYPE", collectionType);
            dlist = specDictionaryService.list(dquery);
        }

        if (CollectionUtils.isEmpty(dlist)) {
            return;
        }
        SpecDictionary dictionary = dlist.get(0);

        QueryWrapper<PerformanceTarget> pquery = Wrappers.query();
        pquery.eq("ASSET_MODE", assetMode);
        pquery.eq("SPEC_ID", dictionary.getSpecId());
        pquery.eq("IS_AVAILABLE", StatusConst.OK);
        pquery.isNotNull("EXECUTION");
        List<PerformanceTarget> plist = targetService.list(pquery);
        if (CollectionUtils.isEmpty(plist)) {
            return;
        }
        List<AssetCommand> alist = new ArrayList<>();
        for (PerformanceTarget target : plist) {
            AssetCommand command = new AssetCommand(target, model, assetImage);
            alist.add(command);
        }
        QueryWrapper<AssetCommand> aquery = Wrappers.query();
        aquery.eq("ASSET_MODE", assetMode);
        aquery.eq("ASSET_IMAGE", assetImage);
        List<AssetCommand> list = commandService.list(aquery);
        if (CollectionUtils.isEmpty(list)) {
            commandService.saveBatch(alist);
        }

    }

    private Boolean checkName(AssetModel model) {
        QueryWrapper<AssetModel> query = Wrappers.query();
        query.eq("MODEL", model.getModel());
        if (!StringUtils.isEmpty(model.getId())) {
            query.ne("ID", model.getId());
        }
        List<AssetModel> list = modelService.list(query);
        return CollectionUtils.isEmpty(list);
    }


    @PostMapping("/edit")
    @ApiOperation("编辑型号")
    @RequiresPermissions("api:assetModel:edit")
    @ActionLog(name = "编辑型号", title = "资产型号管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> edit(@RequestBody AssetModel model) {
        if (StringUtils.isEmpty(model.getId())) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "型号ID不可为空");
        }
        Boolean f = this.checkName(model);
        if (!f) {
            throw new ResultException(ResultEnum.DUPLICATE.getCode(), "型号名称不能重复");
        }
        AssetMode mode = this.getMode(model);
        model.setAssetModeId(mode.getId());
        modelService.saveOrUpdate(model);

        // 保存型号采集命令
//        this.saveImagePerformanceTarget(mode.getAmode(), model);

        return ResultVoUtil.success("修改成功");
    }

    private AssetMode getMode(AssetModel model) {
        Date onlineTime = model.getOnlineTime();
        Date downLineTime = model.getDownlineTime();
        if (Objects.nonNull(onlineTime) && Objects.nonNull(downLineTime)) {
            if (DateUtil.compare(onlineTime, downLineTime) >= 0) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "下架时间不能早于上架时间");
            }
        }

        Integer desk = model.getDesk();
        AssetMode mode = modeService.getByCode(desk);
        if (Objects.isNull(mode)) {
            throw new ResultException(ResultEnum.CANNOT_FIND.getCode(), "资产类型不存在：" + desk);
        }
        return mode;
    }

    /**
     * 资产型号图片
     */
    @GetMapping("/download/{id}")
    @ApiOperation(value = "下载资产图片")
    public void downloadTemplate(HttpServletResponse response, @PathVariable String id) {
        if (StringUtils.isEmpty(id)) {
            return;
        }
        AssetModel model = modelService.getById(id);
        if (ObjectUtil.isNull(model)) {
            return;
        }
        try {
            // 下载本地文件
            String fileName = model.getFileName(); // 文件的默认保存名
            // 读到流中
            InputStream inStream = new FileInputStream(model.getPath());// 文件的存放路径
            // 设置输出的格式
            response.reset();
            response.setContentType("multipart/form-data");
            response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            // 循环取出流中的数据
            byte[] b = new byte[100];
            int len;
            try {
                while ((len = inStream.read(b)) > 0)
                    response.getOutputStream().write(b, 0, len);
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_TEMPLATE_DOWNLOAD, "文件地址: " + model.getPath(), e);
        }
    }


    /**
     * 删除资产图片
     */
    @GetMapping("/deleteFile/{id}")
    @ApiOperation(value = "删除型号图片")
    public ResultVo<Object> deleteFile(@PathVariable String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "ID不能为空");
        }
        AssetModel model = modelService.getById(id);
        if (ObjectUtil.isNull(model)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "未找到对应数据");
        }
        if (!StringUtils.isEmpty(model.getPath())) {
            File file = new File(model.getPath().replace(staticUrl, filePath));
            // 路径为文件且不为空则进行删除
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }

        model.setPath("");
        model.setFileName("");
        modelService.saveOrUpdate(model);
        return ResultVoUtil.success("删除成功");
    }

    /**
     * 上传资产图片
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传资产图片")
    @ActionLog(name = "上传资产图片", title = "资产型号管理", key = LogTypeConstant.MODIFY)
    public ResultVo<Object> upload(String id, @RequestParam("file") MultipartFile multipartFile) {
        if (StringUtils.isEmpty(id)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "型号ID不能为空");
        }
        AssetModel model = modelService.getById(id);
        if (ObjectUtil.isNull(model)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "未找到对应数据");
        }

        //上传文件
        if (multipartFile.getSize() == 0) {
            return ResultVoUtil.warning("文件上传失败-不能上传空文件");
        }
        String originalFilename = multipartFile.getOriginalFilename();
        if (StrUtil.isEmpty(originalFilename) || originalFilename.length() > 60) {
            return ResultVoUtil.warning("文件上传失败-文件名为空或超过60个字符");
        }

        try {
            String path = FileUpload.upload(filePath + "/assetModel", multipartFile);
            model.setFileName(multipartFile.getOriginalFilename());
            model.setPath(staticPath + path.replace(filePath, ""));
            //清除以往图片
            this.deleteFile(id);
            modelService.saveOrUpdate(model);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_TEMPLATE_DOWNLOAD, null, e);
            return ResultVoUtil.error("文件上传失败");
        }
        return ResultVoUtil.success("图片上传成功");

    }

    @GetMapping("/getModelMap/{code}")
    @ApiOperation("获取类型下型号列表")
    public ResultVo<Object> getModeMap(@PathVariable Integer code) {
        AssetMode mode = modeService.getByCode(code);
        if (Objects.isNull(mode)) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND);
        }
        Map<String, String> modelMap = modelService.getModelMapByModeId(mode.getId());
        return ResultVoUtil.success(modelMap);
    }

    @GetMapping("/getById/{modelId}")
    @ApiOperation("获取资产填充信息")
    public ResultVo<Object> getById(@PathVariable String modelId) {
        AssetModel one = modelService.getById(modelId);
        if (Objects.isNull(one)) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND.getCode(), "没有此型号：" + modelId);
        }
        Long manufacturerId = one.getManufacturerId();
        if (!StringUtils.isEmpty(manufacturerId)) {
            AssetManufacturer ma = manufacturerService.getById(manufacturerId);
            if (Objects.nonNull(ma)) {
                one.setManufacturerName(ma.getName());
            }
        }
        if (!StringUtils.isEmpty(one.getPath())) {
            one.setPath(staticUrl + one.getPath());
        }
        String assetModeId = one.getAssetModeId();
        AssetMode mode = modeService.getById(assetModeId);
        if (Objects.isNull(mode)) {
            return ResultVoUtil.error(ResultEnum.CANNOT_FIND.getCode(), "型号[" + one.getModel() + "]没有此类型：" + assetModeId);
        }
        one.setDesk(mode.getCode());
        return ResultVoUtil.success(one);
    }

    @PostMapping("/upload/image")
    @ResponseBody
    public ResultVo<Object> uploadImage(@RequestParam("file") MultipartFile multipartFile) {

        // 创建Upload实体对象
        SysFile upload = FileUpload.getFile(multipartFile, "/images");
        try {
            return saveImage(multipartFile, upload);
        } catch (IOException | NoSuchAlgorithmException e) {
            return ResultVoUtil.error("上传图片失败");
        }
    }

    /**
     * 保存上传的web格式图片
     */
    private ResultVo<Object> saveImage(MultipartFile multipartFile, SysFile upload) throws IOException, NoSuchAlgorithmException {
        // 判断是否为支持的图片格式
        String[] types = {
                "image/gif",
                "image/jpg",
                "image/jpeg",
                "image/png"
        };
        if (!FileUpload.isContentType(multipartFile, types)) {
            throw new ResultException(ResultEnum.NO_FILE_TYPE);
        }

        // 判断图片是否存在
        SysFile uploadSha1 = uploadService.getBySha1(FileUpload.getFileSha1(multipartFile));
        if (uploadSha1 != null) {
            uploadSha1.setFilePath(staticUrl + uploadSha1.getFilePath());
            return ResultVoUtil.success(uploadSha1);
        }

        FileUpload.transferTo(multipartFile, upload);
        // 将文件信息保存到数据库中
        upload.setStatus(StatusConst.OK);
        uploadService.save(upload);
        upload.setFilePath(staticUrl + upload.getFilePath());
        return ResultVoUtil.success(upload);
    }

}