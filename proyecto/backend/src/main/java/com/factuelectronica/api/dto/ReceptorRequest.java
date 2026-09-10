package com.factuelectronica.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReceptorRequest(
        @NotNull UUID emisorId,
        @NotBlank String tipoIdentificacion,
        @NotBlank String identificacion,
        @NotBlank String nombre,
        @Email String correo,
        String telefono,
        String codigoActividadReceptor,
        String provincia,
        String canton,
        String distrito,
        String otrasSenas
) {}
