package cn.wuxia.common.test;

import cn.wuxia.aliyun.components.oss.OSSUtils;
import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.EncodeUtils;
import com.aliyun.oss.model.OSSObject;

import java.io.File;
import java.util.Date;
import java.util.Map;

public class OssMainTest {
    public static void main(String[] args) {

        OSSUtils ossUtils = new OSSUtils("oss-cn-shenzhen.aliyuncs.com","LTAIBvTZ87DQZZrR", "whpaRiBfVj0YmhhbhglJNBBQM553bL");
       Map map = ossUtils.putObject("img-www", "def/","efg.png", new File("/Users/songlin/Desktop/tongguheren.png"));

        System.out.println(map);
        String abc = EncodeUtils.urlDecode(ossUtils.client.generatePresignedUrl("img-www", "def/efg.png", DateUtil.addDays(new Date(), 1)).toString());
        System.out.println(abc);
//      map =  ossUtils.getObjectByParams("img-www", map.get("key").toString());
//        System.out.println(map);
//        OSSObject ossObject = (OSSObject) map.get("object");
//        System.out.println(ossObject.getKey());
    }
}
