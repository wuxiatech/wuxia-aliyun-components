/*
* Created on :2017年3月2日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.aliyun.components.ocr.bean;

import java.io.Serializable;

public class InputBean implements Serializable {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7136501390901611578L;

    ImageBean image;

    ConfigureBean configure;

    public InputBean() {
    }

    public InputBean(ImageBean image, ConfigureBean configure) {
        this.image = image;
        this.configure = configure;
    }

    public ImageBean getImage() {
        return image;
    }

    public void setImage(ImageBean image) {
        this.image = image;
    }

    public ConfigureBean getConfigure() {
        return configure;
    }

    public void setConfigure(ConfigureBean configure) {
        this.configure = configure;
    }

}
