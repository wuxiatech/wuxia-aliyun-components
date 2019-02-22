package cn.wuxia.aliyun.components.ots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.ClientException;
import com.aliyun.openservices.ServiceException;
import com.aliyun.openservices.ots.OTSClient;
import com.aliyun.openservices.ots.OTSErrorCode;
import com.aliyun.openservices.ots.model.BatchGetRowRequest;
import com.aliyun.openservices.ots.model.BatchGetRowResult;
import com.aliyun.openservices.ots.model.BatchWriteRowRequest;
import com.aliyun.openservices.ots.model.BatchWriteRowResult;
import com.aliyun.openservices.ots.model.BatchWriteRowResult.RowStatus;

import cn.wuxia.aliyun.components.ots.supper.AnnotationUtils;
import cn.wuxia.aliyun.components.ots.supper.OTSClientFactory;
import cn.wuxia.aliyun.components.ots.supper.RangeModel;
import cn.wuxia.common.util.reflection.ReflectionUtil;

import com.aliyun.openservices.ots.model.CapacityUnit;
import com.aliyun.openservices.ots.model.ColumnValue;
import com.aliyun.openservices.ots.model.Condition;
import com.aliyun.openservices.ots.model.CreateTableRequest;
import com.aliyun.openservices.ots.model.DeleteRowRequest;
import com.aliyun.openservices.ots.model.DeleteRowResult;
import com.aliyun.openservices.ots.model.DeleteTableRequest;
import com.aliyun.openservices.ots.model.DescribeTableRequest;
import com.aliyun.openservices.ots.model.DescribeTableResult;
import com.aliyun.openservices.ots.model.GetRangeRequest;
import com.aliyun.openservices.ots.model.GetRangeResult;
import com.aliyun.openservices.ots.model.GetRowRequest;
import com.aliyun.openservices.ots.model.GetRowResult;
import com.aliyun.openservices.ots.model.ListTableResult;
import com.aliyun.openservices.ots.model.MultiRowQueryCriteria;
import com.aliyun.openservices.ots.model.PrimaryKeyType;
import com.aliyun.openservices.ots.model.PrimaryKeyValue;
import com.aliyun.openservices.ots.model.PutRowRequest;
import com.aliyun.openservices.ots.model.PutRowResult;
import com.aliyun.openservices.ots.model.RangeRowQueryCriteria;
import com.aliyun.openservices.ots.model.ReservedThroughputChange;
import com.aliyun.openservices.ots.model.Row;
import com.aliyun.openservices.ots.model.RowDeleteChange;
import com.aliyun.openservices.ots.model.RowExistenceExpectation;
import com.aliyun.openservices.ots.model.RowPrimaryKey;
import com.aliyun.openservices.ots.model.RowPutChange;
import com.aliyun.openservices.ots.model.RowUpdateChange;
import com.aliyun.openservices.ots.model.SingleRowQueryCriteria;
import com.aliyun.openservices.ots.model.TableMeta;
import com.aliyun.openservices.ots.model.UpdateRowRequest;
import com.aliyun.openservices.ots.model.UpdateRowResult;
import com.aliyun.openservices.ots.model.UpdateTableRequest;
import com.aliyun.openservices.ots.model.UpdateTableResult;


public class SimpleOTSDao<T, PK extends KeyBaseMode> {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected OTSClientFactory clientFactory;
	
	protected Class<T> entityClass;
	
	protected AnnotationUtils<T> annotationUtils = new AnnotationUtils<>();
	
	
	public SimpleOTSDao(){
		this.entityClass = ReflectionUtil.getSuperClassGenricType(getClass());
		if(entityClass!=null){
			T entity = annotationUtils.newEntity(entityClass);
			if(entity!=null){
				String instanceName = annotationUtils.getInstanceName(entity);
				clientFactory = new OTSClientFactory(instanceName);	
			}
			
		}
		annotationUtils.close();		
	}
	
	public SimpleOTSDao(final Class<T> entityClass){
		this.entityClass = entityClass;
		if(entityClass!=null){
			T entity = annotationUtils.newEntity(entityClass);
			if(entity!=null){
				String instanceName = annotationUtils.getInstanceName(entity);
				clientFactory = new OTSClientFactory(instanceName);	
			}
			
		}
		annotationUtils.close();	
	}

