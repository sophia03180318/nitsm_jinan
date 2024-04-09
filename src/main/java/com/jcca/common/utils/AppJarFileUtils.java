package com.jcca.common.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.common.utils.file.FileUpload;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 传输升级包工具
 *
 * @author lyp
 */
@Slf4j
public class AppJarFileUtils {

    /**
     * byte[]转file
     *
     * @param basePath 文件路径  eg: /home/tmp/
     * @return
     */
    public static String saveByteToFile(byte[] fileByte, String basePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        String filePath = basePath + fileName;
        try {
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(fileByte);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    log.error(e1.getMessage(), e1);
                }
            }
        }
        return filePath;
    }

    /**
     * 将文件按照大小组成小文件
     *
     * @param finishSize 已经读取完成的大小
     * @param rateLimi   限速大小（单次读取的文件大小）
     * @return
     * @throws IOException
     */
    public static byte[] getBytes(SysFile sysFile, Integer finishSize, Integer rateLimi) throws IOException {
        byte[] buffer = null;
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            File file = FileUpload.getFile(sysFile);

            fileInputStream = new FileInputStream(file);
            if (Objects.nonNull(finishSize) && finishSize.intValue() != 0) {
                int n = finishSize;
                fileInputStream.skip(n);
            }

            Long length = file.length();
            byteArrayOutputStream = new ByteArrayOutputStream(length.intValue());

            byte[] b = new byte[rateLimi];

            // 返回-1表示已经读取完成
            int read = fileInputStream.read(b);
            if (read != -1) {
                byteArrayOutputStream.write(b, 0, read);
                byteArrayOutputStream.flush();
                buffer = byteArrayOutputStream.toByteArray();
            }
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
        }

        return buffer;
    }

    /**
     * 将文件流发送至另外服务器的方法
     *
     * @param fileName 文件路径
     * @return 从服务器端响应的流 可通过 new String(bytes); 转换
     */
    public static void httpPost(String subFilePath, String fileName, List<String> uploadUrl, String version, String savePath, Integer finishSize)
            throws Exception {
        HashMap<String, Object> paramMap = new HashMap<>();
        File file = FileUtil.file(subFilePath);

        paramMap.put("file", file);
        paramMap.put("fileName", fileName);
        paramMap.put("version", version);
        paramMap.put("basePath", savePath);
        if (finishSize.intValue() == 0) {
            paramMap.put("isOnce", "isOnce");
        } else {
            paramMap.put("isOnce", "MORE");

        }
        String result =sendPost(uploadUrl,paramMap);
        if(Objects.isNull(result)){
            throw new Exception("升级失败，检查车站网络");
        }

        log.info("文件上传结果：" + result);
        if (!JSONUtil.isJson(result)) {
            throw new Exception("上传失败-返回信息格式错误：" + result);
        }
        JSONObject resp = JSONUtil.parseObj(result);
        String code = resp.getStr("code");
        String msg = resp.getStr("msg");
        if ("success".equals(code)) {
            return;
        }
        throw new Exception("上传失败：" + msg);
    }


    private static String sendPost(List<String> uploadUrl,HashMap<String, Object> paramMap){
        for (String url : uploadUrl) {
            try {
                String result = HttpUtil.post(url, paramMap);
                return result;
            }catch (Exception e){
                log.error("车站发送文件异常:"+e.toString(),e);
            }
        }
        return null;
    }

}
