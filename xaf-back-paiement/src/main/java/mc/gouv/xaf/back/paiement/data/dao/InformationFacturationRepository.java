package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.InformationFacturationBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InformationFacturationRepository extends JpaRepository<InformationFacturationBO, String> {

    InformationFacturationBO findByCommande_PkCommandes(Integer commandId);

    /*
     * A partir du moyen de paiement faire une jointure entre pmnt_moyens_paiements, pmnt_commandes, pmnt_commandes_demandes, dem_demandes et dem_access pour récupérer l'usager id
     *
     */
    /*TODO a voir si utils @Query("select da.usagerId from AccessBO da "+
            "inner join DemandeBO dd on da.pkAccess = dd.fkAccess " +
            "inner join CommandeDemandeBO cd on cd.demande = dd.pkDemandes " +
            "inner join CommandeBO com on com.pkCommandes = cd.commande " +
            "inner join MoyenPaiementBO mp on mp.commande = com.pkCommandes " +
            "where mp.pkMoyensPaiements = :moyenPaiementId")
    Integer findUsagerIdByPkMoyenPaiement(String moyenPaiementId);*/

}
