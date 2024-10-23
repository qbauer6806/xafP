package mc.gouv.xaf.caching;

/**
 * Donnée de test
 *
 * @author qdeme
 */
public class GouvCacheData {

    private Integer id;

    private String texte;

    public GouvCacheData(Integer id, String texte) {
        this.id = id;
        this.texte = texte;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((texte == null) ? 0 : texte.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GouvCacheData other = (GouvCacheData) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (texte == null) {
            if (other.texte != null) {
                return false;
            }
        } else if (!texte.equals(other.texte)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GouvCacheData [id=" + id + ", texte=" + texte + "]";
    }

}
