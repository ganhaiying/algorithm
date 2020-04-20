package com.ghy.service;

import com.alibaba.fastjson.JSONObject;
import com.ghy.entity.CommunicationArea;
import com.ghy.entity.InnerMap;
import com.google.gson.Gson;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Title: 
 * Description: 
 * author: ganhy
 * date: 2020/4/20 23:08
 */
public class ConvertToJson {
    /**
     * 扫描list<bean>封装topMap第一层键值对
     * @param originCommunicationAreaList
     * @return
     */
    public Map<String, Object> listConvertToMap(List<CommunicationArea> originCommunicationAreaList){
        Map<String, Object> topMap = new HashMap<>();
        List<String> innerMapKeyList = new ArrayList<>();
        String innerMapKey = null;
        int start = 0;
        int end = 0;
        //遍历list<bean>封装topMap第一层键值对
        for(int i = 0; i < originCommunicationAreaList.size(); i++){
            CommunicationArea communicationArea = originCommunicationAreaList.get(i);
            //遇到[开头并且以[start]结尾的fieldName时：记录开始节点的下标，取开始节点的fieldName暂存到innerMapKey
            //遇到innerMapKey对应的结束节点，取“开始节点下标+1”到“结束节点下标”的记录撞到innerMapList里
            String fieldName = communicationArea.getFieldName();
            String fieldValue = communicationArea.getFieldValue();
            if(start == 0 && fieldName.startsWith("[") && fieldName.endsWith("[start]")){
                innerMapKey = fieldName.replace("[start]", "").replace("[", "").replace("]", "");
                start = i + 1;
                continue;
            }
            if(!StringUtils.isEmpty(innerMapKey) && fieldName.contains(innerMapKey) && fieldName.endsWith("[end]")){
                end = i;
                if(start > end){
                    continue;
                }
                List<CommunicationArea> innerCommunicationAreaList = originCommunicationAreaList.subList(start, end);
                InnerMap innerMap = new InnerMap();
                innerMap.setCommunicationAreaList(innerCommunicationAreaList);
                topMap.put(innerMapKey, JSONObject.toJSONString(innerMap));
                innerMapKeyList.add(innerMapKey);
                innerMapKey = null;
                start = 0;
                end = 0;
                continue;
            }
            if(start == 0 && end == 0 && innerMapKey == null){
                topMap.put(fieldName, fieldValue);
            }
        }
        if(innerMapKeyList != null && innerMapKeyList.size() > 0){
            handleInnerMap(innerMapKeyList, topMap);
        }
        return topMap;
    }

    /**
     * 扫描innerMap
     * @param innerMapKeyList
     * @param topMap
     */
    private void handleInnerMap(List<String> innerMapKeyList, Map<String, Object> topMap){
        for(int i = 0; i < innerMapKeyList.size(); i++){
            String innerMapKey = innerMapKeyList.get(i);
            Gson gson = new Gson();
            InnerMap innerMap = gson.fromJson(topMap.get(innerMapKey).toString(), InnerMap.class);
            List<CommunicationArea> communicationAreaList = innerMap.getCommunicationAreaList();
            if(communicationAreaList != null && communicationAreaList.size() > 0){
                //递归调用listConvertTopMap方法
                Map<String, Object> subMap = listConvertToMap(communicationAreaList);
                topMap.put(innerMapKey, JSONObject.toJSONString(subMap));
            }
        }
    }
}
