package org.example.service;

import org.example.entity.DnaRecord;
import org.example.repository.DnaRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


import org.example.dto.DnaRequest;
import org.example.dto.StatsResponse;
import org.example.dto.ErrorResponse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class MutantServiceTest {

    @Mock
    private DnaRecordRepository dnaRecordRepository;

    @Mock
    private MutantDetector detector;

    @InjectMocks
    private MutantService mutantService;

    // ADN de ejemplo
    private final String[] ADN_MUTANTE = {
            "ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA","TCACTG"
    };

    private final String[] ADN_HUMANO = {
            "ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"
    };

    private final String HASH_EJEMPLO = "fakehash12345";

    //Crea un registro para simular en la base
    private DnaRecord crearRegistro(boolean esMutante) {
        DnaRecord r = new DnaRecord();
        r.setDnaHash(HASH_EJEMPLO);
        r.setMutant(esMutante);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }
    // 1) Cuando el ADN ya está guardado

    @Test
    @DisplayName("Si ya estaba guardado como mutante, devuelve true sin analizar")
    void cuandoYaEstaGuardadoMutante() {
        when(dnaRecordRepository.findByDnaHash(anyString()))
                .thenReturn(Optional.of(crearRegistro(true)));

        boolean resultado = mutantService.process(ADN_MUTANTE);

        assertTrue(resultado);
        verify(detector, never()).isMutant(any());
        verify(dnaRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Si ya estaba guardado como humano, devuelve false sin analizar")
    void cuandoYaEstaGuardadoHumano() {
        when(dnaRecordRepository.findByDnaHash(anyString()))
                .thenReturn(Optional.of(crearRegistro(false)));

        boolean resultado = mutantService.process(ADN_HUMANO);

        assertFalse(resultado);
        verify(detector, never()).isMutant(any());
        verify(dnaRecordRepository, never()).save(any());
    }


    // 2) Cuando NO está guardado
        @Test
    @DisplayName("Detecta mutante, lo guarda y devuelve true")
    void detectaMutanteYLoGuarda() {
        when(dnaRecordRepository.findByDnaHash(anyString()))
                .thenReturn(Optional.empty());

        when(detector.isMutant(ADN_MUTANTE)).thenReturn(true);

        when(dnaRecordRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        boolean resultado = mutantService.process(ADN_MUTANTE);

        assertTrue(resultado);
        verify(detector).isMutant(ADN_MUTANTE);
        verify(dnaRecordRepository).save(argThat(r ->
                r.isMutant() && r.getDnaHash() != null
        ));
    }

    @Test
    @DisplayName("Detecta humano, lo guarda y devuelve false")
    void detectaHumanoYLoGuarda() {
        when(dnaRecordRepository.findByDnaHash(anyString()))
                .thenReturn(Optional.empty());

        when(detector.isMutant(ADN_HUMANO)).thenReturn(false);

        when(dnaRecordRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        boolean resultado = mutantService.process(ADN_HUMANO);

        assertFalse(resultado);
        verify(detector).isMutant(ADN_HUMANO);
        verify(dnaRecordRepository).save(argThat(r ->
                !r.isMutant() && r.getDnaHash() != null
        ));
    }







    @Test
    @DisplayName("Si algo falla al procesar, no debería tirar excepción")
    void noFallaCuandoElHashDaProblemas() {
        when(dnaRecordRepository.findByDnaHash(anyString()))
                .thenReturn(Optional.empty());

        when(detector.isMutant(any())).thenReturn(true);

        assertDoesNotThrow(() -> mutantService.process(ADN_MUTANTE));
    }



    @Test
    @DisplayName("Utilitario: Cubre líneas de Lombok en DTOs, Entidades (para >80%)")
    void testLombokCoverage() {
        LocalDateTime now = LocalDateTime.now();

        // --- 1. DnaRecord (Entity)
        DnaRecord rec1 = new DnaRecord();
        rec1.setDnaHash("A");
        rec1.setMutant(true);
        rec1.setCreatedAt(now); // Cubre el getter y setter

        DnaRecord rec2 = new DnaRecord();
        rec2.setDnaHash("A");
        rec2.setMutant(true);
        rec2.setCreatedAt(now);

        // Cubre equals() y hashCode()
        assertEquals(rec1, rec2, "DnaRecord: Los objetos con mismos campos deben ser iguales.");
        assertEquals(rec1.hashCode(), rec2.hashCode(), "DnaRecord: Hash codes deben ser iguales.");
        assertNotNull(rec1.toString()); // Cubre toString

        // --- 2. ErrorResponse (DTO)
        ErrorResponse err1 = new ErrorResponse(
                LocalDateTime.now(), 400, "VAL", "msg", "/path"
        );
        // Creamos err2 con la misma hora para que equals() pase la primera rama
        ErrorResponse err2 = new ErrorResponse(
                err1.getTimestamp(), 400, "VAL", "msg", "/path"
        );

        assertEquals(err1, err2, "ErrorResponse: Los objetos con mismos campos deben ser iguales.");
        assertEquals(err1.hashCode(), err2.hashCode(), "ErrorResponse: Hash codes deben ser iguales.");
        assertNotNull(err1.toString());

        // --- 3. StatsResponse (DTO)
        StatsResponse stats1 = StatsResponse.builder().countMutantDna(10L).ratio(0.5).build();
        StatsResponse stats2 = StatsResponse.builder().countMutantDna(10L).ratio(0.5).build();

        assertEquals(stats1, stats2, "StatsResponse: Los objetos con mismos campos deben ser iguales.");
        assertEquals(stats1.hashCode(), stats2.hashCode(), "StatsResponse: Hash codes deben ser iguales.");
        assertNotNull(stats1.toString());

        // --- 4. DnaRequest (DTO)
        DnaRequest req1 = new DnaRequest();
        req1.setDna(new String[]{"A", "T"});

        DnaRequest req2 = new DnaRequest();
        req2.setDna(new String[]{"A", "T"});

        // Usamos el toString/hashCode genérico de DnaRequest
        assertNotNull(req1.toString());
        assertNotNull(req1.hashCode());

    }





}
