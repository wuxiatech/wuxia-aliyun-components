/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.wuxia.aliyun.components.gateway.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.client.util.HttpAsyncClientUtils;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import cn.wuxia.aliyun.components.gateway.Response;
import cn.wuxia.aliyun.components.gateway.constant.Constants;
import cn.wuxia.aliyun.components.gateway.constant.ContentType;
import cn.wuxia.aliyun.components.gateway.constant.HttpHeader;
import cn.wuxia.aliyun.components.gateway.constant.HttpMethod;
import cn.wuxia.aliyun.components.gateway.constant.SystemHeader;
import cn.wuxia.common.web.httpclient.HttpClientException;

/**
 * Http工具类
 */
public class HttpUtil {
    public static Logger logger = LoggerFactory.getLogger("httpclient");

    /**
     * HTTP GET
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpGet(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys,
            List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        headers = initialBasicHeader(HttpMethod.GET, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);

        HttpClient httpClient = wrapClient(host);
        HttpGet get = new HttpGet(initUrl(host, path, querys));
        get.setConfig(RequestConfig.custom().setConnectTimeout(getTimeout(connectTimeout)).build());

        for (Map.Entry<String, String> e : headers.entrySet()) {
            get.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
        }

        return convert(httpClient.execute(get));
    }

    /**
     * HTTP POST表单
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPost(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys,
            Map<String, String> bodys, List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }

        headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_FORM);

        headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, bodys, signHeaderPrefixList, appKey, appSecret);

        HttpClient httpClient = wrapClient(host);

        HttpPost post = new HttpPost(initUrl(host, path, querys));
        post.setConfig(RequestConfig.custom().setConnectTimeout(getTimeout(connectTimeout)).build());
        for (Map.Entry<String, String> e : headers.entrySet()) {
            post.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
        }

        UrlEncodedFormEntity formEntity = buildFormEntity(bodys);
        if (formEntity != null) {
            post.setEntity(formEntity);
        }

        return convert(httpClient.execute(post));
    }

    public static List<Response> httpPost(String host, String path, int connectTimeout, Map<String, String> headers, List<Map<String, String>> bodys,
            String appKey, String appSecret) throws Exception {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }

        headers.put(HttpHeader.HTTP_HEADER_CONTENT_TYPE, ContentType.CONTENT_TYPE_FORM);

        HttpAsyncClientBuilder builder = HttpAsyncClients.custom();
        if (host.startsWith("https://")) {
            ssl(builder);
        }

        CloseableHttpAsyncClient httpclient = builder.build();
        List<Response> resps = Lists.newArrayList();
        try {
            // Start the client
            httpclient.start();

            // One most likely would want to use a callback for operation result
            final CountDownLatch latch = new CountDownLatch(bodys.size());
            List<Future<org.apache.http.HttpResponse>> lists = Lists.newArrayList();
            for (Map<String, String> body : bodys) {
                try {
                    // 创建httppost 
                    final HttpPost httppost = new HttpPost(initUrl(host, path, null));
                    Map<String, String> headerMap = initialBasicHeader(HttpMethod.POST, path, headers, null, body, null, appKey, appSecret);
                    httppost.setConfig(RequestConfig.custom().setConnectTimeout(getTimeout(connectTimeout)).build());
                    for (Map.Entry<String, String> e : headerMap.entrySet()) {
                        httppost.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
                    }

                    UrlEncodedFormEntity formEntity = buildFormEntity(body);
                    if (formEntity != null) {
                        httppost.setEntity(formEntity);
                    }
                    Future<org.apache.http.HttpResponse> future = httpclient.execute(httppost, new FutureCallback<org.apache.http.HttpResponse>() {
                        public void completed(final org.apache.http.HttpResponse response) {
                            latch.countDown();
                            logger.info(httppost.getRequestLine() + "->" + response.getStatusLine());
                        }

                        public void failed(final Exception ex) {
                            latch.countDown();
                            logger.warn(httppost.getRequestLine().toString(), ex);
                        }

                        public void cancelled() {
                            latch.countDown();
                            logger.warn(httppost.getRequestLine().toString() + " cancelled");
                        }

                    });
                    lists.add(future);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            try {
                latch.await();
                for (Future<org.apache.http.HttpResponse> future : lists) {
                    try {
                        org.apache.http.HttpResponse response = future.get();
                        int statusCode = response.getStatusLine().getStatusCode();
                        HttpEntity entity = response.getEntity();
                        if (entity != null && statusCode == HttpStatus.SC_OK) {
                            final Header encoding = entity.getContentEncoding();
                            if (encoding != null) {
                                for (HeaderElement element : encoding.getElements()) {
                                    if (element.getName().equalsIgnoreCase("gzip")) {
                                        entity = new GzipDecompressingEntity(entity);
                                        break;
                                    }
                                }
                            }
                            resps.add(convert(response));
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e.getMessage());
                    }
                }
            } catch (InterruptedException e) {
            }
            logger.info("完成异步请求，关闭连接");
        } finally {
            HttpAsyncClientUtils.closeQuietly(httpclient);
        }
        return resps;
    }

    /**
     * Http POST 字符串
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param body
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPost(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys,
            String body, List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);

        HttpClient httpClient = wrapClient(host);

        HttpPost post = new HttpPost(initUrl(host, path, querys));
        post.setConfig(RequestConfig.custom().setConnectTimeout(getTimeout(connectTimeout)).build());
        for (Map.Entry<String, String> e : headers.entrySet()) {
            post.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
        }

        if (StringUtils.isNotBlank(body)) {
            post.setEntity(new StringEntity(body, Constants.ENCODING));

        }

        return convert(httpClient.execute(post));
    }

    /**
     * HTTP POST 字节数组
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPost(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys,
            byte[] bodys, List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        headers = initialBasicHeader(HttpMethod.POST, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);

        HttpClient httpClient = wrapClient(host);

        HttpPost post = new HttpPost(initUrl(host, path, querys));
        post.setConfig(RequestConfig.custom().setConnectTimeout(getTimeout(connectTimeout)).build());
        for (Map.Entry<String, String> e : headers.entrySet()) {
            post.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
        }

        if (bodys != null) {
            post.setEntity(new ByteArrayEntity(bodys));
        }

        return convert(httpClient.execute(post));
    }

    /**
     * HTTP PUT 字符串
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param body
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPut(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys, String body,
            List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        headers = initialBasicHeader(HttpMethod.PUT, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);

        HttpClient httpClient = wrapClient(host);

        HttpPut put = new HttpPut(initUrl(host, path, querys));
        put.setConfig(RequestConfig.custom().setConnectTimeout(getTimeout(connectTimeout)).build());
        for (Map.Entry<String, String> e : headers.entrySet()) {
            put.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
        }

        if (StringUtils.isNotBlank(body)) {
            put.setEntity(new StringEntity(body, Constants.ENCODING));

        }

        return convert(httpClient.execute(put));
    }

    /**
     * HTTP PUT字节数组
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpPut(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys,
            byte[] bodys, List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        headers = initialBasicHeader(HttpMethod.PUT, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);

        HttpClient httpClient = wrapClient(host);

        HttpPut put = new HttpPut(initUrl(host, path, querys));
        put.setConfig(RequestConfig.custom().setConnectTimeout(getTimeout(connectTimeout)).build());
        for (Map.Entry<String, String> e : headers.entrySet()) {
            put.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
        }

        if (bodys != null) {
            put.setEntity(new ByteArrayEntity(bodys));
        }

        return convert(httpClient.execute(put));
    }

    /**
     * HTTP DELETE
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static Response httpDelete(String host, String path, int connectTimeout, Map<String, String> headers, Map<String, String> querys,
            List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        headers = initialBasicHeader(HttpMethod.DELETE, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);

        HttpClient httpClient = wrapClient(host);

        HttpDelete delete = new HttpDelete(initUrl(host, path, querys));
        delete.setConfig(RequestConfig.custom().setConnectTimeout(getTimeout(connectTimeout)).build());
        for (Map.Entry<String, String> e : headers.entrySet()) {
            delete.addHeader(e.getKey(), MessageDigestUtil.utf8ToIso88591(e.getValue()));
        }

        return convert(httpClient.execute(delete));
    }

    /**
     * 构建FormEntity
     * 
     * @param formParam
     * @return
     * @throws UnsupportedEncodingException
     */
    private static UrlEncodedFormEntity buildFormEntity(Map<String, String> formParam) throws UnsupportedEncodingException {
        if (formParam != null) {
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();

            for (String key : formParam.keySet()) {
                nameValuePairList.add(new BasicNameValuePair(key, formParam.get(key)));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, Constants.ENCODING);
            formEntity.setContentType(ContentType.CONTENT_TYPE_FORM);
            return formEntity;
        }

        return null;
    }

