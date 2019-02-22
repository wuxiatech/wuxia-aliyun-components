package cn.wuxia.aliyun.components.ots;

import java.util.*;

import org.springframework.util.Assert;

import cn.wuxia.aliyun.components.ots.supper.Pages;
import cn.wuxia.aliyun.components.ots.supper.RangeModel;

/**
 * dao支持类。用于辅助查询
 * @author dark
 *
 * @param <T>
 * @param <PK>
 */
public class SupportOTSDao<T, PK extends KeyBaseMode> extends SimpleOTSDao<T, KeyBaseMode> {

	public SupportOTSDao(){
		super();
	}
	
	public SupportOTSDao(Class<T> entityClass) {
		super(entityClass);
		
	}
	
	  public Pages<T> findPage(final Pages<T> page,List<RangeModel> rangeModels) {
		  Assert.notNull(page, "page can not be null");
		  List<T> entityList = super.GetRowsByRange(annotationUtils.newEntity(entityClass), rangeModels);
		  
		  if (page.isAutoCount()) {
		      long totalCount = entityList.size();
		      page.setTotalCount(totalCount);
		      if (totalCount == 0) {
		          return page;
		      }
		  }
		  int pageNo = page.getPageNo();
		  int pageSize = page.getPageSize();
		  int startNo = (pageNo-1)*pageSize;
		  int endNo = pageNo*pageSize;
		  if(endNo>page.getTotalCount()){
			  endNo = (int)page.getTotalCount();
		  }
		  List<T> result = new ArrayList<T>();
		  for(int i=startNo;i<endNo;i++){
			  result.add(entityList.get(i));
		  }
		  page.setResult(result);
		  return page;
	}
	
}
