package com.aokayun.api.beans;

public class ApiDataBean extends BaseBean {
	private boolean run;
	private String desc ; // 接口描述
	private String url  ;
	private String method;
	private String param;
	private boolean contains;
	private int status;
	private String verify;
	private String save;
	private String preParam;
	private int sleep;
	private  String type;
	private  String check_sql;
/*
新增的数据库校验字段
check_sql
 */
	public String getCheck_sql() { return check_sql; }

	public void setCheck_sql(String check_sql) { this.check_sql = check_sql; }

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) { this.run = run; }

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getType() { return type; }

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public boolean isContains() {
		return contains;
	}

	public void setContains(boolean contains) {
		this.contains = contains;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getVerify() {
		return verify;
	}

	public void setVerify(String verify) {
		this.verify = verify;
	}

	public String getSave() {
		return save;
	}

	public void setSave(String save) {
		this.save = save;
	}

	public String getPreParam() {
		return preParam;
	}

	public void setPreParam(String preParam) {
		this.preParam = preParam;
	}

	public int getSleep() {
		return sleep;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}




	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("接口描述:%s,请求方式:%s,请求地址:%s,参数:%s", this.desc,
				this.method, this.url, this.param);
	}

}
