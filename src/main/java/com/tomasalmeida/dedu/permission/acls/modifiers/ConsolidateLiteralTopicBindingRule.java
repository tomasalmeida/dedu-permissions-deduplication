package com.tomasalmeida.dedu.permission.acls.modifiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrenttrees.common.Iterables;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.tomasalmeida.dedu.api.kafka.KafkaAdminClient;
import com.tomasalmeida.dedu.configuration.MainConfiguration;
import com.tomasalmeida.dedu.permission.acls.AclPermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.ActionablePermissionBinding;
import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;
import com.tomasalmeida.dedu.permission.context.CandidatesGroup;
import com.tomasalmeida.dedu.permission.context.ContextExecution;
import com.tomasalmeida.dedu.permission.modifier.BindingTransformationRule;

public class ConsolidateLiteralTopicBindingRule implements BindingTransformationRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolidateLiteralTopicBindingRule.class);
    private static final String CONFIG_RULE = "rule.consolidate.literal.topic.bindings";

    @VisibleForTesting
    static final String CONFIG_RULE_ENABLED = CONFIG_RULE + ".enabled";
    @VisibleForTesting
    static final String CONFIG_RULE_MIN_PREFIX = CONFIG_RULE + ".prefix.min";
    @VisibleForTesting
    static final String CONFIG_RULE_MIN_REPLACED = CONFIG_RULE + ".replaced.min";

    private final KafkaAdminClient adminClient;
    private final boolean isRuleActivated;
    private final int minPrefixSize;
    private final int minReplacedBindings;

    public ConsolidateLiteralTopicBindingRule(@NotNull final KafkaAdminClient adminClient, @NotNull final MainConfiguration mainConfiguration) {
        this.adminClient = adminClient;
        this.isRuleActivated = Boolean.parseBoolean(mainConfiguration.getDeduPropertyOrDefault(CONFIG_RULE_ENABLED, "false"));
        this.minPrefixSize = getPropertyValueOrMin(mainConfiguration, CONFIG_RULE_MIN_PREFIX, 3);
        this.minReplacedBindings = getPropertyValueOrMin(mainConfiguration, CONFIG_RULE_MIN_REPLACED, 2);
    }

    private int getPropertyValueOrMin(@NotNull final MainConfiguration mainConfiguration,
                                      @NotNull final String property,
                                      final int minValue) {
        final int value = Integer.parseInt(mainConfiguration.getDeduPropertyOrDefault(property, "0"));
        return Integer.max(minValue, value);
    }

    @Override
    public void run(final @NotNull ContextExecution contextExecution) {
        if (!isRuleActivated) {
            return;
        }
        contextExecution.getCandidatesGroups()
                .stream()
                .filter(candidatesGroup -> candidatesGroup.getMaster().getResourceType().equals(ResourceType.TOPIC))
                .forEach(candidatesGroup ->
                        consolidateTopicBindings(candidatesGroup, contextExecution.getActionablePermissionBindings()));
    }

    private void consolidateTopicBindings(@NotNull final CandidatesGroup candidatesGroup,
                                          @NotNull final List<ActionablePermissionBinding> actionablePermissionBindings) {
        candidatesGroup.sortLiteralBindingsByResourceName();
        final Map<String, Set<PermissionBinding>> prefixedTopicsMap = createGroupsByPrefix(candidatesGroup);
        cleanCandidateGroup(prefixedTopicsMap, candidatesGroup.getLiteralBindings());
        createActionablePermissions(prefixedTopicsMap, actionablePermissionBindings);
    }

    private void cleanCandidateGroup(@NotNull final Map<String, Set<PermissionBinding>> prefixedTopicsMap,
                                     @NotNull final List<PermissionBinding> literalBindings) {
        prefixedTopicsMap
                .values()
                .stream()
                .flatMap(Set::stream)
                .forEach(literalBindings::remove);
    }

    private void createActionablePermissions(@NotNull final Map<String, Set<PermissionBinding>> prefixedTopicsMap,
                                             @NotNull final List<ActionablePermissionBinding> actionablePermissionBindings) {
        for (final Map.Entry<String, Set<PermissionBinding>> entry : prefixedTopicsMap.entrySet()) {
            final String prefixPattern = entry.getKey();
            final Set<PermissionBinding> permissionBindings = entry.getValue();

            for (final PermissionBinding deletablePermission : permissionBindings) {
                final ActionablePermissionBinding actionableForDeletion = createActionableForDeletion(prefixPattern, deletablePermission);
                actionablePermissionBindings.add(actionableForDeletion);
            }
            createNewPatternPermission(actionablePermissionBindings, permissionBindings, prefixPattern);
        }
    }

    private void createNewPatternPermission(@NotNull final List<ActionablePermissionBinding> actionablePermissionBindings,
                                            @NotNull final Set<PermissionBinding> permissionBindings,
                                            @NotNull final String prefixPattern) {
        permissionBindings
                .stream()
                .findAny()
                .map(exampleBinding -> createActionableForPattern(prefixPattern, exampleBinding))
                .ifPresent(actionablePermissionBindings::add);
    }

    private ActionablePermissionBinding createActionableForPattern(@NotNull final String prefixPattern,
                                                                   @NotNull final PermissionBinding exampleBinding) {
        final PermissionBinding prefixPermission = new AclPermissionBinding(
                exampleBinding.getResourceType(),
                prefixPattern,
                PatternType.PREFIXED,
                exampleBinding.getHost(),
                exampleBinding.getOperation(),
                exampleBinding.getPrincipal(),
                exampleBinding.getPermissionType()
        );
        return new ActionablePermissionBinding(prefixPermission,
                ActionablePermissionBinding.Action.ADD,
                "New prefix " + prefixPattern);
    }

    @NotNull
    private ActionablePermissionBinding createActionableForDeletion(@NotNull final String prefixPattern,
                                                                    @NotNull final PermissionBinding deletablePermission) {
        return new ActionablePermissionBinding(deletablePermission,
                ActionablePermissionBinding.Action.DELETE,
                "Replaced by pattern " + prefixPattern);
    }

    @NotNull
    private Map<String, Set<PermissionBinding>> createGroupsByPrefix(@NotNull final CandidatesGroup candidatesGroup) {

        final Map<String, RadixTree<PermissionBinding>> treesByHost = new HashMap<>();

        // put all elements in the tree
        for (final PermissionBinding binding : candidatesGroup.getLiteralBindings()) {
            LOGGER.debug("Adding binding [{}] to radix tree of host [{}]", binding, binding.getHost());
            final RadixTree<PermissionBinding> tree = treesByHost.getOrDefault(binding.getHost(), new ConcurrentRadixTree<>(new DefaultCharArrayNodeFactory()));
            tree.put(binding.getResourceName(), binding);
            treesByHost.put(binding.getHost(), tree);
        }

        // create a map with a candidate prefix and replaceable queries
        final Map<String, Set<PermissionBinding>> prefixedTopicsMap = new HashMap<>();
        for (final PermissionBinding binding : candidatesGroup.getLiteralBindings()) {
            final String topic = binding.getResourceName();
            final RadixTree<PermissionBinding> tree = treesByHost.get(binding.getHost());

            final String topicPrefix = getCandidatePattern(topic, tree);
            if (topicPrefix != null) {
                removeLargerPrefixFromCandidates(topicPrefix, prefixedTopicsMap);
                if (isShorterPrefixNotFound(topicPrefix, prefixedTopicsMap)) {
                    prefixedTopicsMap.put(topicPrefix, Iterables.toSet(tree.getValuesForKeysStartingWith(topicPrefix)));
                }
            }
        }
        return prefixedTopicsMap;
    }

    private boolean isShorterPrefixNotFound(@NotNull final String topicPrefix,
                                            @NotNull final Map<String, Set<PermissionBinding>> prefixedTopicsMap) {
        return prefixedTopicsMap
                .keySet()
                .stream()
                .noneMatch(topicPrefix::startsWith);
    }

    private void removeLargerPrefixFromCandidates(@NotNull final String topicPrefix,
                                                  @NotNull final Map<String, Set<PermissionBinding>> prefixedTopicsMap) {
        prefixedTopicsMap
                .keySet()
                .stream()
                .filter(keyPrefix -> keyPrefix.startsWith(topicPrefix))
                .findFirst()
                .ifPresent(prefixedTopicsMap::remove);
    }

    @Nullable
    private String getCandidatePattern(final String topicName, final RadixTree<PermissionBinding> tree) {
        for (int size = topicName.length(); size >= minPrefixSize; size--) {
            final String topicPrefix = topicName.substring(0, size);
            final int replaceableCount = Iterables.count(tree.getKeyValuePairsForKeysStartingWith(topicPrefix));
            if (replaceableCount > minReplacedBindings) {
                final long matches = adminClient.countMatches(topicPrefix);
                if (matches == replaceableCount) {
                    //topic prefix is a valid candidate (does not give more permissions than the current ones)
                    return topicPrefix;
                }
            }
        }
        return null;
    }
}
