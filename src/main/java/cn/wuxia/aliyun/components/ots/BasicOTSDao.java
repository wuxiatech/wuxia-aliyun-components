package cn.wuxia.aliyun.components.ots;

import java.util.*;

import org.springframework.util.Assert;

public class BasicOTSDao<T, PK extends KeyBaseMode> extends SupportOTSDao<T, KeyBaseMode> {

	public BasicOTSDao(){
		super();
	}
	
	public BasicOTSDao(Class<T> entityClass) {
		super(entityClass);
	}
	
	public void save(T entity){
		Assert.notNull(entity, "entity Can not be null");
		super.PutRow(entity);
	}

	public void deleteById(final KeyBaseMode... id){
		Assert.notNull(id, "pk Can not be null");
	    List<KeyBaseMode> baseModes = annotationUtils.getKeyModel(entityClass);
	    T entity = annotationUtils.newEntity(entityClass);
	    for(KeyBaseMode baseMode:baseModes){
	    	for(KeyBaseMode formId:id){
		    	if(baseMode.getKey().equals(formId.getKey())){
		    		entity = annotationUtils.setKey(entity, formId);
		    	}	
	    	}
	    }
	    super.DeleteRow(entity);	    
	}
	
	public void delete(T entity){
		Assert.notNull(entity, "entity Can not be null");
		super.DeleteRow(entity);
	}
	
	public void update(T entity){
		Assert.notNull(entity, "entity Can not be null");
		super.updateRow(entity);
	}
	
	public T get(final KeyBaseMode... id){
		Assert.notNull(id, "pk Can not be null");
	    List<KeyBaseMode> baseModes = annotationUtils.getKeyModel(entityClass);
	    T entity = annotationUtils.newEntity(entityClass);
	    for(KeyBaseMode baseMode:baseModes){
	    	for(KeyBaseMode formId:id){
		    	if(baseMode.getKey().equals(formId.getKey())){
		    		entity = annotationUtils.setKey(entity, formId);
		    	}	
	    	}
	    }
	    return super.GetRow(entity);
	}
	
	public T get(T entity){
		return super.GetRow(entity);
	}
	

}
