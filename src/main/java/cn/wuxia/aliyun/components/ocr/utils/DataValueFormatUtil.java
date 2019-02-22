/*
* Created on :2017年3月2日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ocr.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * 
 * [ticket id]
 * @see {@link https://help.aliyun.com/document_detail/30406.html?spm=5176.doc30408.6.548.qR5J5M}
 * @author songlin
 * @ Version : V<Ver.No> <2017年3月2日>
 */
public class DataValueFormatUtil {

    public static String format(InputStream input) throws IOException {
        return Base64.encodeBase64String(IOUtils.toByteArray(input));
    }

}
