package com.tomasalmeida.dedu.permission.modifier;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.permission.modifier.context.ContextRule;

public interface BindingTransformationRule extends Rule {

    void run(@NotNull final ContextRule context);

}
