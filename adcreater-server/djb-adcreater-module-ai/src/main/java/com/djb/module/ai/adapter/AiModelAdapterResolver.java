package com.djb.module.ai.adapter;

import java.util.Map;
import java.util.Optional;

public final class AiModelAdapterResolver {

    private AiModelAdapterResolver() {
    }

    public static Optional<AiModelAdapter> resolve(Map<String, AiModelAdapter> adapters, String adapterClass) {
        return adapters.values().stream()
                .filter(adapter -> matches(adapter, adapterClass))
                .findFirst();
    }

    public static boolean matches(AiModelAdapter adapter, String adapterClass) {
        if (adapterClass == null || adapterClass.isBlank()) {
            return false;
        }
        Class<?> adapterType = adapter.getClass();
        String requestedSimpleName = extractSimpleName(adapterClass);
        return adapterType.getName().equals(adapterClass)
                || adapterType.getSimpleName().equals(requestedSimpleName);
    }

    private static String extractSimpleName(String adapterClass) {
        int index = adapterClass.lastIndexOf('.');
        return index >= 0 ? adapterClass.substring(index + 1) : adapterClass;
    }
}
