package com.jcca.web2.service;

import com.jcca.component.quartz.warn.bean.KeepAlarmVo;
import com.jcca.web2.vo.AlarmSocketVo;
import com.jcca.web2.vo.OptionSocketVo;

/**
 * @description: 大屏首页相关处理
 * @author: Lvyp
 * @create: 2023/11/16 09:22
 */
public interface IndexPageService {

    /**
     * 推送告警信息
     *
     * @param vo
     */
    public void sendAlarmMsg(AlarmSocketVo vo);

    /**
     * V1前端推送
     * @param vo
     */
    public void sendAlarmMsgV1(AlarmSocketVo vo);

    /**
     * 发送信息通知
     *
     * @param vo
     */
    public void sendNotify(OptionSocketVo vo);


}
