package mc.gouv.xaf.back.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchProperties {

    @Value("${mc.gouv.${application.name}.shared.backapi.indexing.enabled}")
    private String indexingEnabled;

    @Value("${mc.gouv.${application.name}.shared.backapi.elasticsearch.reindex.bulksize}")
    private String bulksize;

    @Value("${mc.gouv.${application.name}.shared.backapi.elasticsearch.clusterHosts}")
    private String clusterHosts;

    @Value("${mc.gouv.${application.name}.shared.backapi.elasticsearch.port}")
    private String port;

    @Value("${mc.gouv.${application.name}.shared.backapi.elasticsearch.password}")
    private String password;

    @Value("${mc.gouv.${application.name}.shared.backapi.elasticsearch.user}")
    private String user;

    @Value("${mc.gouv.${application.name}.shared.backapi.elasticsearch.connectTimeout:30000}")
    private String connectTimeout;

    @Value("${mc.gouv.${application.name}.shared.backapi.elasticsearch.socketTimeout:30000}")
    private String socketTimeout;

    @Value("${mc.gouv.${application.name}.backserver.search.highlight.pretags:<strong style=\"color:red\">}")
    private String pretags;

    @Value("${mc.gouv.${application.name}.backserver.search.highlight.posttags:</strong>}")
    private String posttags;

    public String getIndexingEnabled() {
        return indexingEnabled;
    }

    public void setIndexingEnabled(String indexingEnabled) {
        this.indexingEnabled = indexingEnabled;
    }

    public String getBulksize() {
        return bulksize;
    }

    public void setBulksize(String bulksize) {
        this.bulksize = bulksize;
    }

    public String getClusterHosts() {
        return clusterHosts;
    }

    public void setClusterHosts(String clusterHosts) {
        this.clusterHosts = clusterHosts;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPretags() {
        return pretags;
    }

    public void setPretags(String pretags) {
        this.pretags = pretags;
    }

    public String getPosttags() {
        return posttags;
    }

    public void setPosttags(String posttags) {
        this.posttags = posttags;
    }

    public String getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(String socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
}
