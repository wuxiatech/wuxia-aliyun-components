package cn.wuxia.aliyun.components.ots;

import java.io.Serializable;

/**
 * 主键基类。最多支持4列
 * @author dark
 *
 */
public class KeyBaseMode  implements Serializable{

	public KeyBaseMode(){
		
	}
	
	private String key;
	
	private String value;

	public KeyBaseMode(String key){
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}



	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
