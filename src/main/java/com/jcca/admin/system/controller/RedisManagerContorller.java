package com.jcca.admin.system.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.jcca.admin.system.vo.RedisManagerVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author：sophia
 * @ Date：Created in 16:21 2022/5/9
 * @ Description:
 */
@Controller
@Slf4j
@RequestMapping("/system/redisManager")
public class RedisManagerContorller {
    @Resource
    private RedisService redisService;
    @Resource
    private AssetService assetService;


    /**
     * 跳转到列表页面
     */
    @GetMapping("/index")
    @RequiresPermissions("system:redisManager:index")
    @ActionLog(name = "查看redis缓存列表", title = "缓存管理", key = LogTypeConstant.QUERY)
    public String index(Model model, RedisManagerVo redis) {
        String pattern = "*";
        if (ObjectUtil.isNotNull(redis) && ObjectUtil.isNotNull(redis.getKey()) && redis.getKey().trim().length() > 0) {
            pattern = "*" + redis.getKey() + "*";
        }
        ArrayList<RedisManagerVo> redisAllData = new ArrayList<>();
        List<String> keys = redisService.getKeyByPattern(pattern);
        int num = 30;
        int i = 0;
        if (ObjectUtil.isNotNull(keys) && !keys.isEmpty()) {
            if (ObjectUtil.isNull(redis.getType()) || (redis.getType() != 2 && redis.getType() != 3)) {
                redisAllData = redisService.getRedisAllData(keys, num);
            } else if (redis.getType() == 2) {//hash
                for (String key : keys) {
                    try {
                        Map<String, Object> hashMap = redisService.getHashMap(key);
                        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                            RedisManagerVo hashRedis = new RedisManagerVo();
                            hashRedis.setKey(key + ":" + entry.getKey());
                            hashRedis.setValue(entry.getValue().toString());
                            redisAllData.add(hashRedis);
                            i++;
                            if (i >= num) {
                                model.addAttribute("list", redisAllData);
                                return "/system/redisManager/index";
                            }

                        }
                    } catch (RedisSystemException e2) {
                    }
                }
            } else {//set
                for (String key : keys) {
                    try {
                        Cursor<String> cursor = redisService.getSet(key);
                        while (cursor.hasNext()) {
                            RedisManagerVo zsetRedis = new RedisManagerVo();
                            zsetRedis.setKey(key + "->" + cursor.getPosition());
                            zsetRedis.setValue(cursor.next());
                            redisAllData.add(zsetRedis);
                            i++;
                            if (i >= num) {
                                model.addAttribute("list", redisAllData);
                                return "/system/redisManager/index";
                            }
                        }
                    } catch (RedisSystemException e3) {

                    }
                }

            }
        }
        model.addAttribute("list", redisAllData);
        return "/system/redisManager/index";
    }

    /**
     * 修改key
     *
     * @return
     */
    @GetMapping("/update")
    @ActionLog(name = "修改缓存KEY", title = "缓存管理", key = LogTypeConstant.MODIFY)
    public String update(String key, Model model) {
        RedisManagerVo redisManagerVo = new RedisManagerVo();
        if (ObjectUtil.isNotNull(key) && redisService.exists(key)) {
            redisManagerVo.setKey(key);
            Object o = redisService.get(key);
            if (ObjectUtil.isNotNull(o)) {
                String str = o.toString();
                if (JSONUtil.isJson(str)) {
                    str = JsonFormart(str);
                }
                redisManagerVo.setValue(str);
                model.addAttribute("redis", redisManagerVo);
            }
        }
        return "/system/redisManager/update";
    }

    /**
     * 删除key
     *
     * @return
     */
    @PostMapping("/deleteKeys")
    @ResponseBody
    @ActionLog(name = "删除缓存KEY", title = "缓存管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<?> deleteKeys(String key) {
        if (ObjectUtil.isNotNull(key)) {
            redisService.removePattern("*" + key + "*");
        }
        return ResultVoUtil.success("删完了");
    }


    /**
     * 根据IP查询ID  或者根据ip查询id
     *
     * @return
     */
    @PostMapping("/findByIp")
    @ResponseBody
    @ActionLog(name = "根据IP查询ID", title = "缓存管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<?> findByIp(String ip) {
        try {
            Asset asset = assetService.findOneByIp(ip.trim());
            if (ObjectUtil.isNull(asset)) {
                asset = assetService.getById(ip.trim());
                return ResultVoUtil.success(asset.getIp());
            }
            return ResultVoUtil.success(asset.getId());
        } catch (Exception e) {
            return ResultVoUtil.success("没查着~");
        }
    }

    @PostMapping("/save")
    @ResponseBody
    @ActionLog(name = "修改缓存数据", title = "缓存管理", key = LogTypeConstant.MODIFY)
    public ResultVo save(RedisManagerVo redis) {
        String value = redis.getValue();
        if (ObjectUtil.isNull(value) || value.trim().length() == 0) {
            return ResultVoUtil.error("value不可为空,如若不需请直接删除键值");
        }
        String key = redis.getKey();
        if (ObjectUtil.isNotNull(key) && redisService.exists(key)) {
            Object o = redisService.get(key);
            //以前是Json格式字符串
            if (ObjectUtil.isNotNull(o) && JSONUtil.isJson(o.toString())) {
                if (!JSONUtil.isJson(value)) {
                    return ResultVoUtil.error("VALUE JSON格式错误,请检查!");
                }
                //格式再给改回来.....
                String value2 = value.replace("\r", "").replace("\n", "").replace(" ", "").replace("\t", "");
                if (JSONUtil.isJson(value2)) {
                    redisService.set(key, value2);
                } else {
                    return ResultVoUtil.error("VALUE JSON格式错误,请检查!");
                }
            } else {//以前是普通字符串
                redisService.set(key, value);
            }
        } else {
            return ResultVoUtil.error("redis不存在键: " + key);
        }
        return ResultVoUtil.success("修改成功");
    }

    @PostMapping("/addRedis")
    @ResponseBody
    @ActionLog(name = "新增缓存数据", title = "缓存管理", key = LogTypeConstant.REMOVEE)
    public ResultVo addRedis(@Validated RedisManagerVo redis) {
        String key = redis.getKey().trim();
        String value = redis.getValue().trim();
        if (key.length() == 0 || value.length() == 0) {
            return ResultVoUtil.error("KEY:VALUE 不可为空");
        }
        if (ObjectUtil.isNotNull(redis.getExpireTime()) && StringUtils.isNumber(redis.getExpireTime().trim())) {
            Long time = Long.valueOf(redis.getExpireTime().trim());
            redisService.set(key, value, time);
        } else {
            redisService.set(key, value);
        }
        return ResultVoUtil.success("保存成功~");
    }


    /**
     * 跳转到添加页面
     */
    @GetMapping("/add")
    public String toAdd() {
        return "/system/redisManager/add";
    }

    /**
     * 删除key
     *
     * @return
     */
    @GetMapping("/deleteByKey")
    @ResponseBody
    @RequiresPermissions("system:redisManager:deleteByKey")
    @ActionLog(name = "删除缓存KEY", title = "缓存管理", key = LogTypeConstant.REMOVEE)
    public ResultVo deleteByKey(String key) {
        if (ObjectUtil.isNotNull(key)) {
            redisService.remove(key);
        }
        return ResultVoUtil.success("删除成功~");
    }

    /**
     * 清空队列缓存
     *
     * @return
     */
    @GetMapping("/flushAll")
    @ResponseBody
    @RequiresPermissions("system:redisManager:flushAll")
    @ActionLog(name = "清空所有队列缓存", title = "缓存管理", key = LogTypeConstant.REMOVEE)
    public ResultVo flushAll() {
        redisService.removePattern("*" + RedisQueueConst.ALARM_QUEUE + "*");
        redisService.removePattern("*" + RedisQueueConst.ALARM_PUSH_FRONT_QUEUE + "*");
        redisService.removePattern("*" + RedisQueueConst.BROKER_QUEUE_KEY + "*");
        redisService.removePattern("*" + RedisQueueConst.EVENT_GROUP_ALARM + "*");
        redisService.removePattern("*" + RedisQueueConst.EVENT_GROUP_ALARM_ADD + "*");
        redisService.removePattern("*" + RedisQueueConst.EVENT_GROUP_ALARM_EXE + "*");
        redisService.removePattern("*" + RedisQueueConst.THRESHOLD_QUEUE + "*");
        return ResultVoUtil.success("清空成功~");
    }

    /**
     * 清空采集器标记位
     *
     * @return
     */
    @GetMapping("/pushAll")
    @ResponseBody
    @RequiresPermissions("system:redisManager:pushAll")
    @ActionLog(name = "清空采集器标记位", title = "缓存管理", key = LogTypeConstant.REMOVEE)
    public ResultVo pushAll() {
        redisService.deleteStatus();
        return ResultVoUtil.success("同步成功~");
    }


    /**
     * JSON格式化输出
     *
     * @param json
     * @return
     */
    private static String JsonFormart(String json) {
        int level = 0;
        //存放格式化的json字符串
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int index = 0; index < json.length(); index++)//将字符串中的字符逐个按行输出
        {
            //获取s中的每个字符
            char c = json.charAt(index);

            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    /**
     * 日志等级替换成制表符
     *
     * @param level
     * @return
     */
    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }
}

