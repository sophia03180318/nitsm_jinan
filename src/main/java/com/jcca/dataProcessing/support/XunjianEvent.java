package com.jcca.dataProcessing.support;

import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.web2.dto.xunjian.XunjianDataDto;
import lombok.Data;


/**
 * 事件类，向后传递的事件信息
 */
@Data
public class XunjianEvent {
    CommonEntity info;

    XunjianDataDto dto;

}
