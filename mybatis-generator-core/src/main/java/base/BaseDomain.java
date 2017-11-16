package base;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
@JsonInclude(Include.NON_NULL)
public class BaseDomain implements Serializable {

	private static final long serialVersionUID = -3068228184404269814L;
	@JsonIgnore
	private Integer pageNumber; // 当前页
	@JsonIgnore
	private Integer pageSize; // 每页的数量	
	@JsonIgnore
	private String sortName; // 排序字段
	@JsonIgnore
	private String sortOrder; // 排序方式desc,asc
	@JsonIgnore
	private String ids; // ID的字符串集合，逗号分隔
	@JsonIgnore
	private Date beginTime;
	@JsonIgnore
	private Date endTime;
	private Map<String, Object> map;
	private String token;

	public Map<String, Object> getMap() {
		if (map == null) {
			map = new HashMap<String, Object>();
		}
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	public Integer getPageNumber() {
		if (this.pageNumber == null || this.pageNumber <= 1) {
			this.pageNumber = 1;
		}
		return pageNumber;
	}

	public Integer getPageSize() {
		if (this.pageSize == null || this.pageSize <= 1) {
			this.pageSize = 10;
		}
		if (this.pageSize > 1000) {
			this.pageSize = 1000;
		}
		return pageSize;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
