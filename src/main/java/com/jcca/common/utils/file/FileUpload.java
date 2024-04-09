package com.jcca.common.utils.file;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import com.aspose.words.Document;
import com.aspose.words.PdfSaveOptions;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.common.utils.ToolUtil;
import com.jcca.common.utils.file.config.properties.UploadProjectProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传处理工具
 *
 * @author hanwone
 * @date 2018/11/4
 */
@Slf4j
public class FileUpload {

    private static final String PDF_TYPE = ".pdf";

    /**
     * 创建一个Upload实体对象
     *
     * @param multipartFile MultipartFile对象
     * @param modulePath    文件模块路径
     */
    public static SysFile getFile(MultipartFile multipartFile, String modulePath) {
        if (multipartFile.getSize() == 0) {
            throw new ResultException(ResultEnum.NO_FILE_NULL);
        }
        SysFile upload = new SysFile();
        upload.setMime(multipartFile.getContentType());
        upload.setFileSize(multipartFile.getSize());
        upload.setOrignName(multipartFile.getOriginalFilename());
        upload.setFileName(FileUpload.getFileName(multipartFile.getOriginalFilename()));
        upload.setFilePath(getPathPattern() + modulePath + genDateMkdir("yyyyMMdd") + upload.getFileName());
        return upload;
    }

    /**t
     * 判断文件是否为支持的格式
     *
     * @param multipartFile MultipartFile对象
     * @param types         支持的文件类型数组
     */
    public static boolean isContentType(MultipartFile multipartFile, String[] types) {
        List<String> typeList = Arrays.asList(types);
        return typeList.contains(multipartFile.getContentType());
    }

    /**
     * 获取文件上传保存路径
     */
    public static String getUploadPath() {
        UploadProjectProperties properties = SpringContextUtil.getBean(UploadProjectProperties.class);
        return properties.getFilePath();
    }

    /**
     * 获取文件上传目录的静态资源路径
     */
    private static String getPathPattern() {
        UploadProjectProperties properties = SpringContextUtil.getBean(UploadProjectProperties.class);
        return properties.getStaticPath();
    }

    /**
     * 获取访问文件地址
     */
    private static String getStaticUrl() {
        UploadProjectProperties properties = SpringContextUtil.getBean(UploadProjectProperties.class);
        return properties.getStaticUrl();
    }

    /**
     * 生成随机且唯一的文件名
     */
    private static String getFileName(String originalFilename) {
        String fileSuffix = ToolUtil.getFileSuffix(originalFilename);
        return UUID.randomUUID().toString().replace("-", "") + fileSuffix;
    }

    /**
     * 生成指定格式的目录名称(日期格式)
     */
    private static String genDateMkdir(String format) {

        return "/" + DateUtil.format(new Date(), format) + "/";
    }

    /**
     * 获取目标文件对象
     *
     * @param upload 上传实体类
     */
    public static File getDestFile(SysFile upload) throws IOException {

        // 创建保存文件对象
        String path = upload.getFilePath().replace(getPathPattern(), "");
        String filePath = getUploadPath() + path;
        File dest = new File(filePath.replace("//", "/"));
        if (!dest.exists()) {
            dest.getParentFile().mkdirs();
            dest.createNewFile();
        }

        return dest;
    }

    /**
     * 获取目标文件对象 不存在抛出异常
     *
     * @param upload 上传实体类
     */
    public static File getFile(SysFile upload) throws IOException {

        // 创建保存文件对象
        String path = upload.getFilePath().replace(getPathPattern(), "");
        String filePath = getUploadPath() + path;
        File dest = new File(filePath.replace("//", "/"));
        if (!dest.exists()) {
            throw new IOException("文件不存在：" + filePath);
        }

        return dest;
    }

    /**
     * 获取文件真实存放位置
     *
     * @param upload
     * @return
     */
    public static String getFilePath(SysFile upload) {
        String path = upload.getFilePath().replace(getPathPattern(), "");
        String filePath = getUploadPath() + path;
        return filePath;
    }

