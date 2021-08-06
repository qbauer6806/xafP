package mc.gouv.xaf.shared.dto;

import org.junit.Test;

import static org.junit.Assert.*;

public class StatutPublicOuInterneDTOTest {

    @Test
    public void toStringTest() {
        StatutPublicOuInterneDTO statut = new StatutPublicOuInterneDTO("TEST", "test");
        String result = statut.toString();
        assertEquals("test", result);
    }

    @Test
    public void equalsTest() {
        StatutPublicOuInterneDTO statut = new StatutPublicOuInterneDTO("TEST", "test");
        assertNotEquals(null, statut);
        DemandeStatutDTO demStatut = new DemandeStatutDTO();
        assertNotEquals(statut, demStatut);
        assertEquals(statut, statut);
        StatutPublicOuInterneDTO statutDeux = new StatutPublicOuInterneDTO("TEST", "test");
        assertEquals(statut,statutDeux);
        statutDeux = new StatutPublicOuInterneDTO("DEUX", "deux");
        assertNotEquals(statut, statutDeux);
        statutDeux = new StatutPublicOuInterneDTO("TEST", "deux");
        assertNotEquals(statut, statutDeux);
        statutDeux = new StatutPublicOuInterneDTO("DEUX", "test");
        assertNotEquals(statut, statutDeux);
    }

    @Test
    public void hashCodeTest() {
        StatutPublicOuInterneDTO statut = new StatutPublicOuInterneDTO("TEST", "test");
        assertEquals(statut.hashCode(), statut.hashCode());
        StatutPublicOuInterneDTO statutDeux = new StatutPublicOuInterneDTO("TEST", "test");
        assertEquals(statut.hashCode(), statutDeux.hashCode());
        statutDeux = new StatutPublicOuInterneDTO("DEUX", "deux");
        assertNotEquals(statut.hashCode(), statutDeux.hashCode());
        statutDeux = new StatutPublicOuInterneDTO("TEST", "deux");
        assertNotEquals(statut.hashCode(), statutDeux.hashCode());
        statutDeux = new StatutPublicOuInterneDTO("DEUX", "test");
        assertNotEquals(statut.hashCode(), statutDeux.hashCode());
    }
}
