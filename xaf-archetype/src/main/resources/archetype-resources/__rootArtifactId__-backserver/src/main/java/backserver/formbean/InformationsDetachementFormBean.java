#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.backserver.formbean;

public class InformationsDetachementFormBean {

    private Integer nombreSalariesDetaches;

    private Integer nombreSalariesNonDetaches;

    public Integer getNombreSalariesDetaches() {
        return nombreSalariesDetaches;
    }

    public void setNombreSalariesDetaches(Integer nombreSalariesDetaches) {
        this.nombreSalariesDetaches = nombreSalariesDetaches;
    }

    public Integer getNombreSalariesNonDetaches() {
        return nombreSalariesNonDetaches;
    }

    public void setNombreSalariesNonDetaches(Integer nombreSalariesNonDetaches) {
        this.nombreSalariesNonDetaches = nombreSalariesNonDetaches;
    }
}
