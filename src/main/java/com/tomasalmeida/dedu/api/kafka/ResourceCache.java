package com.tomasalmeida.dedu.api.kafka;

import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class ResourceCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminClient.class);

    private final Cache<String, ResourceTypeMarkContainer> resourceExistsCache;

    public ResourceCache() {
        resourceExistsCache = Caffeine.newBuilder()
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
        final ResourceTypeMarkContainer resourceTypeFoundMark = resourceExistsCache.getIfPresent(resourceName);
        return resourceTypeFoundMark != null ? resourceTypeFoundMark.isResourceTypeMarked(resourceType) : null;
    }

    public void cacheResourceExists(@NotNull final String resourceName, @NotNull final ResourceType resourceType, final boolean exists) {
        resourceExistsCache.get(resourceName, unused -> new ResourceTypeMarkContainer()).markResourceType(resourceType, exists);
    }

    private static class ResourceTypeMarkContainer {
        private static final int RESOURCE_TYPE_LENGTH = ResourceType.values().length;

        private final Boolean[] resourceTypeMarker = new Boolean[RESOURCE_TYPE_LENGTH];

        public void markResourceType(@NotNull final ResourceType resourceType, final boolean exists) {
            resourceTypeMarker[resourceType.code()] = exists;
        }

        public boolean isResourceTypeMarked(@NotNull final ResourceType resourceType) {
            return resourceTypeMarker[resourceType.code()];
        }
    }
}
