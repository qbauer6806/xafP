package mc.gouv.xaf.back.properties;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * Permet d'indiquer au GouvPropertiesResolver d'ignorer la propriété si l'annotation est présente mais que
 * mc.gouv.af.back.codeappli.archivage.enabled=false
 * 
 * @author mpavone.ext
 *
 */
@Target(value={java.lang.annotation.ElementType.METHOD})
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface GouvArchivageProperty {

}
