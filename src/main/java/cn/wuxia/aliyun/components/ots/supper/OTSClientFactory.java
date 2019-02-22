package cn.wuxia.aliyun.components.ots.supper;

import com.aliyun.openservices.ots.OTSClient;

public class OTSClientFactory {
	
	private String endPoint = "";
	private String accessId = "";
	private String accessKey = "";
	private String instanceName = "";
	private OTSClient client;
	
	public OTSClientFactory(String instanceName){
		endPoint="http://" + instanceName +".cn-" + OTSConstant.CN_HANGZHOU;
		accessId = "vua23wc72T3x2p5r";
		accessKey = "fXomglL2W8LBJODreuNDLBDKBxkrVy";
		this.instanceName = instanceName;
	}
	
	public OTSClient getOTSClient(){
		 client = new OTSClient(endPoint, accessId, accessKey, instanceName);
		 return client;
	}
	

}
