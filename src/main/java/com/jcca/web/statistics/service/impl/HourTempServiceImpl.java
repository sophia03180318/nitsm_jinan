package com.jcca.web.statistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.statistics.dao.HourTempMapper;
import com.jcca.web.statistics.entity.HourTemp;
import com.jcca.web.statistics.service.HourTempService;
import org.springframework.stereotype.Service;

/**
 * @author GodWone
 * @description TODO
 * @className HourTempServiceImpl
 * @date 2022/11/15 11:44
 * @since 2.0.0.1
 */
@Service
public class HourTempServiceImpl extends ServiceImpl<HourTempMapper, HourTemp> implements HourTempService {
}
