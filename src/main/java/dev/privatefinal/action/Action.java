package dev.privatefinal.action;

@FunctionalInterface
public interface Action {

    void run(ActionContext context);
}
