package mc.gouv.xaf.back.config.es;

import java.io.File;
import java.util.Arrays;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.Netty4Plugin;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;

@Profile("simulateur")
@Conditional(IndexationEnabledCondition.class)
@Service
public class NodeBuilder {

    public Node getNode(String esNodeName, int esPort) {
        Builder builder = Settings.builder();

        File tempDir = Files.createTempDir();
        builder.put("path.home", tempDir.getAbsolutePath());
        builder.put("node.name", esNodeName);
        builder.put("path.data", "");
        builder.put("http.port", esPort);
        builder.put("http.enabled", "true");
        builder.put("http.type", "netty4");
        builder.put("transport.type", "local");

        Settings settings = builder.build();
        return new EsNode(settings, Arrays.asList(Netty4Plugin.class));
    }

}
