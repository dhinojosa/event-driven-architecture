package com.evolutionnext.customer.application.command;


public sealed interface CustomerCommand permits CustomerCommand.Create {
    public record Create(String firstName, String lastName, String email, String state) implements CustomerCommand{}
}
