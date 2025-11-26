package org.example.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.dto.DnaRequest;
import org.example.dto.StatsResponse;
import org.example.service.MutantService;
import org.example.service.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

// Importaciones de Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "Mutant Detector", description = "API principal para la detección y registro de ADN mutante.")
public class MutantController {

    private final MutantService mutantService;
    private final StatsService statsService;

    @PostMapping("/mutant")
    @Operation(summary = "Verificar si una secuencia de ADN pertenece a un mutante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. El ADN es mutante (contiene más de una secuencia de 4 bases iguales)."),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN. El ADN no es mutante (contiene 0 o 1 secuencia)."),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST. La secuencia de ADN es inválida (no es NxN o contiene caracteres no permitidos).")
    })
    public ResponseEntity<Void> isMutant(@RequestBody @Valid DnaRequest req) {

        boolean result = mutantService.process(req.getDna());

        return result ?
                ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas de las verificaciones de ADN realizadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. Retorna el conteo de mutantes, humanos y el ratio.")
    })
    public ResponseEntity<StatsResponse> stats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}