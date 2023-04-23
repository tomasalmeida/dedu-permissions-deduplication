package com.tomasalmeida.dedu.permission.context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

import org.apache.kafka.common.resource.PatternType;
import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.permission.bindings.PermissionBinding;

import lombok.Getter;

@Getter (onMethod_ = {@NotNull})
public class CandidatesGroup {

    private final PermissionBinding master;
    final List<PermissionBinding> literalBindings = new ArrayList<>();
    final List<PermissionBinding> prefixBindings = new ArrayList<>();

    public CandidatesGroup(@NotNull final PermissionBinding binding) {
        this.master = binding;
        segregateByPatternType(binding);
    }

    private void segregateByPatternType(@NotNull final PermissionBinding binding) {
        if (PatternType.LITERAL.equals(binding.getPatternType())) {
            literalBindings.add(binding);
        } else if (PatternType.PREFIXED.equals(binding.getPatternType())) {
            prefixBindings.add(binding);
        } else {
            throw new IllegalArgumentException("Binding should be Prefixed or Literal");
        }
    }

    public boolean addIfMatches(@NotNull final PermissionBinding binding) {
        final boolean itsMatch = matchesMaster(binding);
        if (itsMatch) {
            segregateByPatternType(binding);
        }
        return itsMatch;
    }

    public void sortPrefixBindingByLength() {
        final ToIntFunction<PermissionBinding> extractResourceLength = permissionBinding -> permissionBinding.getResourceName().length();
        final Comparator<PermissionBinding> comparator = Comparator.comparingInt(extractResourceLength);
        prefixBindings.sort(comparator);
    }

    public void sortLiteralBindingsByResourceName() {
        literalBindings.sort(Comparator.comparing(PermissionBinding::getResourceName) );
    }

    private boolean matchesMaster(@NotNull final  PermissionBinding binding) {
        return master.getResourceType().equals(binding.getResourceType())
                && master.getOperation().equals(binding.getOperation())
                && master.getPrincipal().equals(binding.getPrincipal())
                && master.getPermissionType().equals(binding.getPermissionType());
    }
}
