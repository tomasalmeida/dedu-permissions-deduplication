package com.tomasalmeida.dedu.api.kafka;

import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class ResourceCache {

    private final Cache<String, ResourceTypeMarkContainer> resourceExistsCache;
    private final Cache<String, ResourceTypeMarkContainer> resourceDroppedCache;

    public ResourceCache() {
        resourceExistsCache = buildCache();
        resourceDroppedCache = buildCache();
    }

    @NotNull
    private static Cache<String, ResourceTypeMarkContainer> buildCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    /**
     * Verify if a given resource was already searched
     *
     * @param resourceName name of the resource
     * @param resourceType type of the resource
     * @return true if the resource exists, false if does not exist, null if there is no results cached
     */
    @Nullable
    public Boolean isResourceResultCached(@NotNull final String resourceName, @NotNull final ResourceType resourceType) {
        if (isResourceMarked(resourceExistsCache, resourceName, resourceType)) {
            return true;
        }
        if (isResourceMarked(resourceDroppedCache, resourceName, resourceType)) {
            return false;
        }
        return null;
    }

    public void cacheResourceExists(@NotNull final String resourceName, @NotNull final ResourceType resourceType, final boolean exists) {
        if (exists) {
            resourceExistsCache.get(resourceName, unused -> new ResourceTypeMarkContainer()).markResourceType(resourceType);
        } else {
            resourceDroppedCache.get(resourceName, unused -> new ResourceTypeMarkContainer()).markResourceType(resourceType);
        }
    }

    private boolean isResourceMarked(@NotNull final Cache<String, ResourceTypeMarkContainer> cache,
                                     @NotNull final String resourceName,
                                     @NotNull final ResourceType resourceType) {
        final ResourceTypeMarkContainer resourceTypeFoundMark = cache.getIfPresent(resourceName);
        return resourceTypeFoundMark != null && resourceTypeFoundMark.isResourceTypeMarked(resourceType);
    }

    private static class ResourceTypeMarkContainer {

        private final boolean[] resourceTypeMarker = new boolean[ResourceType.values().length];

        public void markResourceType(@NotNull final ResourceType resourceType) {
            resourceTypeMarker[resourceType.code()] = true;
        }

        public boolean isResourceTypeMarked(@NotNull final ResourceType resourceType) {
            return resourceTypeMarker[resourceType.code()];
        }
    }
}
