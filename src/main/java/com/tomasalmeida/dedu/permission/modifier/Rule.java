package com.tomasalmeida.dedu.permission.modifier;

import org.jetbrains.annotations.NotNull;

import com.tomasalmeida.dedu.permission.context.ContextExecution;

public interface Rule {

    void run(@NotNull final ContextExecution contextExecution);

}
