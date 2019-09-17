package cn.wuxia.aliyun.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author songlin
 */
@Getter
@Setter
@AllArgsConstructor
public class ApiAccount {
    private String appKey;

    private String appSecret;

    private String appCode;

    public ApiAccount(String appKey, String appSecret) {
        this.appKey = appKey;
        this.appSecret = appSecret;
    }


}
