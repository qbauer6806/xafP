package mc.gouv.xaf.back.service;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * Interface permettant de définir des CustomRequests dans la démarche, c'est à dire des endpoints dont l'implémentation est définissable à la volée
 * dans le téléservice.
 * 
 * @author qdeme
 */
public interface CustomRequestService {

    ResponseEntity getCustomRequest(HttpServletRequest request, Integer usagerId);
    
    ResponseEntity postCustomRequest(HttpServletRequest request, Integer usagerId);

    ResponseEntity putCustomRequest(HttpServletRequest request, Integer usagerId);

    ResponseEntity deleteCustomRequest(HttpServletRequest request, Integer usagerId);
    
}
