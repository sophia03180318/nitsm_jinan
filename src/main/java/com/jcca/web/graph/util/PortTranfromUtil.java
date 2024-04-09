package com.jcca.web.graph.util;

import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.graph.controller.bean.GroupPort;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ Author：sophia
 * @ Date：Created in 9:21 2023/6/29
 * @ Description:
 */
public class PortTranfromUtil {
    //将原有AssetPortVolist=>NAME名称转换后的 list
    public static List<AssetPortVo> tranfromIntger(List<AssetPortVo> ports) {

        Map<String, String> PortsnameAndIntger = new HashMap<String, String>();
        ArrayList<GroupPort> groupPorts = new ArrayList<GroupPort>();
        for (AssetPortVo assetPortVo : ports) {
            assetPortVo.setPortSlugName(assetPortVo.getPortName());
            String groupId = getGroupId(assetPortVo.getPortName());
            if (!groupId.isEmpty()) {
                groupPorts.add(new GroupPort(groupId, assetPortVo.getPortName()));
            } else {
                String portName = assetPortVo.getPortName();
                String headStr = "";
                if (portName.contains("/")) { //最后两位 不含有非零数字
                    headStr = Pattern.compile("[0-9]+/").split(portName)[0];
                } else {//不含有 /
                    headStr = Pattern.compile("[0-9]").split(portName)[0];
                }
                String num = portName.replace(headStr, "");
                String name = portName.substring(0, 1) + num;
                PortsnameAndIntger.put(portName, name);
            }
        }
        // map=>id+trouble/double,(portNameList)
        Map<String, List<String>> groupIdAndPortName = groupPorts.stream().collect(Collectors
                .groupingBy(GroupPort::getGroupId, Collectors.mapping(GroupPort::getPortName, Collectors.toList())));

        String postfix = "";

        for (List<String> portNameList : groupIdAndPortName.values()) {// 每次拉出来同一个组的portName
            Collections.sort(portNameList, new Comparator<String>() {// 排序  按照大小排序
                @Override
                public int compare(String portName1, String portName2) {
                    int[] nameArr1 = tarnfrom(portName1);
                    int[] nameArr2 = tarnfrom(portName2);
                    if (nameArr1[0] > nameArr2[0]) {
                        return 1;
                    } else if (nameArr1[0] == nameArr2[0]) {
                        return nameArr1[0] - nameArr2[0];
                    } else {
                        return -1;
                    }
                }
            });
            String[] portNameArr = portNameList.toArray(new String[portNameList.size()]);
            //对排序后的序列进行计算
            int num = 1;
            for (int i = 0; i < portNameArr.length; i++) {
                //排除 0/1 -> 0/3这种zz跨越   比较上一个name是不是正常增值
                if (i == 0) {
                    num = tarnfrom(portNameArr[0])[1];
                } else {
                    num = getNum(portNameArr[i - 1], portNameArr[i], num);
                }

                //处理后缀冒号:
                if (portNameArr[i].contains(":")) {
                    postfix = ":" + portNameArr[i].split("\\:")[1];
                }

                PortsnameAndIntger.put(portNameArr[i], portNameArr[i].substring(0, 1) + num + postfix);
            }
        }
        ArrayList<AssetPortVo> portList = new ArrayList<>();
        for (AssetPortVo assetPortVo : ports) {
            if (PortsnameAndIntger.containsKey(assetPortVo.getPortName())) {
                assetPortVo.setPortName(PortsnameAndIntger.get(assetPortVo.getPortName()));
            }
            //修改状态，兼容除了1之外的通的状态
            if (Objects.nonNull(assetPortVo.getStatus())) {
                if (!InterfaceStatus.isUp(assetPortVo.getStatus().byteValue())) {
                    assetPortVo.setStatus(InterfaceStatus.NO.getCode().intValue());
                } else if (InterfaceStatus.UNKINOW.getCode() != assetPortVo.getStatus().byteValue()) {
                    assetPortVo.setStatus(InterfaceStatus.OK.getCode().intValue());
                }
            }
            portList.add(assetPortVo);
        }
        return portList;
    }

    //根据portName  返回groupId 即: headStr + "double"/"trouble";
    private static String getGroupId(String portName) {
        boolean matches = false;
        String[] portArr = portName.split("/");// 按照 / 进行字符串切割
        for (int i = 1; i < portArr.length; i++) {
            if (Pattern.compile("[1-9]").matcher(portArr[i]).find()) {
                matches = true;
            }
        }

        String groupId = "";
        if (portName.contains("/") && matches) {// 含有/ 且 含有1~9的数字
            if (portArr.length == 2) { // id n / n
                String headStr = Pattern.compile("[0-9]+/").split(portName)[0];
                groupId = headStr + "double";
            } else if (portArr.length == 3) { // 三部分模式  第一个数字 归并为groupid中
                String headStr = portArr[0];
                groupId = headStr + "trouble";
            }
        }
        return groupId;
    }


    //将portName 例: cia/b :0 名称拆分成 a b :postfix;  ci a/b/c  => b c :postfix
    public static int[] tarnfrom(String portName) {
        int[] nameArr = new int[2];
        if (portName.contains(":")) {
            portName = portName.split(":")[0];
        }

        String[] split = portName.split("/");

        if (split.length == 2) {
            Matcher matcher = Pattern.compile("[0-9]+/").matcher(portName);
            while (matcher.find()) {
                nameArr[0] = Integer.parseInt(matcher.group(0).replace("/", ""));
            }
            Matcher matcher2 = Pattern.compile("/[0-9]+").matcher(portName);
            while (matcher2.find()) {
                nameArr[1] = Integer.parseInt(matcher2.group(0).replace("/", ""));
            }
        } else {
            nameArr[0] = Integer.parseInt(split[1]);
            nameArr[1] = Integer.parseInt(split[2]);
        }

        return nameArr;
    }

    //根据同组中前一个portNum  判断是否下一个port的number增长方式
    private static int getNum(String portName1, String portName2, int num) {
        int[] nameArr1 = tarnfrom(portName1);
        int[] nameArr2 = tarnfrom(portName2);

        if (nameArr1[0] == nameArr2[0]) {// a/b -a位相同
            if (nameArr2[1] - nameArr1[1] == 1) {//正常跨越 a/b => a/b+1   num++;
                num++;
            } else {
                num += nameArr2[1] - nameArr1[1];  //第二位跨越  a/b => a/c  num+=c-a;
            }
        } else {   //进位
            if (nameArr2[1] == 1) {   //正常进位  a/b => a+1/1  num++;
                num++;
            } else {   //非正常进位  a/b => c/d  num=
                num = nameArr2[0] * nameArr1[1] + nameArr2[1];
            }
        }
        return num;
    }
}
