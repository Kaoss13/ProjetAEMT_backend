package com.helha.projetaemt_backend.application.utils;

public interface ICommandHandler<I, O> {
    O handle(I input);
}
