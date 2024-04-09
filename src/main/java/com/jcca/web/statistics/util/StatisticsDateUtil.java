package com.jcca.web.statistics.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 汇总时间工具
 *
 * @author Lvyp
 */
public class StatisticsDateUtil {

    /**
     * 获取开始结束时间之间按照偏移量计算出的所有分割时间
     *
     * @param beginDay
     * @param endDay
     * @param dateField
     * @param quantity
     * @return
     */
    public static List<Date> paragraph(Date beginDay, Date endDay, DateField dateField, Integer quantity) {
        long between = DateUtil.between(beginDay, endDay, DateUnit.MS, false);
        Assert.isTrue(between > 0, "结束时间必须大于开始时间");
        List<Date> dateTimes = new ArrayList<Date>();
        dateTimes.add(beginDay);
        while (between > 0 || between == 0) {
            between = DateUtil.between(beginDay, endDay, DateUnit.MS, false);
            beginDay = DateUtil.offset(beginDay, dateField, quantity);
            if (between > 0 || between == 0) {
                dateTimes.add(beginDay);
            }
        }
        return dateTimes;
    }

}
