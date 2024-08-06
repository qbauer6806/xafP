package mc.gouv.xaf.backweb.web.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogonBean {

    public static final String GOUV_SESSION_REQUEST_PARAM = "_KSESS";
    public static final String GOUV_APP_ROOT_REQUEST_PARAM = "_RAC";
    public static final String GOUV_APP_ID_REQUEST_PARAM = "app";

    private String sessionId;
    private String appRoot;
    private String appId;

    public LogonBean(HttpServletRequest httpRequest) {
        super();
        this.sessionId = httpRequest.getParameter(LogonBean.GOUV_SESSION_REQUEST_PARAM);
        this.appRoot = httpRequest.getParameter(LogonBean.GOUV_APP_ROOT_REQUEST_PARAM);
        this.appId = httpRequest.getParameter(LogonBean.GOUV_APP_ID_REQUEST_PARAM);
    }

    public LogonBean(String sessionId, String appRoot, String appId) {
        super();
        this.sessionId = sessionId;
        this.appRoot = appRoot;
        this.appId = appId;
    }

    public String getQueryString() {
        return GOUV_SESSION_REQUEST_PARAM + "=" + sessionId + "&" + GOUV_APP_ROOT_REQUEST_PARAM + "=" + appRoot + "&"
                + GOUV_APP_ID_REQUEST_PARAM + "=" + appId;
    }

}
