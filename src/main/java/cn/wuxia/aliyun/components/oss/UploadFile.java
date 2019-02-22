/*
* Created on :Nov 27, 2014
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.aliyun.components.oss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.activation.MimetypesFileTypeMap;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;

import cn.wuxia.common.util.FileUtil;
import cn.wuxia.common.util.StringUtil;

@Deprecated
public class UploadFile {
    String accessKeyId;

    String accessKeySecret;

    private String bucketName;

    // 初始化 OSSClient
    OSSClient client;

    public UploadFile() {

    }

    public UploadFile(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        client = new OSSClient(accessKeyId, accessKeySecret);
    }

    public UploadFile(String accessKeyId, String accessKeySecret, String bucketName) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        client = new OSSClient(accessKeyId, accessKeySecret);
    }


    private String upload(String key,  File content) throws FileNotFoundException {
        // 新建一个 Bucket
        //client.createBucket(bucketName);

        // 获取指定文件的输入流
        //         创建上传 Object 的 Metadata
        ObjectMetadata meta = new ObjectMetadata();
        //         必须设置 ContentLength 
        meta.setContentLength(FileUtil.sizeOf(content));
        meta.setContentType(new MimetypesFileTypeMap().getContentType(content));
        // 上传 Object.
        PutObjectResult result = client.putObject(bucketName, key, content, meta);
        // 打印 ETag
        return result.getETag();
    }

    public void getObject(String key) throws IOException {

        // 获取 Object,返回结果为 OSSObject 对象
        OSSObject object = client.getObject(bucketName, key);
        // 获取 Object 的输入流
        InputStream objectContent = object.getObjectContent();
        // 处理 Object ...

        // 关闭流 
        objectContent.close();
    }

    public void getUrl(String key) {
        // 设置URL过期时间为1小时
        Date expiration = new Date(new Date().getTime() + 3600 * 1000);
        // 生成 URL
        URL url = client.generatePresignedUrl(bucketName, key, expiration);
        System.out.println(url.getPath());
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

}
