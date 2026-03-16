package mc.gouv.xaf.shared.dto;

public record SearchFilterDefinition(
        String key,
        String label,
        FilterType type
) {
}
