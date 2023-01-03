package mc.gouv.xaf.back.config.es;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class EsNode extends Node {

    private static final Supplier<String> supplier = new Supplier<String>() {
        @Value("${application.name}")
        private String indexAlias;

        @Override
        public String get() {
            return indexAlias;
        }
    };

    public EsNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
        super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, Collections.emptyMap(), null, supplier), classpathPlugins, false);
    }

    public EsNode(Settings preparedSettings) {
        super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, Collections.emptyMap(), null, supplier));
    }

}