	private void checkOTSClient() throws Exception{
		if(clientFactory==null){
			throw new Exception("OTS客户端未被初期化");
		}
	}
	
	
	////////////////////////////
	// OTS表操作
	///////////////////////////
	/**
	 * 创建表。一个实例最多创建10张表
	 * @param entity 表对象
	 * @param readCapacity 读吐出量
	 * @param writeCapcity 写吐出量
	 */
	public void CreateTable(final T entity,int readCapacity,int writeCapcity){
		try{
			checkOTSClient();
			List<String> keys = annotationUtils.getKeys(entity);
			String tableName = annotationUtils.getTableName(entity);
			OTSClient client = clientFactory.getOTSClient();
			TableMeta tableMeta = new TableMeta(tableName);
			for(String key:keys){
				tableMeta.addPrimaryKeyColumn(key, PrimaryKeyType.STRING);
			}
			// 将该表的读写CU都设置为1
			CapacityUnit capacityUnit = new CapacityUnit(readCapacity, writeCapcity);
			 
			CreateTableRequest request = new CreateTableRequest();
			request.setTableMeta(tableMeta);
			request.setReservedThroughput(capacityUnit);
			client.createTable(request);
		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
		  logger.error(ex.getMessage());	
		}finally{
			annotationUtils.close();
		}
	}
	
	public void CreateTable(final T entity){
		CreateTable(entity, 100, 100);
	}
	
