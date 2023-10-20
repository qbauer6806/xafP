package mc.gouv.xaf.servlet.dto;

public class CustomRequestRechercheDTO {

    private String action;
    private CustomRequestRechercheDataDTO data;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public CustomRequestRechercheDataDTO getData() {
        return data;
    }

    public void setData(CustomRequestRechercheDataDTO data) {
        this.data = data;
    }
}
