package mc.gouv.xaf.back.service.itg.nomen;

import mc.gouv.xaf.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.client.ApiClient;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenNomenclatureDTO;
import org.springframework.stereotype.Component;

@Component
public class NomenClient extends ApiClient {

    public static final String NOMEN_PATH = "nomenclatures";
    public static final String VALEUR_PATH = "/valeurs";

    public NomenClient(GouvPropertiesResolver resolver) {
        super(resolver.getNomenUrl(), new JwtAuthorizationHeaderProvider(resolver.getNomenJwt()));
    }

    public NomenNomenclatureDTO getNomenclature(String identifiant) {
        return getRestClient().get()
                .uri("/" + NOMEN_PATH + "/{identifiant}" + VALEUR_PATH, identifiant)
                .retrieve()
                .body(NomenNomenclatureDTO.class);
    }

    public NomenNomenclatureDTO getNomenclatureAvecLocale(String identifiant, String locale) {
        return getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + NOMEN_PATH + "/{identifiant}" + VALEUR_PATH)
                        .queryParam("valeurLangue", locale)
                        .build(identifiant))
                .retrieve()
                .body(NomenNomenclatureDTO.class);
    }

    public NomenNomenclatureDTO getNomenclatureValeur(String identifiant, String valeur) {
        return getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + NOMEN_PATH + "/{identifiant}" + VALEUR_PATH)
                        .queryParam("valeurCode", valeur)
                        .build(identifiant))
                .retrieve()
                .body(NomenNomenclatureDTO.class);
    }

    public NomenNomenclatureDTO getNomenclatureValeurAvecLocale(
            String identifiant,
            String valeur,
            String locale) {

        return getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + NOMEN_PATH + "/{identifiant}" + VALEUR_PATH)
                        .queryParam("valeurCode", valeur)
                        .queryParam("valeurLangue", locale)
                        .build(identifiant))
                .retrieve()
                .body(NomenNomenclatureDTO.class);
    }
}
