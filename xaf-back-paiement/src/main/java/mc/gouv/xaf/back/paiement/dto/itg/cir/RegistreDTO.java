package mc.gouv.xaf.back.paiement.dto.itg.cir;

import java.util.List;

public class RegistreDTO {
	
	private List<VehiculeDTO> vehicules;

	public List<VehiculeDTO> getVehicules() {
		return vehicules;
	}

	public void setVehicules(List<VehiculeDTO> vehicules) {
		this.vehicules = vehicules;
	}
}
