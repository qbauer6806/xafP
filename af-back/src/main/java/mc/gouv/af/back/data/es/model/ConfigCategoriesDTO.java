package mc.gouv.af.back.data.es.model;

import java.util.ArrayList;
import java.util.List;

public class ConfigCategoriesDTO {

    private List<EsCategory> categories = new ArrayList<>();

    public List<EsCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<EsCategory> categories) {
        this.categories = categories;
    }

}
