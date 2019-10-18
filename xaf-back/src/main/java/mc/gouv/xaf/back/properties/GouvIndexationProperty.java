package mc.gouv.xaf.back.properties;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(value={java.lang.annotation.ElementType.METHOD})
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface GouvIndexationProperty {

}
