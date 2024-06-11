package mc.gouv.xaf.apiclient2tiers.authentication.impl;


import mc.gouv.xaf.apiclient2tiers.authentication.AuthorizationHeaderProvider;

public class JwtAuthorizationHeaderProvider implements AuthorizationHeaderProvider {

    private String jwtToken;

    public JwtAuthorizationHeaderProvider(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public String getHeaderValue() {
        return "Bearer " + jwtToken;

    }

}
