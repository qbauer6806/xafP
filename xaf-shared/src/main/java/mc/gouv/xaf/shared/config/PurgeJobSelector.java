package mc.gouv.xaf.shared.config;

import mc.gouv.xaf.shared.annotations.TypeDePurge;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.quartz.Job;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PurgeJobSelector {

    private final Map<String, Class<? extends Job>> jobClasses = new HashMap<>();

    public PurgeJobSelector(List<Job> jobs) {
        for (Job job : jobs) {
            // Récupérer la vraie classe derrière le proxy
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(job);

            // Récupérer l'annotation en tenant compte de l'héritage
            TypeDePurge annotation = AnnotationUtils.findAnnotation(targetClass, TypeDePurge.class);

            if (annotation != null) {
                jobClasses.put(annotation.value(), targetClass.asSubclass(Job.class));
            }
        }
    }

    public Class<? extends Job> getJobClass(boolean purgePaiement) {
        return jobClasses.get(purgePaiement ? "paiement" : "default");
    }
}
