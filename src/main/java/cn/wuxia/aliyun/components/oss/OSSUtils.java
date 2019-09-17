/*
 * Created on :May 18, 2015
 * Author     :Wind.ZHao
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */

package cn.wuxia.aliyun.components.oss;

import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.EncodeUtils;
import cn.wuxia.common.util.StringUtil;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.*;
import com.google.common.collect.Maps;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OSSUtils {

    protected Logger logger = LoggerFactory.getLogger(getClass());


    public OSSClient client;


    public OSSClient getClient() {
        return client;
    }

    public void setClient(OSSClient client) {
        this.client = client;
    }


    /**
     * 构造函数，初始化ossclien
     *
     * @param endpoint        区域，具体参照阿里云的oss，可为空
     * @param accessKeyId     阿里云oss访问匙id
     * @param accessKeySecret 阿里云oss访问匙密钥
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public OSSUtils(String endpoint, String accessKeyId, String accessKeySecret) {
        this(endpoint, accessKeyId, accessKeySecret, null);
    }

    /**
     * 构造函数，初始化ossclient
     *
     * @param endpoint        区域，具体参照阿里云的oss
     * @param accessKeyId     阿里云oss访问匙id
     * @param accessKeySecret 阿里云oss访问匙密钥
     * @param conf            client的配置信息，具体参照阿里云的oss
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public OSSUtils(String endpoint, String accessKeyId, String accessKeySecret, ClientConfiguration conf) {

        Assert.notNull(endpoint, "endpoint不能为空");
        Assert.notNull(accessKeyId, "accessKeyId不能为空");
        Assert.notNull(accessKeySecret, "accessKeySecret不能为空");

        client = new OSSClient(endpoint, new DefaultCredentialProvider(accessKeyId, accessKeySecret), conf);

    }

    /**
     * 检查client是否存在
     *
     * @return 返回标识，true：存在；false：不存在
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public boolean checkClientIsExist() {
        if (null != client) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 创建bucket
     *
     * @param bucketName bucket名称
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00001的时候存在；
     * 3）bucket：把创建后的bucket对象返回，当code为00000的时候才有。
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public Map<String, Object> createBucket(String bucketName) {
        Map<String, Object> retMap = Maps.newHashMap();

        if (checkClientIsExist()) {
            if (client.doesBucketExist(bucketName)) {
                retMap.put("code", "00000");
                retMap.put("bucket", client.createBucket(bucketName));
            } else {
                retMap.put("code", "00001");
                retMap.put("msg", "创建bucket失败，bucket（" + bucketName + "）已经存在，不能重复创建！");
            }
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "创建bucket失败，oss client 不存在，请先进行实例化！");
        }

        return retMap;
    }

    /**
     * 获取所有bucket
     *
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）buckets：阿里云oss上所有的bucket，当code为00000的时候才有。
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public Map<String, Object> getAllBuckets() {
        Map<String, Object> retMap = Maps.newHashMap();

        if (checkClientIsExist()) {
            retMap.put("code", "00000");
            retMap.put("buckets", client.listBuckets());
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "获取所有bucket失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }


    /**
     * 在bucket上创建对象，如果当前路径有相同名字则会进行替换<br>
     * bucketName和filePath一定不能为空<br>
     * 1）key和fileName同时为空，则在bucket下面创建对象，对象名称为<br>
     * filePath中的文件名，如bucketName为'testBucket'，filePath<br>
     * 为'c:/test/a.jpg'则在testBucket下创建a.jpg的对象；
     * 2）key不为空，fileName为空，如key为a/b/c，bucketName<br>
     * 为'test'，filePath为'c:/test/a.jpg'，则在testBucket<br>
     * 创建'a/b/c/a.jpg'的文件
     * 3）key为空，fileName不为空，如fileName为b,bucketName<br>
     * 为'testBucket'，filePath为'c:/test/a.jpg'，则在<br>
     * testBucket下创建b.jpg的对象
     *
     * @param bucketName 文件夹所在的bucket名字
     * @param key        所在bucket的路径，如果key的第一位为"/"，则去除"/"<br>
     *                   注意该参数为文件路径，并不包含文件名称
     * @param meta       用户对该object的描述，由一系列name-value对组成；<br>
     *                   其中ContentLength是必须设置的，以便SDK可以正确识<br>
     *                   别上传Object的大小。支持的Http Header有四种，分<br>
     *                   别为：Cache-Control 、 Content-Disposition 、<br>
     *                   Content-Encoding 、 Expires
     * @param fileName   文件名，上传到oss的文件名
     * @param filePath   需要上传文件的路径
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）object：创建后的对象（PutObjectResult）；
     * 4）url：对象对应的服务器地址；
     * 5）key：文件的具体路径，格式如：/app/test。
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public Map<String, Object> putObject(String bucketName, String key, ObjectMetadata meta, String fileName, String filePath) {
        Map<String, Object> retMap = Maps.newHashMap();
        // 获取指定文件的输入流
        if (StringUtil.isBlank(filePath)) {
            retMap.put("code", "00001");
            retMap.put("msg", "创建文件对象失败，filePath不能为空！");
            return retMap;
        }
        try {
            File file = new File(filePath);
            retMap = putObject(bucketName, key, meta, fileName, file);
        } catch (Exception ex) {
            retMap.put("code", "00001");
            retMap.put("msg", "创建文件对象失败，创建file异常：" + ex.getMessage());
            return retMap;
        }

        return retMap;
    }

    /**
     * 在bucket上创建对象，如果当前路径有相同名字则会进行替换<br>
     * bucketName和file一定不能为空<br>
     * 1）key和fileName同时为空，则在bucket下面创建对象，对象名称为<br>
     * file的文件名，如bucketName为'testBucket'，filePath<br>
     * 为'c:/test/a.jpg'则在testBucket下创建a.jpg的对象；
     * 2）key不为空，fileName为空，如key为a/b/c，bucketName<br>
     * 为'test'，filePath为'c:/test/a.jpg'，则在testBucket<br>
     * 创建'a/b/c/a.jpg'的文件
     * 3）key为空，fileName不为空，如fileName为b,bucketName<br>
     * 为'testBucket'，filePath为'c:/test/a.jpg'，则在<br>
     * testBucket下创建b.jpg的对象
     *
     * @param bucketName 文件夹所在的bucket名字
     * @param key        所在bucket的路径，最后一个为"/"后面的为上传对象的名字<br>
     *                   如果路径不存在则会自动创建，如果key的第一位为"/"，则去除"/"
     * @param fileName   文件名，上传到oss的文件名，不包括后缀
     * @param file       需要上传文件
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）object：创建后的对象（PutObjectResult）；
     * 4）url：文件路径；
     * 5）key：文件的具体路径，格式如：/app/test。
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public Map<String, Object> putObject(String bucketName, String key, String fileName, File file) {
        return putObject(bucketName, key, null, fileName, file);
    }

    /**
     * 在bucket上创建对象，如果当前路径有相同名字则会进行替换<br>
     * bucketName和file一定不能为空<br>
     * 1）key和fileName同时为空，则在bucket下面创建对象，对象名称为<br>
     * file的文件名，如bucketName为'testBucket'，filePath<br>
     * 为'c:/test/a.jpg'则在testBucket下创建a.jpg的对象；
     * 2）key不为空，fileName为空，如key为a/b/c，bucketName<br>
     * 为'test'，filePath为'c:/test/a.jpg'，则在testBucket<br>
     * 创建'a/b/c/a.jpg'的文件
     * 3）key为空，fileName不为空，如fileName为b,bucketName<br>
     * 为'testBucket'，filePath为'c:/test/a.jpg'，则在<br>
     * testBucket下创建b.jpg的对象
     *
     * @param bucketName  文件夹所在的bucket名字
     * @param key         所在bucket的路径，最后一个为"/"后面的为上传对象的名字<br>
     *                    如果路径不存在则会自动创建，如果key的第一位为"/"，则去除"/"
     * @param fileName    文件名，上传到oss的文件名，不包括后缀
     * @param inputStream 需要上传文件
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）object：创建后的对象（PutObjectResult）；
     * 4）url：文件路径；
     * 5）key：文件的具体路径，格式如：/app/test。
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public Map<String, Object> putObject(String bucketName, String key, String fileName, InputStream inputStream) {
        return putObject(bucketName, key, null, fileName, inputStream);
    }

    /**
     * 在bucket上创建对象，如果当前路径有相同名字则会进行替换<br>
     * bucketName和file一定不能为空<br>
     * 1）key和fileName同时为空，则在bucket下面创建对象，对象名称为<br>
     * file的文件名，如bucketName为'testBucket'，filePath<br>
     * 为'c:/test/a.jpg'则在testBucket下创建a.jpg的对象；
     * 2）key不为空，fileName为空，如key为a/b/c，bucketName<br>
     * 为'test'，filePath为'c:/test/a.jpg'，则在testBucket<br>
     * 创建'a/b/c/a.jpg'的文件
     * 3）key为空，fileName不为空，如fileName为b,bucketName<br>
     * 为'testBucket'，filePath为'c:/test/a.jpg'，则在<br>
     * testBucket下创建b.jpg的对象
     *
     * @param bucketName 文件夹所在的bucket名字
     * @param key        所在bucket的路径，如果key的第一位为"/"，则去除"/"<br>
     *                   注意该参数为文件路径，并不包含文件名称
     * @param meta       用户对该object的描述，由一系列name-value对组成；<br>
     *                   其中ContentLength是必须设置的，以便SDK可以正确识<br>
     *                   别上传Object的大小。支持的Http Header有四种，分<br>
     *                   别为：Cache-Control 、 Content-Disposition 、<br>
     *                   Content-Encoding 、 Expires
     * @param fileName   文件名，上传到oss的文件名
     * @param file       需要上传文件
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）object：创建后的对象（PutObjectResult）；
     * 4）url：对象对应的服务器地址；
     * 5）key：文件的具体路径，格式如：/app/test。
     * @author Wind.Zhao
     * @date 2015/05/18
     */
    public Map<String, Object> putObject(String bucketName, String key, ObjectMetadata meta, String fileName, File file) {
        Map<String, Object> retMap = Maps.newHashMap();
        if (StringUtil.isBlank(bucketName)) {
            retMap.put("code", "00001");
            retMap.put("msg", "创建文件对象失败，bucketName不能为空！");
            return retMap;
        }
        if (StringUtil.isBlank(file)) {
            retMap.put("code", "00001");
            retMap.put("msg", "创建文件对象失败，file不能为空！");
            return retMap;
        }
        URI uri = client.getEndpoint();

        String fileType = FilenameUtils.getExtension(file.getName());
        //上传到oss的文件名
        if (StringUtil.isBlank(fileName)) {
            fileName = file.getName();
        }
        //当key不为空的情况下需要判断key的最后一位是否为"/"，如果不是则追加"/"
        if (StringUtil.isNotBlank(key)) {
            if ("/".equals(key.subSequence(0, 1))) {
                key = key.substring(1, key.length());
            }
            if ((key.length() - 1) != key.lastIndexOf("/")) {
                key += "/";
            }
        }
        //过滤以"/"开头的路径
        if ("/".equals(key.subSequence(0, 1))) {
            key = key.substring(1);
        }
        if (checkClientIsExist()) {
            // 获取指定文件的输入流
            if (null == meta) {
                // 创建上传Object的Metadata
                meta = new ObjectMetadata();
            }
            //必须设置ContentLength
            meta.setContentLength(file.length());
            // 上传Object.
            PutObjectResult result = client.putObject(bucketName, key + fileName, file, meta);
            // 获取存储空间的访问权限。
            String url = uri.getScheme() + "://" + bucketName + "." + uri.getAuthority();
            retMap.put("code", "00000");
            retMap.put("object", result);
            try {
                retMap.put("url", URLDecoder.decode(url, "utf-8"));
                retMap.put("key", URLDecoder.decode(key + fileName, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                logger.error("创建文件对象成功，返回路径异常：" + e.getMessage());
                retMap.put("code", "00001");
                retMap.put("msg", "创建文件对象成功，返回路径异常：" + e.getMessage());
            }
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "创建文件对象失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }


    public Map<String, Object> putObject(String bucketName, String key, ObjectMetadata meta, String fileName, InputStream inputStream) {
        Map<String, Object> retMap = Maps.newHashMap();
        if (StringUtil.isBlank(bucketName)) {
            retMap.put("code", "00001");
            retMap.put("msg", "创建文件对象失败，bucketName不能为空！");
            return retMap;
        }
        URI uri = client.getEndpoint();


        //当key不为空的情况下需要判断key的最后一位是否为"/"，如果不是则追加"/"
        if (StringUtil.isNotBlank(key)) {
            if ("/".equals(key.subSequence(0, 1))) {
                key = key.substring(1, key.length());
            }
            if ((key.length() - 1) != key.lastIndexOf("/")) {
                key += "/";
            }
        }
        //过滤以"/"开头的路径
        if ("/".equals(key.subSequence(0, 1))) {
            key = key.substring(1);
        }
        if (checkClientIsExist()) {
            // 获取指定文件的输入流
            // 上传Object.
            PutObjectResult result = client.putObject(bucketName, key + fileName, inputStream);
            String url = uri.getScheme() + "://" + bucketName + "." + uri.getAuthority();
            retMap.put("code", "00000");
            retMap.put("object", result);
            try {
                retMap.put("url", URLDecoder.decode(url, "utf-8"));
                retMap.put("key", URLDecoder.decode(key + fileName, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("创建文件对象成功，返回路径异常：" + e.getMessage());
                retMap.put("code", "00001");
                retMap.put("msg", "创建文件对象成功，返回路径异常：" + e.getMessage());
            }
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "创建文件对象失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }

    /**
     * 获取当前bucket下的所有object，可遍历返回的对象列表，获取每个对象信息<br>
     * eg：for (OSSObjectSummary objectSummary : listing.getObjectSummaries())
     *
     * @param bucketName bucket名称
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）objectList：当code为00000的时候存在，ObjectListing类型<br>
     * 包含N个OSSObjectSummary。
     * @author Wind.Zhao
     * @date 2015/05/20
     */
    public Map<String, Object> getAllObject(String bucketName) {
        Map<String, Object> retMap = Maps.newHashMap();

        if (checkClientIsExist()) {
            // 获取指定bucket下的所有Object信息
            ObjectListing listing = client.listObjects(bucketName);

            retMap.put("code", "00000");
            retMap.put("objectList", listing);
            // 遍历所有Object
            /*for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
                System.out.println(objectSummary.getKey());
            }*/
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "获取bucket下所有对象失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }

    /**
     * 根据参数获取指定bucket下的对象，如果需要遍历所有的object，而object数量大于1000，<br>
     * 则需要进行多次迭代。每次迭代时，将上次迭代列取最后一个object的key作为本次迭代中的marker即可。<br>
     * 注意：delimiter此参数可能与oss解释的不一样，1）当该参数为"/"的时候，结果为当前bucket的所<br>
     * 有文件，并不包括文件夹，2）当该参数为非"/"，如"a"则结果为不包含a的文件和文件夹对象，假如a文件夹<br>
     * 下有个b文件夹或者文件，则b不显示；3）当为空或null的情况下则获取当前bucket所有的对象。
     *
     * @param bucketName bucket名称
     * @param delimiter  长度只能为1，中文长度为2，故不行。用于对object名字进行分组的字符。所有名字包含指定的前缀且第一次出现<br>
     *                   delimiter字符之间的object作为一组元素: commonPrefixes。<br>
     * @param marker     设定结果从marker之后按字母排序的第一个开始返回。如对象大于1000，则需<br>
     *                   要迭代获取时，则把当前获取最后一个对象的key最为下次迭代开始的标识。
     * @param maxKeys    限定此次返回object的最大数，如果不设定，默认为100，<br>
     *                   maxKeys取值不能大于1000。
     * @param prefix     限定返回的object key必须以prefix作为前缀。注意使用<br>
     *                   prefix查询时，返回的key中仍会包含prefix。
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）objectList：当code为00000的时候存在，ObjectListing类型<br>
     * 包含N个OSSObjectSummary。
     * @author Wind.Zhao
     * @date 2015/05/20
     */
    public Map<String, Object> getObjectByParams(String bucketName, String delimiter, String marker, Integer maxKeys, String prefix) {
        Map<String, Object> retMap = Maps.newHashMap();
        if (null == maxKeys || maxKeys < 0) {
            maxKeys = 100;
        }

        if (checkClientIsExist()) {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);
            //设置参数
            listObjectsRequest.setDelimiter(delimiter);
            listObjectsRequest.setMarker(marker);
            if (null != maxKeys) {
                listObjectsRequest.setMaxKeys(maxKeys);
            }
            //过滤以"/"开头的路径
            if ("/".equals(prefix.subSequence(0, 1))) {
                prefix = prefix.substring(1);
            }
            listObjectsRequest.setPrefix(prefix);

            ObjectListing listing = client.listObjects(listObjectsRequest);
            String nextMarker = listing.getNextMarker();
            if (null != listing && !StringUtil.equals(null, nextMarker)) {
                Map<String, Object> tempMap = getObjectByParams(bucketName, delimiter, nextMarker, maxKeys, prefix);
                if (null != tempMap && null != tempMap.get("objectList")) {
                    ObjectListing tempList = (ObjectListing) tempMap.get("objectList");
                    for (OSSObjectSummary objectSummary : tempList.getObjectSummaries()) {
                        listing.addObjectSummary(objectSummary);
                    }
                }
            }
            retMap.put("code", "00000");
            retMap.put("objectList", listing);

            //遍历所有Object
            /*for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
                System.out.println(objectSummary.getKey());
            }*/
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "获取bucket对象失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }

    /**
     * 根据bucke名称和对象路径获取对象信息
     *
     * @param bucketName bucket名称
     * @param key        对象路径，包括对象名，对于key的首位为"/"则会默认过滤<br>
     *                   eg：/resource/a.jpg最终会转化为resource/a.jpg
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）object：当code为00000的时候存在，OSSObject类型。
     * @author Wind.Zhao
     * @date 2015/05/20
     */
    public Map<String, Object> getObjectByParams(String bucketName, String key) {
        Map<String, Object> retMap = Maps.newHashMap();
        if (StringUtil.isBlank(key)) {
            retMap.put("code", "00001");
            retMap.put("msg", "获取对象失败，对象路径为空！");
            return retMap;
        }
        if (checkClientIsExist()) {

            try {
                //过滤以"/"开头的路径
                if ("/".equals(key.subSequence(0, 1))) {
                    key = key.substring(1);
                }
                // 获取Object，返回结果为OSSObject对象
                OSSObject object = client.getObject(bucketName, key);
                retMap.put("code", "00000");
                retMap.put("object", object);
            } catch (Exception ex) {
                retMap.put("code", "00001");
                retMap.put("msg", "获取bucket对象失败，读取对象异常！");
                logger.error("获取bucket对象失败，读取对象异常：" + ex.getMessage());
                return retMap;
            }
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "获取bucket对象失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }

    /**
     * 删除对象，不能自动递归删除，只能有最底层往上逐删除对于文件夹对象是创建文件对象<br>
     * 的时候产生的，则删除所有文件对象后，该文件夹也会删除（在oss上手动创建的除外），<br>
     * 如bucke下面有一个叫resource的文件夹（在oss上手动创建的除外），创建文件对象<br>
     * a.jpg的时候创建了image的文件夹那么在删除a.jpg（image只有a.jpg一个文件），<br>
     * image文件夹对象也会删除。
     *
     * @param bucketName bucket名称
     * @param key        被删除的对象路径
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在。
     * @author Wind.Zhao
     * @date 2015/05/20
     */
    public Map<String, Object> deleteObject(String bucketName, String key) {
        Map<String, Object> retMap = Maps.newHashMap();
        if (StringUtil.isBlank(key)) {
            retMap.put("code", "00001");
            retMap.put("msg", "删除对象失败，对象路径为空！");
            return retMap;
        }
        if (checkClientIsExist()) {
            //过滤以"/"开头的路径
            if ("/".equals(key.subSequence(0, 1))) {
                key = key.substring(1);
            }
            // 删除Object
            client.deleteObject(bucketName, key);
            retMap.put("code", "00000");
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "删除bucket对象失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }

    /**
     * 删除对象，实现自动删除当前路径的所有对象<br>
     *
     * @param bucketName bucket名称
     * @param key        被删除的对象路径
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在。
     * @author Wind.Zhao
     * @date 2015/05/20
     */
    public Map<String, Object> deleteObjects(String bucketName, String key) {
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("code", "00001");
        retMap.put("msg", "递归删除对象失败，危险操作，方法已经关闭！");
        return retMap;
        /*
        if(StringUtil.isBlank(key)){
        	retMap.put("code", "00001");
        	retMap.put("msg", "删除对象失败，对象路径为空！");
        	return retMap;
        }
        if(checkClientIsExist()){
        	//过滤以"/"开头的路径
        	if("/".equals(key.subSequence(0, 1))){
        		key = key.substring(1);
        	}
        	//由于oss只能进行单对象复制，所以在删除之前要把当前路径下的所有文件和文件夹查找出来，再顺序循环一个个删除
        	Map<String, Object> tempMap = getObjectByParams(bucketName, null, null, null, key);
        	if(null != tempMap){
        		ObjectListing ol = (ObjectListing) tempMap.get("objectList");
        		if(null != ol){
        			List<OSSObjectSummary> obsList = ol.getObjectSummaries();
        			if(null != obsList){
        				int len = obsList.size();
        				for(int index=(len-1); index>=0; index--){
        					OSSObjectSummary obs = obsList.get(index);
        					client.deleteObject(bucketName, obs.getKey());
        				}
        			}
        		}
        	}
        	retMap.put("code", "00000");
        }else{
        	retMap.put("code", "00001");
        	retMap.put("msg", "删除bucket对象失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;*/
    }

    /**
     * 复制对象，如果复制对象下有自对象也会复制到目的地址。使用该方法copy的object大于800M<br>
     * 则内部调用uploadPartCopy。
     *
     * @param srcBucketName    复制源bucket的名称
     * @param srcKey           复制源路径
     * @param targetBucketName 目的源bucket的名称
     * @param targetKey        目的源路径
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在。
     * @author Wind.Zhao
     * @date 2015/05/20
     */
    public Map<String, Object> copyObjects(String srcBucketName, String srcKey, String targetBucketName, String targetKey) {
        Map<String, Object> retMap = Maps.newHashMap();
        //判断复制源srcBucketName是否为空
        if (StringUtil.isBlank(srcBucketName)) {
            retMap.put("code", "00001");
            retMap.put("msg", "复制对象失败，复制源buckeName不能为空！");
            return retMap;
        }
        //判断复制源srcKey是否为空，不为空则判断第一位是否为"/"若是则去除"/"
        if (StringUtil.isBlank(srcKey)) {
            retMap.put("code", "00001");
            retMap.put("msg", "复制对象失败，复制源srcKey不能为空！");
            return retMap;
        } else {
            if ("/".equals(srcKey.subSequence(0, 1))) {
                srcKey = srcKey.substring(1, srcKey.length());
            }
        }
        //判断目的源targetBucketName是否为空，若是则把复制源srcBucketName赋给目的源destBucketName
        if (StringUtil.isBlank(targetBucketName)) {
            targetBucketName = srcBucketName;
        }
        //判断目的源targetKey是否为空，若是则把复制源srcKey赋给目的源destKey
        if (StringUtil.isBlank(targetKey)) {
            targetKey = srcKey;
        }
        //判断目的源targetKey第一位是否为"/"若是则去除"/"
        if ("/".equals(targetKey.subSequence(0, 1))) {
            targetKey = targetKey.substring(1, targetKey.length());
        }
        String curDate = DateUtil.dateToString(new Date(), DateUtil.DateFormatter.FORMAT_YYYYMMDDHHMMSSSSS);
        //判断复制的地址
        if (StringUtil.equals(srcBucketName, targetBucketName) && StringUtil.equals(srcKey, targetKey)) {
            if ((targetKey.length() - 1) == targetKey.lastIndexOf("/")) {
                targetKey = targetKey.substring(0, targetKey.lastIndexOf("/"));
                targetKey = targetKey.substring(0, targetKey.lastIndexOf("/") + 1) + "Copy(" + curDate + ")"
                        + targetKey.substring(targetKey.lastIndexOf("/") + 1, targetKey.length()) + "/";
            } else {
                targetKey = targetKey.substring(0, targetKey.lastIndexOf("/") + 1) + "Copy(" + curDate + ")"
                        + targetKey.substring(targetKey.lastIndexOf("/") + 1, targetKey.length());
            }
        }

        //判断oss client是否存在
        if (checkClientIsExist()) {
            //判断复制源srcBucketName是否存在
            if (!client.doesBucketExist(srcBucketName)) {
                retMap.put("code", "00001");
                retMap.put("msg", "复制对象失败，复制源srcBucketName（" + srcBucketName + "）不存在！");
                return retMap;
            }
            //判断复制源srcKey是否存在
            try {
                if (null == client.getObject(srcBucketName, srcKey)) {
                    retMap.put("code", "00001");
                    retMap.put("msg", "复制对象失败，复制源srcKey（" + srcKey + "）不存在！");
                    return retMap;
                }
            } catch (Exception ex) {
                retMap.put("code", "00001");
                retMap.put("msg", "复制对象失败，获取复制源srcKey（" + srcKey + "）异常！");
                logger.error("复制对象失败，获取复制源srcKey（" + srcKey + "）异常：" + ex.getMessage());
                return retMap;
            }
            //判断目的源targetBucketName是否存在，不存在则创建一个
            if (!client.doesBucketExist(targetBucketName)) {
                try {
                    if (null == client.createBucket(targetBucketName)) {
                        retMap.put("code", "00001");
                        retMap.put("msg", "复制对象失败，目的源destBucketName（" + targetBucketName + "）不存在，创建目的源失败！");
                        return retMap;
                    }
                } catch (Exception ex) {
                    retMap.put("code", "00001");
                    retMap.put("msg", "复制对象失败，目的源destBucketName（" + targetBucketName + "）不存在，创建目的源异常！");
                    logger.error("复制对象失败，目的源destBucketName不存在，创建目的源异常:" + ex.getMessage());
                    return retMap;
                }
            }
            //由于oss只能进行单对象复制，所以在复制之前要把当前路径下的所有文件和文件夹查找出来，再循环一个个复制
            Map<String, Object> tempMap = getObjectByParams(srcBucketName, null, null, null, srcKey);
            if (null != tempMap) {
                ObjectListing ol = (ObjectListing) tempMap.get("objectList");
                if (null != ol) {
                    List<OSSObjectSummary> obsList = ol.getObjectSummaries();
                    if (null != obsList) {
                        for (OSSObjectSummary obs : obsList) {
                            String tempSrcKey = obs.getKey();
                            String tempTargetKey = tempSrcKey.replace(srcKey, targetKey);

                            //if(obs.getSize()>1024*1024*200){
                            uploadPartCopy(srcBucketName, tempSrcKey, targetBucketName, tempTargetKey);
                            //}else{
                            // 拷贝Object
                            //CopyObjectResult result = client.copyObject(srcBucketName, tempSrcKey, targetBucketName, tempTargetKey);
                            //}
                        }
                    }
                }
            }
            retMap.put("code", "00000");
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "获取bucket对象失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }

    /**
     * 复制对象，如果复制对象下有自对象也会复制到目的地址。使用该方法copy的object一般<br>
     * 大于1G时候使用。
     *
     * @param srcBucketName    复制源bucket的名称
     * @param srcKey           复制源路径
     * @param targetBucketName 目的源bucket的名称
     * @param targetKey        目的源路径
     * @return completeMultipartUploadResult
     * @author Wind.Zhao
     * @date 2015/05/22
     */
    private CompleteMultipartUploadResult uploadPartCopy(String srcBucketName, String srcKey, String targetBucketName, String targetKey) {

        String curDate = DateUtil.dateToString(new Date(), DateUtil.DateFormatter.FORMAT_YYYYMMDDHHMMSSSSS);
        //判断复制的地址
        if (StringUtil.equals(srcBucketName, targetBucketName) && StringUtil.equals(srcKey, targetKey)) {
            if ((targetKey.length() - 1) == targetKey.lastIndexOf("/")) {
                targetKey = targetKey.substring(0, targetKey.lastIndexOf("/"));
                targetKey = targetKey.substring(0, targetKey.lastIndexOf("/") + 1) + "Copy(" + curDate + ")"
                        + targetKey.substring(targetKey.lastIndexOf("/") + 1, targetKey.length()) + "/";
            } else {
                targetKey = targetKey.substring(0, targetKey.lastIndexOf("/") + 1) + "Copy(" + curDate + ")"
                        + targetKey.substring(targetKey.lastIndexOf("/") + 1, targetKey.length());
            }
        }

        //过滤以"/"开头的路径
        if ("/".equals(srcKey.subSequence(0, 1))) {
            srcKey = srcKey.substring(1);
        }
        if ("/".equals(targetKey.subSequence(0, 1))) {
            targetKey = targetKey.substring(1);
        }

        ObjectMetadata objectMetadata = client.getObjectMetadata(srcBucketName, srcKey);
        // 开始Multipart Upload
        InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(targetBucketName, targetKey);
        InitiateMultipartUploadResult initiateMultipartUploadResult = client.initiateMultipartUpload(initiateMultipartUploadRequest);
        String uploadId = initiateMultipartUploadResult.getUploadId();

        long partSize = 1024 * 1024 * 100;
        // 得到被拷贝object大小
        long contentLength = objectMetadata.getContentLength();

        // 计算分块数目
        int partCount = (int) (contentLength / partSize);
        if (contentLength % partSize != 0) {
            partCount++;
        }
        List<PartETag> partETags = new ArrayList<PartETag>();

        for (int i = 0; i < partCount; i++) {
            long skipBytes = partSize * i;
            // 计算每个分块的大小
            long size = partSize < contentLength - skipBytes ? partSize : contentLength - skipBytes;
            // 创建UploadPartCopyRequest，上传分块
            UploadPartCopyRequest uploadPartCopyRequest = new UploadPartCopyRequest();
            uploadPartCopyRequest.setSourceKey(srcKey);
            uploadPartCopyRequest.setSourceBucketName(srcBucketName);
            uploadPartCopyRequest.setBucketName(targetBucketName);
            uploadPartCopyRequest.setKey(targetKey);
            uploadPartCopyRequest.setUploadId(uploadId);
            uploadPartCopyRequest.setPartSize(size);
            uploadPartCopyRequest.setBeginIndex(skipBytes);
            uploadPartCopyRequest.setPartNumber(i + 1);
            UploadPartCopyResult uploadPartCopyResult = client.uploadPartCopy(uploadPartCopyRequest);
            // 将返回的PartETag保存到List中。
            partETags.add(uploadPartCopyResult.getPartETag());
        }
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(targetBucketName, targetKey, uploadId,
                partETags);
        //完成分块上传
        CompleteMultipartUploadResult completeMultipartUploadResult = client.completeMultipartUpload(completeMultipartUploadRequest);

        return completeMultipartUploadResult;
    }

    /**
     * 分块上传文件，针对大文件。每块大小不能小于100k<br>
     *
     * @param bucketName bucket的名称
     * @param key        所在bucket的路径，如果key的第一位为"/"，则去除"/"<br>
     *                   注意该参数为文件路径，并不包含文件名称
     * @param meta       用户对该object的描述，由一系列name-value对组成；<br>
     *                   其中ContentLength是必须设置的，以便SDK可以正确识<br>
     *                   别上传Object的大小。支持的Http Header有四种，分<br>
     *                   别为：Cache-Control 、 Content-Disposition 、<br>
     *                   Content-Encoding 、 Expires
     * @param partSize   每块大小，单位为：M
     * @param fileName   文件名称
     * @param filePath   上传文件的路径
     * @return Map对象
     * 1）code：00000为成功代码，其他为失败代码；
     * 2）msg：失败信息，code不为00000的时候存在；
     * 3）completeMultipartUploadResult：分块上传结果；
     * 4）url：对象对应的服务器地址；
     * 5）key：文件的具体路径
     * @author Wind.Zhao
     * @date 2015/05/20
     */
    public Map<String, Object> uploadPart(String bucketName, String key, ObjectMetadata meta, Integer partSize, String fileName, String filePath) {
        Map<String, Object> retMap = Maps.newHashMap();

        if (StringUtil.isBlank(bucketName)) {
            retMap.put("code", "00001");
            retMap.put("msg", "创建对象失败，bucketName不能为空！");
            return retMap;
        }
        if (StringUtil.isBlank(filePath)) {
            retMap.put("code", "00001");
            retMap.put("msg", "创建对象失败，filePath不能为空！");
            return retMap;
        }
        URI uri = client.getEndpoint();
        //上传到oss的文件名
        if (StringUtil.isBlank(fileName)) {
            fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        }
        //当key不为空的情况下需要判断key的最后一位是否为"/"，如果不是则追加"/"
        if (StringUtil.isNotBlank(key)) {
            if ("/".equals(key.subSequence(0, 1))) {
                key = key.substring(1, key.length());
            }
            if ((key.length() - 1) != key.lastIndexOf("/")) {
                key += "/";
            }
        }

        //过滤以"/"开头的路径
        if ("/".equals(key.subSequence(0, 1))) {
            key = key.substring(1);
        }

        if (checkClientIsExist()) {
            //设置每块大小为空的，则设置默认值为200M
            if (null == partSize || partSize <= 0) {
                partSize = 200;
            }
            partSize = 1024 * 1024 * partSize;
            File partFile = new File(filePath);
            //计算分块数目
            int partCount = (int) (partFile.length() / partSize);
            if (partFile.length() % partSize != 0) {
                partCount++;
            }

            //新建一个List保存每个分块上传后的ETag和PartNumber
            List<PartETag> partETags = new ArrayList<PartETag>();
            //路径+文件名
            String keyFile = key + fileName;
            try {
                //获取文件流
                FileInputStream fis = new FileInputStream(partFile);
                //开始Multipart Upload
                InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucketName, keyFile, meta);
                InitiateMultipartUploadResult initiateMultipartUploadResult = client.initiateMultipartUpload(initiateMultipartUploadRequest);
                String uploadId = initiateMultipartUploadResult.getUploadId();
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(keyFile);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(fis);
                for (int i = 0; i < partCount; i++) {
                    //跳到每个分块的开头
                    long skipBytes = partSize * i;
                    fis.skip(skipBytes);
                    //计算每个分块的大小
                    long size = partSize < (partFile.length() - skipBytes) ? partSize : (partFile.length() - skipBytes);
                    //创建UploadPartRequest，上传分块
                    uploadPartRequest.setPartSize(size);
                    uploadPartRequest.setPartNumber(i + 1);
                    uploadPartRequest.setUseChunkEncoding(true); // 使用chunked编码
                    UploadPartResult uploadPartResult = client.uploadPart(uploadPartRequest);
                    //将返回的PartETag保存到List中。
                    partETags.add(uploadPartResult.getPartETag());
                    //关闭文件
                    fis.close();
                }

                CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(bucketName, keyFile, uploadId,
                        partETags);
                //完成分块上传
                CompleteMultipartUploadResult completeMultipartUploadResult = client.completeMultipartUpload(completeMultipartUploadRequest);

                retMap.put("completeMultipartUploadResult", completeMultipartUploadResult);

                String url = uri.getScheme() + "://" + bucketName + "." + uri.getAuthority() + "/";
                try {
                    retMap.put("url", URLDecoder.decode(url, "utf-8"));
                    retMap.put("key", URLDecoder.decode(keyFile, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.error("上传文件对象成功，返回路径异常：" + e.getMessage());
                }
            } catch (Exception e) {
                retMap.put("code", "00001");
                retMap.put("msg", "上传文件异常：" + e.getMessage());
                logger.error("上传文件异常：" + e.getMessage());
            }
            retMap.put("code", "00000");
            retMap.put("partETags", partETags);
        } else {
            retMap.put("code", "00001");
            retMap.put("msg", "上传文件失败，oss client 不存在，请先进行实例化！");
        }
        return retMap;
    }

    /**
     * 1天的有效期
     *
     * @param bucketName
     * @param key
     * @return
     */
    public String getAccessUrl(String bucketName, String key) {
        AccessControlList acl = client.getBucketAcl(bucketName);
        if ("/".equals(key.subSequence(0, 1))) {
            key = key.substring(1, key.length());
        }
        if (acl.getCannedACL() == CannedAccessControlList.Private) {
            return EncodeUtils.urlDecode(client.generatePresignedUrl(bucketName, key, DateUtil.addDays(new Date(), 1)).toString());
        } else {
            URI uri = client.getEndpoint();
            String url = uri.getScheme() + "://" + bucketName + "." + uri.getAuthority();
            return url + "/" + key;
        }
    }

}
