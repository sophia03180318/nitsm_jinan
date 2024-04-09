package com.jcca.common.enums;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.utils.AppMathUtil;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 单位 从b转换到对应单位需要的进制
 *
 * @author Lvyp
 */
@Getter
public enum UnitEnum {
    /**
     * b单位
     */
    b(1L),
    /**
     * b转为KB
     */
    KB(8192L),
    /**
     * b转为MB
     */
    MB(8388608L),
    /**
     * b转为GB
     */
    GB(8589934592L),
    /**
     * 自适应
     */
    AUTO(0L);

    private Long scale;

    private UnitEnum(Long scale) {
        this.scale = scale;
    }


    /**
     * raid 单位转换
     * @author zyh
     * @param size
     * @return
     */
    public static String getNetFileSizeDescription(long size) {
        BigDecimal bigDecimal = new BigDecimal(size);
        if (size >= 1099511627776L) {
            return bigDecimal.divide(new BigDecimal(1099511627776L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "TB";
        } else if (size >= 1073741824L) {
            return bigDecimal.divide(new BigDecimal(1073741824L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "GB";
        } else if (size >= 11048576L) {
            return bigDecimal.divide(new BigDecimal(1048576L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "MB";
        } else if (size >= 1024L) {
            return bigDecimal.divide(new BigDecimal(1024L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "KB";
        } else {
            return size + "B";
        }
    }

    /**
     * 自动适应单位
     *
     * @param b
     * @return
     */
    public static String AutoScale(Long b) {
        UnitEnum[] unitList = UnitEnum.values();
        String scale = "";
        for (UnitEnum item : unitList) {
            if (item == UnitEnum.AUTO) {
                continue;
            }
            String div = AppMathUtil.div(b, item.getScale(), 2);
            if (StrUtil.startWith(div, "0")) {
                return scale;
            }
            scale = div + item.name();
        }
        return scale;

    }

    /**
     * kb自动适应单位 注意这里入参是KB
     * AIX专用
     *
     * @param kb
     * @return
     */
    public static String autoScaleKb(Long kb) {
        UnitEnum[] unitList = UnitEnum.values();
        String scale = "";
        for (UnitEnum item : unitList) {
            if (item == UnitEnum.AUTO || item == UnitEnum.KB || item == UnitEnum.b) {
                continue;
            }
            Long scale2 = item.getScale();
            BigDecimal divide = new BigDecimal(scale2).divide(new BigDecimal(8192), 0, BigDecimal.ROUND_HALF_DOWN);

            String div = AppMathUtil.div(kb, divide.longValue(), 0);
            if (StrUtil.startWith(div, "0")) {
                return scale;
            }
            scale = div + item.name();
        }
        return scale;

    }

}
