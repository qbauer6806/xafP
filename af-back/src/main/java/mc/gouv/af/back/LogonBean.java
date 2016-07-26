package mc.gouv.af.back;

import javax.servlet.http.HttpServletRequest;

public class LogonBean {

	public final static String GOUV_SESSION_REQUEST_PARAM= "_KSESS";
	public final static String GOUV_APP_ROOT_REQUEST_PARAM= "_RAC";
	public final static String GOUV_APP_ID_REQUEST_PARAM= "app";
	

	private String sessionId;
	private String appRoot;
	private String appId;
	
	
	
	public LogonBean(HttpServletRequest httpRequest) {
		super();
		String sessionId = httpRequest.getParameter(LogonBean.GOUV_SESSION_REQUEST_PARAM);
		String appRoot = httpRequest.getParameter(LogonBean.GOUV_APP_ROOT_REQUEST_PARAM);
		String appId = httpRequest.getParameter(LogonBean.GOUV_APP_ID_REQUEST_PARAM);
		this.sessionId = sessionId;
		this.appRoot = appRoot;
		this.appId = appId;
	}
	
	public LogonBean(String sessionId, String appRoot, String appId) {
		super();
		this.sessionId = sessionId;
		this.appRoot = appRoot;
		this.appId = appId;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	

	
	public String getAppRoot() {
		return appRoot;
	}
	public void setAppRoot(String appRoot) {
		this.appRoot = appRoot;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getQueryString(){
		return GOUV_SESSION_REQUEST_PARAM+"="+sessionId+"&"+GOUV_APP_ROOT_REQUEST_PARAM+"="+appRoot+"&"+GOUV_APP_ID_REQUEST_PARAM+"="+appId;
	}
	
}
