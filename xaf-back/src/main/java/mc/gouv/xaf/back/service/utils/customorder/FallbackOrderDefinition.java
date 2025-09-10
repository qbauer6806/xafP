package mc.gouv.xaf.back.service.utils.customorder;

import java.util.List;
import java.util.stream.Stream;

/**
 * Allow to select another order when default order is null switch to secondary ones
 *
 * @param jsonDefaultOrder
 * @param jsonFallbackOrders
 *         ex: jsonDefaultOrder : contenu.x.y ex: jsonFallbackOrders : { contenu.x.w, contenu.x.z }
 */
public record FallbackOrderDefinition(String jsonDefaultOrder, List<String> jsonFallbackOrders) {

    public List<String> getAllProperties() {
        return Stream.concat(Stream.of(jsonDefaultOrder), jsonFallbackOrders.stream()).toList();
    }

}
