package com.jcca.web.xunjian.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.impl.OutServiceImpl;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Handler;
import com.jcca.web.xunjian.controller.bean.BeginXunJianReq;

import com.jcca.web.xunjian.dao.XunjianRecordDao;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import com.jcca.web.xunjian.entity.bean.XunjianLogBean;
import com.jcca.web.xunjian.entity.XunjianRecord;
import com.jcca.web.xunjian.entity.bean.XunjianServerDetailBean;
import com.jcca.web.xunjian.service.XunjianDetailV2Service;
import com.jcca.web.xunjian.service.XunjianRecordService;
import com.jcca.web.xunjian.service.bean.XunjianTab;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author yu_chen
 * @date 2021-02-24 16:12
 **/
@Slf4j
@Service
public class XunjianRecordServiceImpl extends ServiceImpl<XunjianRecordDao, XunjianRecord> implements XunjianRecordService {

}