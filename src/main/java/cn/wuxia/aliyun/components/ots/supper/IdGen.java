package cn.wuxia.aliyun.components.ots.supper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class IdGen {

	/**
	 * OTS主键定义生成方法。长度为32位主键。前11位为预留字段
	 * 00000000000 2015 06 11 15 15 40 961 2fXS
	 *    预留      yyyy MM dd HH mm ss SSS 校验码
	 * @return
	 */
	public static String genId() {
		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHssmmSSS");
		 String timeDate = dateFormat.format(new Date());
		 String v = getRandomString(4);
		 timeDate ="00000000000" + timeDate + v;
		 return timeDate;
		 
	}
	
	private static String getRandomString(int length){
	    String str="01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	    Random random=new Random();
	    StringBuffer sb=new StringBuffer();
	    for(int i=0;i<length;i++){
	      int number=random.nextInt(62);
	      sb.append(str.charAt(number));
	    }
	    return sb.toString();
	}
	
}
