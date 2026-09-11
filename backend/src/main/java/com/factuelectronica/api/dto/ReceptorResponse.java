package com.factuelectronica.api.dto;

import com.factuelectronica.api.model.Receptor;

import java.util.UUID;

public record ReceptorResponse(
        UUID id,
        String tipoIdentificacion,
        String identificacion,
        String nombre,
        String correo,
        String estado
) {
    public static ReceptorResponse desde(Receptor r) {
        return new ReceptorResponse(
                r.getId(),
                r.getTipoIdentificacion(),
                r.getIdentificacion(),
                r.getNombre(),
                r.getCorreo(),
                r.getEstado()
        );
    }
}
