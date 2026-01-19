package com.helha.projetaemt_backend.application.utils;

public interface IQueryHandler<I, O>{
    O handle(I input);
}
