package com.jcca.component.client.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.component.client.AsmiAgent;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * ASMI 客户端
 */
@Service
public class AsmiAgentImpl implements AsmiAgent {

    @Resource
    private AssetService assetServ;
    @Resource
    private RedisService redisServ;

    @Override
    public String getCookie(String assetId) throws Exception{
        Asset asset = assetServ.getById(assetId);
        if(Objects.isNull(asset)){
            throw new Exception("设备不存在");
        }
        String userName = asset.getIpmiUser();
        String password = asset.getIpmiPwd();
        String ipmiIp = asset.getIpmiIp();

        String asmiUrl = getUrl(asset.getAssetImage(), ipmiIp);
        if(StrUtil.isEmpty(userName)||StrUtil.isEmpty(password)||StrUtil.isEmpty(asmiUrl)){
            throw new Exception("ASMI 管理口信息配置不全,无法查看日志");
        }

        password = EncryptUtil.aesDecryptStr(password);

        String cacheKey = "ASMI_COOKIE:"+ipmiIp;
        if(Objects.nonNull(redisServ.get(cacheKey))){
            return redisServ.get(cacheKey).toString();
        }

        String req = String.format("CSRF_TOKEN=0&user=%s&password=%s&lang=0&login=Log+in",userName,password);
        HttpResponse execute = HttpUtil.createPost(asmiUrl).body(req, "application/x-www-form-urlencoded").timeout(10000).execute();
        String cookieStr = execute.getCookieStr();
        String str = cookieStr.split(";")[0];

        if(StrUtil.isEmpty(str)){
            throw new Exception("获取失败，稍后再试");
        }

        redisServ.set(cacheKey,cookieStr,5*60L);

        return str;
    }

    @Override
    public String getUrl(String assetId) throws Exception {
        Asset asset = assetServ.getById(assetId);
        if(Objects.isNull(asset)){
            throw new Exception("设备不存在");
        }
        String ipmiIp = asset.getIpmiIp();

        return getUrl(asset.getAssetImage(), ipmiIp);

    }

    @Override
    public String getEventLog(String cookie,String assetId) {
        try {
            String url = getUrl(assetId);
            HttpResponse result = HttpUtil.createGet(url + "?form=31").header("Cookie", cookie).timeout(10000).execute();
            String body = result.body();
            int end = body.indexOf("</table>");
            int start = body.indexOf("</form>");

            String head = body.substring(0, end);
            String hind = body.substring(start, body.length());

            body = head +hind;
            return body.replace("</body>","<style>.div-box{color: #FFFFFF;}</style>");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getDetail(String cookie, List<String> eventIds, String csrfToken, String assetId) {
        StringBuilder str = new StringBuilder("");
        for (String eventId : eventIds) {
            str.append(eventId);
            str.append(":on");
            str.append("&");
        }
        String substring = str.substring(0, str.length() - 1);
        try {
            String url = getUrl(assetId);
            String req = String.format("form=31&%s&CSRF_TOKEN=%s&det=Show+details",substring,csrfToken);
            HttpResponse result = HttpUtil.createPost(url).header("Cookie", cookie).body(req, "application/x-www-form-urlencoded").timeout(10000).execute();
            return result.body();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取请求地址
     * @param assetImage
     * @return
     */
    private String getUrl(String assetImage,String ipmiIp){
        //后续有差异的设备可以根据型号来区分
        if("".equals(assetImage)){
            return "";
        }
        return String.format("https://%s/cgi-bin/cgi",ipmiIp);
    }


}