    private static String initUrl(String host, String path, Map<String, String> querys) throws UnsupportedEncodingException {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(host);
        if (!StringUtils.isBlank(path)) {
            sbUrl.append(path);
        }
        if (null != querys) {
            StringBuilder sbQuery = new StringBuilder();
            for (Map.Entry<String, String> query : querys.entrySet()) {
                if (0 < sbQuery.length()) {
                    sbQuery.append(Constants.SPE3);
                }
                if (StringUtils.isBlank(query.getKey()) && !StringUtils.isBlank(query.getValue())) {
                    sbQuery.append(query.getValue());
                }
                if (!StringUtils.isBlank(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (!StringUtils.isBlank(query.getValue())) {
                        sbQuery.append(Constants.SPE4);
                        sbQuery.append(URLEncoder.encode(query.getValue(), Constants.ENCODING));
                    }
                }
            }
            if (0 < sbQuery.length()) {
                sbUrl.append(Constants.SPE5).append(sbQuery);
            }
        }

        return sbUrl.toString();
    }

    /**
     * 初始化基础Header
     * @param method
     * @param path
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws MalformedURLException
     */
    private static Map<String, String> initialBasicHeader(String method, String path, Map<String, String> headers, Map<String, String> querys,
            Map<String, String> bodys, List<String> signHeaderPrefixList, String appKey, String appSecret) throws MalformedURLException {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }

        headers.put(SystemHeader.X_CA_TIMESTAMP, String.valueOf(new Date().getTime()));
        //headers.put(SystemHeader.X_CA_NONCE, UUID.randomUUID().toString());
        headers.put(SystemHeader.X_CA_KEY, appKey);
        headers.put(SystemHeader.X_CA_SIGNATURE, SignUtil.sign(appSecret, method, path, headers, querys, bodys, signHeaderPrefixList));

        return headers;
    }

