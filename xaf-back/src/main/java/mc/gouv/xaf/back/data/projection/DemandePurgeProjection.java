package mc.gouv.xaf.back.data.projection;

import java.util.Date;

// TODO Proposer une alternative unique pour généraliser les projections ou d'enlever le config / contenuInitial des BO
public record DemandePurgeProjection(Integer pkDemandes, String identifiant, Date dateCreation, String usagerPrenom,
                                     String usagerNom, String statutLibelle, String buildId) {
}
