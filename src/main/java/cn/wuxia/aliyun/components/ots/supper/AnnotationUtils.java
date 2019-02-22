package cn.wuxia.aliyun.components.ots.supper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import com.aliyun.openservices.ots.OTS;
import com.aliyun.openservices.ots.model.ColumnValue;
import com.aliyun.openservices.ots.model.Row;

import cn.wuxia.aliyun.components.ots.KeyBaseMode;

/**
 * OTS反射操作Model工具类
 * @author dark
 *
 * @param <T>
 */
public class AnnotationUtils<T> {
	List<String> fidles = new ArrayList<String>();
	List<Object> fidleValues = new ArrayList<Object>();
	List<String> keys = new ArrayList<String>();
	List<String> keyValues = new ArrayList<String>();
	
	
	/**
	 * 获得表属性
	 * @param entity
	 * @return
	 */
	public  List<String> getFidles(T entity) throws Exception{
		if(fidles.size()==0){
			initFidle(entity);	
		}
		return fidles;
		
	}
	
	/**
	 * 获得表属性值
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public List<Object> getFidleValues(T entity) throws Exception{
		if(fidleValues.size()==0){
			initFidle(entity);
		}
		return fidleValues;
	}
	
	private void initFidle(T entity) throws Exception{
		if(entity !=null){
            /**返回类中所有字段，包括公共、保护、默认（包）访问和私有字段，但不包括继承的字段 
             * entity.getFields();只返回对象所表示的类或接口的所有可访问公共字段 
             * 在class中getDeclared**()方法返回的都是所有访问权限的字段、方法等； 
             * 可看API 
             * */  
            Field[] fields = FieldUtils.getAllFields(entity.getClass()); 
            for(Field f : fields){  
                //获取字段中包含fieldMeta的注解  
                FieldMeta meta = f.getAnnotation(FieldMeta.class);  
                if(meta!=null){
                	f.setAccessible(true);
                	fidleValues.add(f.get(entity));
                    fidles.add(meta.name());  
                }  
            }  
		}
	}
	
	/**
	 * 获得表的Key，一张表最多只能定义4个Key,超过4个后抛弃
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public List<String> getKeys(T entity) throws Exception{
		if(keys.size()==0){
			initKeys(entity);
		}
		return keys;
	}
	
	/**
	 * 获得表的KeyValue，一张表最多只能定义4个Key,超过4个后抛弃
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public List<String> getKeyValues(T entity) throws Exception{
		if(keyValues.size()==0){
			initKeys(entity);
		}
		return keyValues;
	}
	
	private void initKeys(T entity) throws Exception{
		if(entity !=null){
				 Field[] fields = FieldUtils.getAllFields(entity.getClass());
				 for(Field f : fields){
					 f.setAccessible(true);
					 Class<T> typeClass = (Class<T>) f.getType();
					 if(typeClass.newInstance() instanceof KeyBaseMode){
						 if(keys.size()>=4){
							 return;
						 }
						 KeyBaseMode baseMode = (KeyBaseMode)f.get(entity);
						 keys.add(baseMode.getKey());
						 keyValues.add(baseMode.getValue());
					 }
					 
				 }

		}
	}
	
	
	/**
	 * 获得表存在的阿里云的实例名称
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public String getInstanceName(T entity){
		if(entity !=null){			
		         FieldUtils.getAllFields(entity.getClass());
				 Field[] fields = FieldUtils.getAllFields(entity.getClass());
				 try{
					 for(Field f : fields){
						 f.setAccessible(true);
						 if(f.getName().equals("instanceName")){
							String value = (String)f.get(entity);
							return value;
						 }
						 				 
					 }					 
				 }catch(Exception ex){
					 ex.printStackTrace();
				 }

		}
		return null;
	}
	
	/**
	 * 获得表名称
	 * @param entity
	 * @return
	 */
	public String getTableName(T entity){
		if(entity !=null){
				TableMeta tableMeta = entity.getClass().getAnnotation(TableMeta.class);
				if(tableMeta!=null){
					return tableMeta.name();
				}
		}
		return null;
	}
	
	/**
	 * OTS返回结果对象转换成泛型entity
	 * @param entity
	 * @param row
	 * @return
	 * @throws Exception
	 */
	public T setEntity(T entity,Row row) throws Exception{
		if(row !=null && entity !=null){
			getFidles(entity);
			for(String fidle:fidles){
				ColumnValue cvalue = row.getColumns().get(fidle);
				Object value=null;
				if(cvalue==null) continue; //需要查询列在OTS找不到
				if(cvalue.getType().name().equals("STRING")){
					 value = cvalue.asString();
				}
				if(cvalue.getType().name().equals("LONG")){
					 value = cvalue.asLong();
				}
				if(cvalue.getType().name().equals("DOUBLE")){
					 value = cvalue.asDouble();
				}
				if(cvalue.getType().name().equals("BOOLEAN")){
					 value = cvalue.asBoolean();
				}
				if(cvalue.getType().name().equals("BINARY")){
					 value = cvalue.asBinary();
				}
				String methodName = "set" + fidle.substring(0,1).toUpperCase() + fidle.substring(1);
				MethodUtils.invokeMethod(entity, methodName, value);
				//Method method = entity.getClass().getMethod(methodName, value.getClass());
				//method.setAccessible(true);
				//method.invoke(entity,value);
			}
		
		}
		return entity;
	}
	
	/**
	 * 实例化泛型类
	 * @param classEntity
	 * @return
	 */
	public  T newEntity(Class<T> classEntity){
		try {
		   return classEntity.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获得CLASS的主键集合
	 * @param classEntity
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public List<KeyBaseMode> getKeyModel(Class<T> classEntity){
		List<KeyBaseMode> keyBaseModes = new ArrayList<KeyBaseMode>();
		T entity = newEntity(classEntity);
		if(entity !=null){
			 Field[] fields = classEntity.getDeclaredFields();
			 try{
				 for(Field f : fields){
					 f.setAccessible(true);
					 Class<T> typeClass = (Class<T>) f.getType();
					 if(typeClass.newInstance() instanceof KeyBaseMode){
						 KeyBaseMode baseMode = (KeyBaseMode)f.get(entity);
						 keyBaseModes.add(baseMode);
					 }
					 
				 }				 
			 }catch(Exception ex){
				 ex.printStackTrace();
			 }

		}
		return keyBaseModes;
	}
	
	public T   setKey(T entity,KeyBaseMode key){
		if(entity !=null){
			 Field[] fields = FieldUtils.getAllFields(entity.getClass());
			 try{
				 for(Field f : fields){
					 f.setAccessible(true);
					 Class<T> typeClass = (Class<T>) f.getType();
					 if(typeClass.newInstance() instanceof KeyBaseMode){
						 KeyBaseMode baseMode = (KeyBaseMode)f.get(entity);
						 if(key.getKey().equals(baseMode.getKey())){
							 f.set(entity, key);
						 }
						
					 }
					 
				 }				 
			 }catch(Exception ex){
				 ex.printStackTrace();
			 }

		}
		return entity;
	}
	
	/**
	 * 清空
	 */
	public void close(){
		fidles = new ArrayList<String>();
		fidleValues = new ArrayList<Object>();
		keys = new ArrayList<String>();
		keyValues = new ArrayList<String>();
	}

}
