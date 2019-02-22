package cn.wuxia.aliyun.components.ots.supper;

import java.io.Serializable;
import java.util.*;

public class Pages<T> implements Serializable {
    private static final long serialVersionUID = -6560967807524300982L;

    // -- page parameter --//
    protected int pageNo = 1;

    protected int pageSize = -1;

    protected boolean autoCount = true;

    protected boolean autoReturnFirstPage = false;

    // -- return result --//
    protected List<T> result = new ArrayList<T>();


    protected long totalCount = -1;

    protected int totalPages = 0;

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isAutoCount() {
		return autoCount;
	}

	public void setAutoCount(boolean autoCount) {
		this.autoCount = autoCount;
	}

	public boolean isAutoReturnFirstPage() {
		return autoReturnFirstPage;
	}

	public void setAutoReturnFirstPage(boolean autoReturnFirstPage) {
		this.autoReturnFirstPage = autoReturnFirstPage;
	}

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
    
    
    
}
