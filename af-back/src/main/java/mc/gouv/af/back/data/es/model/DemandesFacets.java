package mc.gouv.af.back.data.es.model;

import java.util.ArrayList;
import java.util.List;

public class DemandesFacets {

    private List<DemandesFacet> facets = new ArrayList<>();

    public void add(DemandesFacet facet) {
        facets.add(facet);
    }

    public List<DemandesFacet> getFacets() {
        return facets;
    }

    public void setFacets(List<DemandesFacet> facets) {
        this.facets = facets;
    }

    @Override
    public String toString() {
        return "DemandesFacets [facets=" + facets + "]";
    }

}
