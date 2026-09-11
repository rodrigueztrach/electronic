package com.factuelectronica.api.service;

import com.factuelectronica.api.dto.ReceptorRequest;
import com.factuelectronica.api.dto.ReceptorResponse;
import com.factuelectronica.api.exception.ResourceNotFoundException;
import com.factuelectronica.api.model.Emisor;
import com.factuelectronica.api.model.Receptor;
import com.factuelectronica.api.repository.ReceptorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceptorService {

    private final ReceptorRepository receptorRepository;
    private final EmisorService emisorService;

    @Transactional
    public ReceptorResponse crear(ReceptorRequest request) {
        Emisor emisor = emisorService.buscarOFallar(request.emisorId());
        Receptor receptor = Receptor.builder()
                .emisor(emisor)
                .tipoIdentificacion(request.tipoIdentificacion())
                .identificacion(request.identificacion())
                .nombre(request.nombre())
                .correo(request.correo())
                .telefono(request.telefono())
                .codigoActividadReceptor(request.codigoActividadReceptor())
                .provincia(request.provincia())
                .canton(request.canton())
                .distrito(request.distrito())
                .otrasSenas(request.otrasSenas())
                .build();
        return ReceptorResponse.desde(receptorRepository.save(receptor));
    }

    public Page<ReceptorResponse> listar(UUID emisorId, String nombre, Pageable pageable) {
        Page<Receptor> pagina = (nombre == null || nombre.isBlank())
                ? receptorRepository.findByEmisorId(emisorId, pageable)
                : receptorRepository.findByEmisorIdAndNombreContainingIgnoreCase(emisorId, nombre, pageable);
        return pagina.map(ReceptorResponse::desde);
    }

    public ReceptorResponse obtener(UUID id) {
        return ReceptorResponse.desde(buscarOFallar(id));
    }

    @Transactional
    public ReceptorResponse actualizar(UUID id, ReceptorRequest request) {
        Receptor receptor = buscarOFallar(id);
        receptor.setNombre(request.nombre());
        receptor.setCorreo(request.correo());
        receptor.setTelefono(request.telefono());
        receptor.setCodigoActividadReceptor(request.codigoActividadReceptor());
        receptor.setProvincia(request.provincia());
        receptor.setCanton(request.canton());
        receptor.setDistrito(request.distrito());
        receptor.setOtrasSenas(request.otrasSenas());
        return ReceptorResponse.desde(receptorRepository.save(receptor));
    }

    @Transactional
    public void inactivar(UUID id) {
        Receptor receptor = buscarOFallar(id);
        receptor.setEstado("inactivo");
        receptorRepository.save(receptor);
    }

    private Receptor buscarOFallar(UUID id) {
        return receptorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptor no encontrado: " + id));
    }
}
