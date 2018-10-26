package mc.gouv.af.back.config.es;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

@Named
public class PropertiesResolver {

    @Value("${application.name}")
    private String indexAlias;

    public String getIndexAlias() {
        return indexAlias;
    }

    public void setIndexAlias(String indexAlias) {
        this.indexAlias = indexAlias;
    }

}
