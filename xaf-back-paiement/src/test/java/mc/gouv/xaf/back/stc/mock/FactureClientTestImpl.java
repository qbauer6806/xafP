package mc.gouv.xaf.back.stc.mock;

import mc.gouv.xaf.back.stc.client.FactureClient;

import java.io.IOException;
import java.io.InputStream;

public class FactureClientTestImpl implements FactureClient {
    @Override
    public String check(String numFacture) {
        return null;
    }

    @Override
    public String createFacture(String numPermis, String numImmat, double montant, String codeTransaction, Integer usagerId) {
        return "facture001";
    }

    @Override
    public InputStream getFacture(String numFacture) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }
}
