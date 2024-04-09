package com.jcca.admin.system.controller;

import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.file.FileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @date 2018/11/02
 */
@Controller
public class UploadController {

    @Resource
    private SysFileService uploadService;

    /**
     * 上传web格式图片
     */
    @PostMapping("/upload/image")
    @ResponseBody
    public ResultVo uploadImage(@RequestParam("image") MultipartFile multipartFile) {

        // 创建Upload实体对象
        SysFile upload = FileUpload.getFile(multipartFile, "/images");
        try {
            return saveImage(multipartFile, upload);
        } catch (IOException | NoSuchAlgorithmException e) {
            return ResultVoUtil.error("上传图片失败");
        }
    }

    /**
     * 上传web格式头像
     */
    @PostMapping("/upload/picture")
    @ResponseBody
    public ResultVo uploadPicture(@RequestParam("picture") MultipartFile multipartFile) {

        // 创建Upload实体对象
        SysFile upload = FileUpload.getFile(multipartFile, "/picture");
        try {
            return saveImage(multipartFile, upload);
        } catch (IOException | NoSuchAlgorithmException e) {
            return ResultVoUtil.error("上传头像失败");
        }
    }

    /**
     * 保存上传的web格式图片
     */
    private ResultVo saveImage(MultipartFile multipartFile, SysFile upload) throws IOException, NoSuchAlgorithmException {
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
            return ResultVoUtil.success(uploadSha1);
        }

        FileUpload.transferTo(multipartFile, upload);
        // 将文件信息保存到数据库中
        uploadService.save(upload);
        return ResultVoUtil.success(upload);
    }

}
