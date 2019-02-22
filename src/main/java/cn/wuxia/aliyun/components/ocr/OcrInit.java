/*
* Created on :2017年3月2日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ocr;

import java.io.File;
import java.io.IOException;

import cn.wuxia.aliyun.components.ocr.utils.IdCardUtil;

public class OcrInit {
  public static String idCardUrl = "https://shujuapi.aliyun.com/dataplus_50961/ocr/ocr_idcard"
          + "";
  public static final String akId = "o4RC9Db5DxNhJudu";
  public static final String akSecret="Kqt1sUhcFHpLxiwOHphEYlzf7Daftc";
  
  
  public static void main(String [] args){
      try {
        IdCardUtil.validHeadImage(new File("/Users/songlin/Documents/个人资料/idcard/身份证正面上传.jpg"));
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
  }
}
