package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class DonneesExternesDTO {

    protected DonneesMConnectDTO mconnect;

    public DonneesMConnectDTO getMconnect() {
        return mconnect;
    }

    public void setMconnect(DonneesMConnectDTO mconnect) {
        this.mconnect = mconnect;
    }

}
