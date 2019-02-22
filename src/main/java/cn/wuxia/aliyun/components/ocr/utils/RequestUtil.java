/*
* Created on :2017年3月2日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ocr.utils;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import cn.wuxia.aliyun.components.ocr.OcrInit;
import cn.wuxia.aliyun.components.ocr.bean.InputBean;
import cn.wuxia.common.util.JsonUtil;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.web.httpclient.HttpClientRequest;
import cn.wuxia.common.web.httpclient.HttpClientUtil;

/**
 * 
 * 
 * <pre>
 * "inputs": [
    {
        "image": {
            "dataType": 50,
            "dataValue": "base64_image_string"      #图片以base64编码的string
        },
        "configure": {
            "dataType": 50,
            "dataValue": "{
                \"arg1\" : \"arg1_value\",
                \"arg2\" : \"arg2_value\"
            }"
        }   #[可选参数]
    }
]
 * 
 * </pre>
 * @author songlin
 * @ Version : V<Ver.No> <2017年3月2日>
 */
public class RequestUtil {

    public static final String akId = OcrInit.akId;

    public static final String akSecret = OcrInit.akSecret;

    public static void post(String url, List<InputBean> inputs) {
        if (ListUtil.isEmpty(inputs))
            return;
        Map<String, Object> input = Maps.newHashMap();
        input.put("input", inputs);
        String jsonString = JsonUtil.toJson(input);
        //HttpClientRequest param = new HttpClientRequest(url);
        //HttpClientUtil.postJSON(param, jsonString);
        try {
            AliyunDemo.sendPost(url, jsonString, akId, akSecret);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
