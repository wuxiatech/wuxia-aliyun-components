/*
* Created on :2017年3月2日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ocr.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.wuxia.aliyun.components.ocr.OcrInit;
import cn.wuxia.aliyun.components.ocr.bean.ConfigureBean;
import cn.wuxia.aliyun.components.ocr.bean.ImageBean;
import cn.wuxia.aliyun.components.ocr.bean.InputBean;

/**
 * <pre>
 * {
    "inputs": [
    {
        "image": {
            "dataType": 50,                         #50表示image的数据类型为字符串
            "dataValue": "base64_image_string"      #图片以base64编码的string
        },
        "configure": {
            "dataType": 50,
            "dataValue": "{
                \"side\": \"face\"                      #身份证正反面类型: face/back
            }"
        }
    }]
}
 * </pre>
 * @author songlin
 * @ Version : V<Ver.No> <2017年3月2日>
 */
public class IdCardUtil {
    final static String idCardUrl = OcrInit.idCardUrl;

    /**
     * 验证身份证的正反面
     * @author songlin
     * @param facePic
     * @param backPic
     * @throws IOException 
     */
    public static void validIdCard(File facePic, File backPic) throws IOException {
        validIdCard(FileUtils.openInputStream(facePic), FileUtils.openInputStream(backPic));
    }

    /**
     * 验证身份证的正反面
     * @author songlin
     * @param facePic
     * @param backPic
     */
    public static void validIdCard(InputStream facePic, InputStream backPic) {
        List<InputBean> inputs = Lists.newArrayList();
        ImageBean faceImage = new ImageBean(facePic);
        Map<String, String> faceMap = Maps.newHashMap();
        faceMap.put("side", "face");
        ConfigureBean faceConfigure = new ConfigureBean(faceMap);

        ImageBean backImage = new ImageBean(backPic);
        Map<String, String> backMap = Maps.newHashMap();
        backMap.put("side", "back");
        ConfigureBean backConfigure = new ConfigureBean(backMap);

        inputs.add(new InputBean(faceImage, faceConfigure));
        inputs.add(new InputBean(backImage, backConfigure));
        RequestUtil.post(idCardUrl, inputs);
    }

    /**
     * 验证正面身份证
     * @author songlin
     * @param facePic
     * @throws IOException 
     */
    public static void validHeadImage(File facePic) throws IOException {
        validHeadImage(FileUtils.openInputStream(facePic));
    }

    /**
     * 验证正面身份证
     * @author songlin
     * @param facePic
     */
    public static void validHeadImage(InputStream facePic) {
        post(facePic, "face");
    }

    /**
     * 验证反面身份证
     * @author songlin
     * @param backPic
     * @throws IOException 
     */
    public static void validBackImage(File backPic) throws IOException {
        validBackImage(FileUtils.openInputStream(backPic));
    }

    /**
     * 验证反面身份证
     * @author songlin
     * @param backPic
     */
    public static void validBackImage(InputStream backPic) {
        post(backPic, "back");
    }

    private static void post(InputStream in, String side) {
        ImageBean image = new ImageBean(in);
        Map<String, String> map = Maps.newHashMap();
        map.put("side", side);
        ConfigureBean configure = new ConfigureBean(map);
        RequestUtil.post(idCardUrl, Lists.newArrayList(new InputBean(image, configure)));
    }
}
