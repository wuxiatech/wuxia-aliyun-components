/*
* Created on :2017年2月13日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ons.bean;

import com.aliyun.openservices.ons.api.bean.Subscription;
import javax.validation.constraints.NotBlank;

public class BasicONSBean extends Subscription {

    private String name;

    private boolean istartup;

    private boolean isorder;

    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIstartup() {
        return istartup;
    }

    public void setIstartup(boolean istartup) {
        this.istartup = istartup;
    }

    public boolean getIsorder() {
        return isorder;
    }

    public void setIsorder(boolean isorder) {
        this.isorder = isorder;
    }
}