	/**
	 * 删除表
	 * @param entity
	 */
	public void DeleteTable(final T entity){
		try{
			checkOTSClient();
			DeleteTableRequest request = new DeleteTableRequest();
			String tableName = annotationUtils.getTableName(entity);
			OTSClient client = clientFactory.getOTSClient();
	        request.setTableName(tableName);
	        client.deleteTable(request);
		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
	}
	
	/**
	 * 查看表属性
	 * @param entity
	 */
	public TableMeta GetTableMeta(final T entity){
		try{
			checkOTSClient();
			OTSClient client = clientFactory.getOTSClient();
			String tableName = annotationUtils.getTableName(entity);
			DescribeTableRequest request = new DescribeTableRequest();
	        request.setTableName(tableName);
	        DescribeTableResult result = client.describeTable(request);
	        TableMeta tableMeta = result.getTableMeta();			
	        return tableMeta;
		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
		return null;

    }
	
	/**
	 * 获得当前表的实例下的全部表名称
	 * @param entity
	 * @return
	 */
	public ListTableResult ListTable(final T entity){
		try{
			checkOTSClient();
			OTSClient client = clientFactory.getOTSClient();
			ListTableResult result = client.listTable();
			return result;
		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
		return null;
	}
	
	/**
	 * 调整表的读写配额。是调整表的热点分布情况。每天可以修改4次
	 * @param entity 表对象
	 * @param readCapacity 读配额 最多5000
	 * @param writeCapacityUnit 写配额最大5000
	 */
	public void updateTable(final T entity,int readCapacity,int writeCapacityUnit){
		try{
			checkOTSClient();			
			String tableName = annotationUtils.getTableName(entity);
			OTSClient client = clientFactory.getOTSClient();
	        UpdateTableRequest request = new UpdateTableRequest();
	        request.setTableName(tableName);
	        ReservedThroughputChange cuChange = new ReservedThroughputChange();
	        cuChange.setReadCapacityUnit(readCapacity); // 若想单独调整写CU，则无须设置读CU
	        cuChange.setWriteCapacityUnit(writeCapacityUnit); // 若想单独调整读CU，则无须设置写CU
	        request.setReservedThroughputChange(cuChange);
	        UpdateTableResult result = client.updateTable(request);

		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}

	}
	
	
	//////////////////////////////////////////////
	//OTS数据操作
	/////////////////////////////////////////////
	/**
	 * 创建一行记录
	 * @param entity
	 */
	public void PutRow(final T entity){
		try{
			checkOTSClient();
			OTSClient client = clientFactory.getOTSClient();
			RowPutChange rowChange = setRowPutChange(entity);
			
	        PutRowRequest request = new PutRowRequest();
	        request.setRowChange(rowChange);

	        PutRowResult result = client.putRow(request);
	        int consumedWriteCU = result.getConsumedCapacity().getCapacityUnit().getWriteCapacityUnit();

		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
	}
	
	
	/**
	 *  批量创建记录。最多不能超过100条记录
	 * @param entitys
	 */
	public void MultiPutRow(final List<T> entitys){
		try{
			if(entitys.size()==0){
				throw new Exception("没有需要创建的记录");
			}
			if(entitys.size()>100){
				throw new Exception("批量新增不能超过100条记录");
			}
			checkOTSClient();
			OTSClient client = clientFactory.getOTSClient();
			BatchWriteRowRequest request = new BatchWriteRowRequest();
			for(T entity:entitys){
				annotationUtils.close();
				RowPutChange rowChange = setRowPutChange(entity);
				request.addRowPutChange(rowChange);
			}
		
		    // batchWriteRow接口会返回一个结果集， 结果集中包含的结果个数与插入的行数相同。 结果集中的结果不一定都是成功，
	        // 用户需要自己对不成功的操作进行重试。

	        BatchWriteRowResult result = client.batchWriteRow(request);
	       
	        BatchWriteRowRequest failedOperations = null;
	        int succeedCount = 0;
	        
	        int retryCount = 0;
	        do {
	            Map<String, List<RowStatus>> status = result.getPutRowStatus();
	            failedOperations = new BatchWriteRowRequest();
	            for (Entry<String, List<RowStatus>> entry : status.entrySet()) {
	                String tableName = entry.getKey();
	                List<RowStatus> statuses = entry.getValue();
	                for (int index = 0; index < statuses.size(); index++) {
	                    RowStatus rowStatus = statuses.get(index);
	                    if (!rowStatus.isSucceed()) {
	                        // 操作失败， 需要放到重试列表中再次重试
	                        // 需要重试的操作可以从request中获取参数
	                        failedOperations.addRowPutChange(request
	                                .getRowPutChange(tableName, index));
	                    } else {
	                        succeedCount++;
	                        logger.info("本次操作消耗的写CapacityUnit为：" + rowStatus.getConsumedCapacity().getCapacityUnit().getWriteCapacityUnit());
	                    }
	                }
	            }

	            if (failedOperations.isEmpty() || ++retryCount > 3) {
	                break; // 如果所有操作都成功了或者重试次数达到上线， 则不再需要重试。
	            }
                
	            // 如果有需要重试的操作， 则稍微等待一会后再次重试， 否则继续出错的概率很高。
	            try {
	                Thread.sleep(100); // 100ms后继续重试
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }

	            request = failedOperations;
	            result = client.batchWriteRow(request);
	        } while (true);

		}catch(ServiceException e){
			e.printStackTrace();
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
	}
	
	private RowPutChange setRowPutChange(final T entity) throws Exception{
		String tableName = annotationUtils.getTableName(entity);
		List<String> keys = annotationUtils.getKeys(entity);
		List<String> keyVaules = annotationUtils.getKeyValues(entity);
		List<String> fidles = annotationUtils.getFidles(entity);
		List<Object> fidleValues = annotationUtils.getFidleValues(entity);
		RowPutChange rowChange = new RowPutChange(tableName);
        RowPrimaryKey primaryKey = new RowPrimaryKey();
        for(int i=0;i<keys.size();i++){
	        primaryKey.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.fromString(keyVaules.get(i)));	        	
        }
        rowChange.setPrimaryKey(primaryKey);
        for(int i=0;i<fidles.size();i++){
        	rowChange.addAttributeColumn(fidles.get(i), setColumnValue(fidleValues.get(i)));
        }

        rowChange.setCondition(new Condition(RowExistenceExpectation.EXPECT_NOT_EXIST)); 
        return rowChange;
	}
	
	/**
	 * 删除一行记录
	 * @param entity
	 */
	public void DeleteRow(final T entity){
		try{
			checkOTSClient();
			String tableName = annotationUtils.getTableName(entity);
			List<String> keys = annotationUtils.getKeys(entity);
			List<String> keyVaules = annotationUtils.getKeyValues(entity);
			OTSClient client = clientFactory.getOTSClient();
	        RowDeleteChange rowChange = new RowDeleteChange(tableName);
	        RowPrimaryKey primaryKeys = new RowPrimaryKey();
	        for(int i=0;i<keys.size();i++){
		        primaryKeys.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.fromString(keyVaules.get(i)));	        	
	        }
	        rowChange.setPrimaryKey(primaryKeys);
	        
	        DeleteRowRequest request = new DeleteRowRequest();
	        request.setRowChange(rowChange);

	        DeleteRowResult result = client.deleteRow(request);
	        int consumedWriteCU = result.getConsumedCapacity().getCapacityUnit().getWriteCapacityUnit();

		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
	}
	
	/**
	 *  批量删除记录。最多不能超过100条记录
	 * @param entitys
	 */
	public void MultiDeleteRow(final List<T> entitys){
		try{
			if(entitys.size()==0){
				throw new Exception("没有需要删除的记录");
			}
			if(entitys.size()>100){
				throw new Exception("批量删除不能超过100条记录");
			}
			checkOTSClient();
			String tableName = annotationUtils.getTableName(entitys.get(0));
			OTSClient client = clientFactory.getOTSClient();
			BatchWriteRowRequest request = new BatchWriteRowRequest();
			for(T entity:entitys){
				annotationUtils.close();
				List<String> keys = annotationUtils.getKeys(entity);
				List<String> keyVaules = annotationUtils.getKeyValues(entity);
				RowDeleteChange rowChange = new RowDeleteChange(tableName);
		        RowPrimaryKey primaryKeys = new RowPrimaryKey();
		        for(int i=0;i<keys.size();i++){
			        primaryKeys.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.fromString(keyVaules.get(i)));	        	
		        }
		        rowChange.setPrimaryKey(primaryKeys);
				request.addRowDeleteChange(rowChange);
			}

		    // batchWriteRow接口会返回一个结果集， 结果集中包含的结果个数与插入的行数相同。 结果集中的结果不一定都是成功，
	        // 用户需要自己对不成功的操作进行重试。
	        BatchWriteRowResult result = client.batchWriteRow(request);
	        BatchWriteRowRequest failedOperations = null;
	        int succeedCount = 0;

	        int retryCount = 0;
	        do {
	            Map<String, List<RowStatus>> status = result.getPutRowStatus();
	            failedOperations = new BatchWriteRowRequest();
	            for (Entry<String, List<RowStatus>> entry : status.entrySet()) {
	                tableName = entry.getKey();
	                List<RowStatus> statuses = entry.getValue();
	                for (int index = 0; index < statuses.size(); index++) {
	                    RowStatus rowStatus = statuses.get(index);
	                    if (!rowStatus.isSucceed()) {
	                        // 操作失败， 需要放到重试列表中再次重试
	                        // 需要重试的操作可以从request中获取参数
	                        failedOperations.addRowDeleteChange(request
	                                .getRowDeleteChange(tableName, index));
	                    } else {
	                        succeedCount++;
	                        logger.info("本次操作消耗的写CapacityUnit为：" + rowStatus.getConsumedCapacity().getCapacityUnit().getWriteCapacityUnit());
	                    }
	                }
	            }

	            if (failedOperations.isEmpty() || ++retryCount > 3) {
	                break; // 如果所有操作都成功了或者重试次数达到上线， 则不再需要重试。
	            }

	            // 如果有需要重试的操作， 则稍微等待一会后再次重试， 否则继续出错的概率很高。
	            try {
	                Thread.sleep(100); // 100ms后继续重试
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }

	            request = failedOperations;
	            result = client.batchWriteRow(request);
	        } while (true);
			

		}catch(ServiceException e){
			e.printStackTrace();
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
	}
	
	//(working)
	public void BatchModifyRow(final T entity){
		try{
			String instanceName = annotationUtils.getInstanceName(entity);
			String tableName = annotationUtils.getTableName(entity);
			List<String> keys = annotationUtils.getKeys(entity);
			List<String> keyVaules = annotationUtils.getKeyValues(entity);
			List<String> fidles = annotationUtils.getFidles(entity);
			List<Object> fidleValues = annotationUtils.getFidleValues(entity);
			OTSClientFactory clientFactory = new OTSClientFactory(instanceName);
			OTSClient client = clientFactory.getOTSClient();
			

		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
	}
	
	/**
	 * 获得一行记录。colNames为空的情况默认全部字段属性
	 * @param entity 表对象
	 * @param colNames 需要显示列
	 * @return
	 */
	public T GetRow(final T entity,String... colNames){
		try{
			checkOTSClient();
			String tableName = annotationUtils.getTableName(entity);
			List<String> keys = annotationUtils.getKeys(entity);
			List<String> keyVaules = annotationUtils.getKeyValues(entity);
			List<String> fidles = annotationUtils.getFidles(entity);
			OTSClient client = clientFactory.getOTSClient();
	        SingleRowQueryCriteria criteria = new SingleRowQueryCriteria(tableName);
	        RowPrimaryKey primaryKeys = new RowPrimaryKey();
	        for(int i=0;i<keys.size();i++){
		         primaryKeys.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.fromString(keyVaules.get(i)));	        	
	        }
	        criteria.setPrimaryKey(primaryKeys);
	        if(colNames !=null){
	        	criteria.addColumnsToGet(colNames);
	        }else{
	        	String[] allColNams = new  String[fidles.size()];
		        for(int i=0;i<fidles.size();i++){
		        	allColNams[i]=fidles.get(i);
		        }
		        criteria.addColumnsToGet(allColNams);
	        }
	        GetRowRequest request = new GetRowRequest();
	        request.setRowQueryCriteria(criteria);
	        GetRowResult result = client.getRow(request);
	        Row row = result.getRow();
	        row.getColumns();
	        annotationUtils.setEntity(entity,row);
	        return entity;
	        //int consumedReadCU = result.getConsumedCapacity().getCapacityUnit().getReadCapacityUnit();

		}catch(ServiceException e){
			e.printStackTrace();
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
        	e.printStackTrace();
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
        	ex.printStackTrace();
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
		return null;
		
	}
	
	public T GetRow(final T entity){
		return GetRow(entity, null);
	}
	

	/**
	 * 获得查询行记录。最大获得10条记录
	 * @param entitys 表列表。需要查询的KEY都存放在该列
	 * @param colNames 查询列
	 * @return
	 */
	public List<T> MultiGetRow(List<T> entitys,String... colNames){
		try{
			if(entitys.size()==0){
				throw new Exception("没有需要更新的记录");
			}
			if(entitys.size()>10){
				throw new Exception("每次查询最多为10条记录");
			}
			checkOTSClient();
			String tableName = annotationUtils.getTableName(entitys.get(0));
			List<String> fidles = annotationUtils.getFidles(entitys.get(0));
			OTSClient client = clientFactory.getOTSClient();

			BatchGetRowRequest request = new BatchGetRowRequest();
			MultiRowQueryCriteria tableRows = new MultiRowQueryCriteria(tableName);
			for (T entity:entitys) {
				annotationUtils.close();
				List<String> keys = annotationUtils.getKeys(entity);
				List<String> keyVaules = annotationUtils.getKeyValues(entity);
			    RowPrimaryKey primaryKeys = new RowPrimaryKey();
		        for(int i=0;i<keys.size();i++){
			        primaryKeys.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.fromString(keyVaules.get(i)));	        	
		        }
			    tableRows.addRow(primaryKeys);
			}
	        if(colNames !=null){
	        	tableRows.addColumnsToGet(colNames);
	        }else{
	        	String[] allColNams = new  String[fidles.size()];
		        for(int i=0;i<fidles.size();i++){
		        	allColNams[i]=fidles.get(i);
		        }
		        tableRows.addColumnsToGet(allColNams);
	        }
			request.addMultiRowQueryCriteria(tableRows);
			 
			BatchGetRowResult result = client.batchGetRow(request);
			BatchGetRowRequest failedOperations = null;
			 
			List<Row> succeedRows = new ArrayList<Row>();
			 
			int retryCount = 0;
			do {
			    failedOperations = new BatchGetRowRequest();
			 
			    Map<String, List<com.aliyun.openservices.ots.model.BatchGetRowResult.RowStatus>> status = result
			            .getTableToRowsStatus();
			    for (Entry<String, List<com.aliyun.openservices.ots.model.BatchGetRowResult.RowStatus>> entry : status
			            .entrySet()) {
			        tableName = entry.getKey();
			        tableRows = new MultiRowQueryCriteria(tableName);
			        List<com.aliyun.openservices.ots.model.BatchGetRowResult.RowStatus> statuses = entry
			                .getValue();
			        for (int index = 0; index < statuses.size(); index++) {
			            com.aliyun.openservices.ots.model.BatchGetRowResult.RowStatus rowStatus = statuses
			                    .get(index);
			            if (!rowStatus.isSucceed()) {
			                // 操作失败， 需要放到重试列表中再次重试
			                // 需要重试的操作可以从request中获取参数
			                tableRows.addRow(request
			                        .getPrimaryKey(tableName, index));
			            } else {
			                succeedRows.add(rowStatus.getRow());
			                
			                logger.info("本次读操作消耗的读CapacityUnti为：" + rowStatus.getConsumedCapacity().getCapacityUnit().getReadCapacityUnit());
			            }
			        }
			 
			        if (!tableRows.getRowKeys().isEmpty()) {
				        if(colNames !=null){
				        	tableRows.addColumnsToGet(colNames);
				        }else{
				        	String[] allColNams = new  String[fidles.size()];
					        for(int i=0;i<fidles.size();i++){
					        	allColNams[i]=fidles.get(i);
					        }
					        tableRows.addColumnsToGet(allColNams);
				        }
			            failedOperations.addMultiRowQueryCriteria(tableRows);
			        }
			    }
			    if (failedOperations.isEmpty() || ++retryCount > 3) {
			        break; // 如果所有操作都成功了或者重试次数达到上线， 则不再需要重试。
			    }
			 
			    // 如果有需要重试的操作， 则稍微等待一会后再次重试， 否则继续出错的概率很高。
			    try {
			        Thread.sleep(100); // 100ms后继续重试
			    } catch (InterruptedException e) {
			        e.printStackTrace();
			    }
			 
			    request = failedOperations;
			    result = client.batchGetRow(request);
			} while (true);
			T entityTemp = entitys.get(0);
			entitys = new ArrayList<T>();
			for(Row row:succeedRows){
				T entity = annotationUtils.setEntity((T) entityTemp.getClass().newInstance(), row);
				entitys.add(entity);
			}
			return entitys;
		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
        	e.printStackTrace();
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
		return null;
	}
	
	public List<T> MultiGetRow(List<T> entitys){
		return MultiGetRow(entitys, null);
	}
	
	/**
	 * 获取范围记录每次返回数据的行数超过5000行，或者返回数据的数据大小大于1MB。以上任一条件满足时，超出上限的数据将会被截掉
	 * 在只有1个主键的情况下
	 * @param entity 表对象
	 * @param rangeModels 需要搜寻的主键类
	 * @param colNames 查询列。默认为全部列
	 * @return
	 */
	public List<T> GetRowsByRange(final T entity,List<RangeModel> rangeModels,String... colNames){
		try{
			checkOTSClient();
			String tableName = annotationUtils.getTableName(entity);
			List<String> keys = annotationUtils.getKeys(entity);
			List<String> fidles = annotationUtils.getFidles(entity);
			OTSClient client = clientFactory.getOTSClient();
			RangeRowQueryCriteria criteria = new RangeRowQueryCriteria(tableName);
			
			RowPrimaryKey inclusiveStartKey = new RowPrimaryKey();
			RangeModel rangeModel=null;
	        for(int i=0;i<keys.size();i++){
	        	for(RangeModel rangeModel_t:rangeModels){
		        	if(rangeModel_t.getRangeKeyName().equals(keys.get(i))){
		        		rangeModel = rangeModel_t;
		        	}      
	        	}
	        	if(rangeModel!=null){
	        		inclusiveStartKey.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.fromString(rangeModel.getStart()));	        
	        	}else{
	        		// 范围的边界需要提供完整的PK，若查询的范围不涉及到某一列值的范围，则需要将该列设置为无穷大或者无穷小
	        		inclusiveStartKey.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.INF_MIN);	        
	        	}
	        	rangeModel=null;
	        }
	        RowPrimaryKey exclusiveEndKey = new RowPrimaryKey();
	        for(int i=0;i<keys.size();i++){
	        	for(RangeModel rangeModel_t:rangeModels){
		        	if(rangeModel_t.getRangeKeyName().equals(keys.get(i))){
		        		rangeModel= rangeModel_t;       
		        	}	
	        	}
	        	if(rangeModel!=null){
	        		exclusiveEndKey.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.fromString(rangeModel.getEnd()));	        
	        	}else{
	        		// 范围的边界需要提供完整的PK，若查询的范围不涉及到某一列值的范围，则需要将该列设置为无穷大或者无穷小
	        		exclusiveEndKey.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.INF_MAX);	
	        	}
	        	rangeModel=null;
	        }	        
	        if(colNames !=null){
	        	criteria.addColumnsToGet(colNames);
	        }else{
	        	String[] allColNams = new  String[fidles.size()];
		        for(int i=0;i<fidles.size();i++){
		        	allColNams[i]=fidles.get(i);
		        }
		        criteria.addColumnsToGet(allColNams);
	        }			
	        criteria.setInclusiveStartPrimaryKey(inclusiveStartKey);
	        criteria.setExclusiveEndPrimaryKey(exclusiveEndKey);
	        GetRangeRequest request = new GetRangeRequest();
	        request.setRangeRowQueryCriteria(criteria);
	        GetRangeResult result = client.getRange(request);
	        List<Row> rows = result.getRows();
	        List<T> entitys = new ArrayList<>();
	        for (Row row : rows) {
	          T newEntity = (T) entity.getClass().newInstance();
	          entitys.add(annotationUtils.setEntity(newEntity, row));
	        }
//	        int consumedReadCU = result.getConsumedCapacity().getCapacityUnit()
//	                .getReadCapacityUnit();
	        return entitys;

		}catch(ServiceException e){
			e.printStackTrace();
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
        	e.printStackTrace();
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
        	ex.printStackTrace();
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
		return null;
	}

	public List<T> GetRowsByRange(final T entity,List<RangeModel> rangeModels){
		return GetRowsByRange(entity, rangeModels, null);
	}
	
	/**
	 * 更新记录
	 * @param entity
	 */
	public void updateRow(final T entity){
		try{
			checkOTSClient();
			OTSClient client = clientFactory.getOTSClient();
			RowUpdateChange rowChange = setRowUpdateChange(entity);
	        
	        UpdateRowRequest request = new UpdateRowRequest();
	        request.setRowChange(rowChange);

	        UpdateRowResult result = client.updateRow(request);
	       
	        int consumedWriteCU = result.getConsumedCapacity().getCapacityUnit().getWriteCapacityUnit();			

		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
		}
	}
	
	public void MultiUpdateRow(final List<T> entitys){
		try{	
			if(entitys.size()==0){
				throw new Exception("没有需要更新的记录");
			}
			if(entitys.size()>100){
				throw new Exception("批量更新不能超过100条记录");
			}
			checkOTSClient();
			OTSClient client = clientFactory.getOTSClient();
			BatchWriteRowRequest request = new BatchWriteRowRequest();
			for(T entity:entitys){
				annotationUtils = new AnnotationUtils<T>();
				RowUpdateChange rowChange = setRowUpdateChange(entity);
				request.addRowUpdateChange(rowChange);
			}

		    // batchWriteRow接口会返回一个结果集， 结果集中包含的结果个数与插入的行数相同。 结果集中的结果不一定都是成功，
	        // 用户需要自己对不成功的操作进行重试。
	        BatchWriteRowResult result = client.batchWriteRow(request);
	        BatchWriteRowRequest failedOperations = null;
	        int succeedCount = 0;

	        int retryCount = 0;
	        do {
	            Map<String, List<RowStatus>> status = result.getPutRowStatus();
	            failedOperations = new BatchWriteRowRequest();
	            for (Entry<String, List<RowStatus>> entry : status.entrySet()) {
	                String tableName = entry.getKey();
	                List<RowStatus> statuses = entry.getValue();
	                for (int index = 0; index < statuses.size(); index++) {
	                    RowStatus rowStatus = statuses.get(index);
	                    if (!rowStatus.isSucceed()) {
	                        // 操作失败， 需要放到重试列表中再次重试
	                        // 需要重试的操作可以从request中获取参数
	                        failedOperations.addRowUpdateChange(request
	                                .getRowUpdateChange(tableName, index));
	                    } else {
	                        succeedCount++;
	                        logger.info("本次操作消耗的写CapacityUnit为：" + rowStatus.getConsumedCapacity().getCapacityUnit().getWriteCapacityUnit());
	                    }
	                }
	            }

	            if (failedOperations.isEmpty() || ++retryCount > 3) {
	                break; // 如果所有操作都成功了或者重试次数达到上线， 则不再需要重试。
	            }

	            // 如果有需要重试的操作， 则稍微等待一会后再次重试， 否则继续出错的概率很高。
	            try {
	                Thread.sleep(100); // 100ms后继续重试
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }

	            request = failedOperations;
	            result = client.batchWriteRow(request);
	        } while (true);

		}catch(ServiceException e){
			logger.error("操作失败，详情：" + e.getMessage());
            // 可以根据错误代码做出处理， OTS的ErrorCode定义在OTSErrorCode中。
            if (OTSErrorCode.QUOTA_EXHAUSTED.equals(e.getErrorCode())){
            	logger.error("超出存储配额。");
            }
            // Request ID可以用于有问题时联系客服诊断异常。
            logger.error("Request ID:" + e.getRequestId());
        }catch(ClientException e){
            // 可能是网络不好或者是返回结果有问题
        	logger.error("请求失败，详情：" + e.getMessage());
        } catch (InterruptedException e) {
        	logger.error(e.getMessage());
        }catch(Exception ex){
			logger.error(ex.getMessage());
        }finally{
			annotationUtils.close();
		}
	}
	
	private RowUpdateChange setRowUpdateChange(final T entity) throws Exception{
		List<String> keys = annotationUtils.getKeys(entity);
		List<String> keyVaules = annotationUtils.getKeyValues(entity);
		List<String> fidles = annotationUtils.getFidles(entity);
		List<Object> fidleValues = annotationUtils.getFidleValues(entity);
		String tableName = annotationUtils.getTableName(entity);
		RowUpdateChange rowChange = new RowUpdateChange(tableName);
        RowPrimaryKey primaryKeys = new RowPrimaryKey();
        for(int i=0;i<keys.size();i++){
	        primaryKeys.addPrimaryKeyColumn(keys.get(i), PrimaryKeyValue.fromString(keyVaules.get(i)));	        	
        }
        rowChange.setPrimaryKey(primaryKeys);
        for(int i=0;i<fidles.size();i++){
        	rowChange.addAttributeColumn(fidles.get(i), setColumnValue(fidleValues.get(i)));
        }
        rowChange.setCondition(new Condition(RowExistenceExpectation.EXPECT_EXIST));
        return rowChange;
	}
	
	
	//Transaction
	public void StartTransaction(){
		
	}
	public void CommitTransaction(){
		
	}
	public void AbortTransaction(){
		
	}
	
	
	private ColumnValue setColumnValue(Object value){
		if(value instanceof String){
			return ColumnValue.fromString((String)value);
		}
		if(value instanceof Long){
			return ColumnValue.fromLong((Long)value);
		}
		if(value instanceof Double){
			return ColumnValue.fromDouble((Double)value);
		}
		if(value instanceof Boolean){
			return ColumnValue.fromBoolean((Boolean)value);
		}
		if(value instanceof byte[]){
			return ColumnValue.fromBinary((byte[])value);
		}
		return null;
	}
	


}
