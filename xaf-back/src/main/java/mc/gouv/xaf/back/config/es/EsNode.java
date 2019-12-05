package mc.gouv.xaf.back.config.es;

import java.util.Collection;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;

public class EsNode extends Node {

    EsNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
        super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins, false);
    }

    public EsNode(Settings preparedSettings) {
        super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null));
    }

    @Override
    protected void registerDerivedNodeNameWithLogger(String s) {
    }
}
