package mc.gouv.xaf.back.service.itg.gichuni.api;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

/**
 * 
 * Classe de configuration pour l'appel à l'API GICHUNI avec authentification OIDC
 * 
 * @author qdeme
 * 
 */
@Configuration
public class GichuniApiClientConfig {
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;
	
	@Bean
	public OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails() {
	    ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
	    resourceDetails.setAccessTokenUri(gouvPropertiesResolver.getGichkeyUrl() + "/protocol/openid-connect/token");
	    resourceDetails.setClientId(gouvPropertiesResolver.getGichkeyClientId());
	    resourceDetails.setClientSecret(gouvPropertiesResolver.getGichkeyClientSecret());
	    resourceDetails.setGrantType("client_credentials");
	    resourceDetails.setScope(Collections.singletonList("openid"));
	    return resourceDetails;
	}

	@Bean
	public OAuth2ClientContext oauth2ClientContext() {
	    DefaultOAuth2ClientContext defaultOAuth2ClientContext = new DefaultOAuth2ClientContext();
	    return defaultOAuth2ClientContext;
	}

	@Bean
	public OAuth2RestTemplate oAuth2RestTemplate(OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails, OAuth2ClientContext oauth2ClientContext) {
	    OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(oAuth2ProtectedResourceDetails, oauth2ClientContext);
	    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

	    restTemplate.setRequestFactory(factory);
	    return restTemplate;
	}
	
}