    /**
     * 读取超时时间
     * 
     * @param timeout
     * @return
     */
    private static int getTimeout(int timeout) {
        if (timeout == 0) {
            return Constants.DEFAULT_TIMEOUT;
        }

        return timeout;
    }

    private static Response convert(HttpResponse response) throws IOException {
        Response res = new Response();

        if (null != response) {
            res.setStatusCode(response.getStatusLine().getStatusCode());
            for (Header header : response.getAllHeaders()) {
                res.setHeader(header.getName(), MessageDigestUtil.iso88591ToUtf8(header.getValue()));
            }

            res.setContentType(res.getHeader("Content-Type"));
            res.setRequestId(res.getHeader("X-Ca-Request-Id"));
            res.setErrorMessage(res.getHeader("X-Ca-Error-Message"));
            res.setBody(readStreamAsStr(response.getEntity().getContent()));

        } else {
            //服务器无回应
            res.setStatusCode(500);
            res.setErrorMessage("No Response");
        }

        return res;
    }

    /**
     * 将流转换为字符串
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static String readStreamAsStr(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        WritableByteChannel dest = Channels.newChannel(bos);
        ReadableByteChannel src = Channels.newChannel(is);
        ByteBuffer bb = ByteBuffer.allocate(4096);

        while (src.read(bb) != -1) {
            bb.flip();
            dest.write(bb);
            bb.clear();
        }
        src.close();
        dest.close();

        return new String(bos.toByteArray(), Constants.ENCODING);
    }

    private static HttpClient wrapClient(String host) throws HttpClientException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (host.startsWith("https://")) {
            ssl(builder);
        }

        return builder.build();
    }

    /**
     * HttpClient连接SSL
     */
    private static void ssl(HttpClientBuilder builder) throws HttpClientException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            builder.setSSLSocketFactory(sslsf);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new HttpClientException(e.getMessage());
        }
    }

    /**
     * HttpAsyncClient连接SSL
     */
    private static void ssl(HttpAsyncClientBuilder builder) throws HttpClientException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            builder.setSSLContext(sslContext);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            logger.warn(e.getMessage());
            throw new HttpClientException(e.getMessage());
        }
    }
}
