package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.UsagersCourrierBO;

/**
 * 
 * @author qdeme
 *
 */
public interface UsagersCourrierRepository extends CrudRepository<UsagersCourrierBO, Integer> {
    
    public List<UsagersCourrierBO> findByDemarcheId(String demarcheId);
    
    public UsagersCourrierBO findByDemarcheIdAndPkUsagersCourrier(String demarcheId, Integer pkUsagersCourrier);
    
}