    /**
     * 保存文件及获取文件MD5值和SHA1值
     *
     * @param multipartFile MultipartFile对象
     * @param upload        Upload
     */
    public static void transferTo(MultipartFile multipartFile, SysFile upload)
            throws IOException, NoSuchAlgorithmException {

        byte[] buffer = new byte[4096];
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        try (OutputStream fos = Files.newOutputStream(getDestFile(upload).toPath());
             InputStream fis = multipartFile.getInputStream()) {
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                md5.update(buffer, 0, len);
                sha1.update(buffer, 0, len);
            }
            fos.flush();
        }
        BigInteger md5Bi = new BigInteger(1, md5.digest());
        BigInteger sha1Bi = new BigInteger(1, sha1.digest());
        upload.setMd5(md5Bi.toString(16));
        upload.setSha1(sha1Bi.toString(16));
    }

    /**
     * 获取文件的SHA1值
     */
    public static String getFileSha1(MultipartFile multipartFile) {
        if (multipartFile.getSize() == 0) {
            throw new ResultException(ResultEnum.NO_FILE_NULL);
        }
        byte[] buffer = new byte[4096];
        try (InputStream fis = multipartFile.getInputStream()) {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                sha1.update(buffer, 0, len);
            }
            BigInteger sha1Bi = new BigInteger(1, sha1.digest());
            return sha1Bi.toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 移动文件
     *
     * @param resourceFilePath 源文件地址
     * @param targetPath       保存的目标路径
     * @throws Exception
     */
    public static void moveFile(String resourceFilePath, String targetPath) throws Exception {
        // 创建保存文件对象
        String path = resourceFilePath.replace(getPathPattern(), "");
        String filePath = getUploadPath() + path;
        File src = new File(filePath.replace("//", "/"));

        String target = targetPath.replace(getPathPattern(), "");
        String newfilePath = getUploadPath() + target;

        File dest = new File(newfilePath.replace("//", "/"));
        if (!src.exists()) {
            throw new Exception("源文件不存在");
        }
        if (!dest.exists()) {
            dest.mkdirs();
        }
        try {
            FileUtil.move(src, dest, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("移动文件失败：" + e.getMessage());
        }
    }

    /**
     * 获取前端访问路径
     *
     * @return
     */
    public static String getViewPath(SysFile upload) {
        return getStaticUrl() + upload.getFilePath();
    }

    /**
     * 下载文件
     *
     * @param response
     * @param filePath
     * @param desName
     * @return
     */
    public static String downloadFile(HttpServletResponse response, String filePath, String desName) {
        try {
            desName = new String(desName.getBytes("GB2312"), "ISO_8859_1");
            response.reset();
            response.setHeader("Content-Disposition", "attachment;fileName=" + desName);
            InputStream inStream = new FileInputStream(filePath);
            OutputStream os = response.getOutputStream();
            byte[] buff = new byte[1024];
            int len;
            while ((len = inStream.read(buff)) > 0) {
                os.write(buff, 0, len);
            }

            os.flush();
            os.close();
            inStream.close();
            return "文件下载成功";
        } catch (Exception var7) {
            return "文件下载异常";
        }
    }

    /**
     * 获取文件预览地址
     *
     * @return
     * @throws Exception
     */
    public static String getPreviewUrl(SysFile sysFile) throws Exception {
        // 创建保存文件对象
        String path = sysFile.getFilePath().replace(getPathPattern(), "");

        // 文件真实地址
        String filePath = getUploadPath() + path;

        String pathNoType = filePath.substring(0, filePath.indexOf("."));
        String pdfPath = pathNoType + PDF_TYPE;

        String fileFormtPath = filePath.replace("//", "/");
        String pdfFormtPath = pdfPath.replace("//", "/");

        File dest = new File(fileFormtPath);
        if (!dest.exists()) {
            throw new Exception("文件不存在");
        }

        // 如果不是doc类型全部返回
        List<String> typeList = Arrays.asList("docx", "doc");
        String type = FileTypeUtil.getType(dest);
        if (!typeList.contains(type)) {
            return getViewPath(sysFile);
        }

        File pafFile = new File(pdfFormtPath);
        if (!pafFile.exists()) {
            // 生成pdf
            try {
                transformDoc(fileFormtPath, pdfFormtPath);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new Exception("文件转换失败");
            }

            return getStaticUrl() + pdfFormtPath.replace(getUploadPath(), getPathPattern() + "/");
        } else {
            return getStaticUrl() + pdfPath.replace(getUploadPath(), getPathPattern());
        }
    }

    /**
     * 将doc文件转换为Pdf
     *
     * @return
     * @throws Exception
     */
    private static String transformDoc(String filePath, String savePath) throws Exception {
        Document doc = new Document(filePath);
        PdfSaveOptions options = new PdfSaveOptions();

        FileOutputStream os = new FileOutputStream(new File(savePath));
        doc.save(os, options);
        os.close();
        return savePath;
    }

    /**
     * 文件上传
     *
     * @param baseDir 相对应用的基目录
     * @param file    上传的文件
     * @return 返回上传成功的文件名
     */
    public static String upload(String baseDir, MultipartFile file) throws IOException {
        //获取上传地址
        String fileName = getFileName(file.getOriginalFilename());
        String path=baseDir+"/"+fileName;
        File dest = new File(path.replace("//", "/"));
        if (!dest.exists()) {
            dest.getParentFile().mkdirs();
            dest.createNewFile();
        }
        byte[] buffer = new byte[4096];
        try (OutputStream fos = Files.newOutputStream(dest.toPath());
             InputStream fis = file.getInputStream()) {
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        }

        return path;
    }


    /**
     * 文件上传
     *
     * @param baseDir 相对应用的基目录
     * @param file    上传的文件
     * @return 返回上传成功的文件名
     */
    public static String upload(String baseDir, String fileName,MultipartFile file) throws IOException {
        String path=baseDir+"/"+fileName;
        File dest = new File(path.replace("//", "/"));
        if (!dest.exists()) {
            dest.getParentFile().mkdirs();
            dest.createNewFile();
        }
        byte[] buffer = new byte[4096];
        try (OutputStream fos = Files.newOutputStream(dest.toPath());
             InputStream fis = file.getInputStream()) {
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        }

        return path;
    }

}
