package com.tomasalmeida.dedu.permission.bindings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

@Getter
public class ActionablePermissionBinding extends PermissionBinding {

    public enum Action {DELETE, NONE}

    private final Action action;
    private final String note;

    public ActionablePermissionBinding(@NotNull final PermissionBinding permission,
                                       @NotNull final Action action,
                                       @Nullable final String note) {
        super(permission);
        this.action = action;
        this.note = note;
    }

    @Override
    public boolean equals(final Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "ActionablePermissionBinding(" +
                "action=" + action +
                ", note='" + note + '\'' +
                ", permission='" + super.toString() +
                ')';
    }
}
