package org.example.service;

import org.example.dto.StatsResponse;
import org.example.repository.DnaRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private DnaRecordRepository dnaRecordRepository;

    @InjectMocks
    private StatsService statsService;
//TESTS
    @Test
    @DisplayName("Calcula bien el ratio cuando hay mutantes y humanos")
    void calculaRatioConValoresNormales() {

        when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
        when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

        StatsResponse res = statsService.getStats();

        assertEquals(40, res.getCountMutantDna());
        assertEquals(100, res.getCountHumanDna());
        assertEquals(0.4, res.getRatio(), 0.0001);
    }

    @Test
    @DisplayName("Cuando no hay humanos, el ratio debe ser igual a los mutantes")
    void ratioSinHumanos() {

        when(dnaRecordRepository.countByIsMutant(true)).thenReturn(10L);
        when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

        StatsResponse res = statsService.getStats();

        assertEquals(10, res.getCountMutantDna());
        assertEquals(0, res.getCountHumanDna());
        assertEquals(10.0, res.getRatio(), 0.0001);
    }

    @Test
    @DisplayName("Si no hay ningún registro, el ratio debe ser 0.0")
    void ratioSinDatos() {

        when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
        when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

        StatsResponse res = statsService.getStats();

        assertEquals(0, res.getCountMutantDna());
        assertEquals(0, res.getCountHumanDna());
        assertEquals(0.0, res.getRatio());
    }

    @Test
    @DisplayName("Cuando no hay mutantes, el ratio debe ser 0.0")
    void ratioSoloHumanos() {

        when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
        when(dnaRecordRepository.countByIsMutant(false)).thenReturn(50L);

        StatsResponse res = statsService.getStats();

        assertEquals(0, res.getCountMutantDna());
        assertEquals(50, res.getCountHumanDna());
        assertEquals(0.0, res.getRatio(), 0.0001);
    }

    @Test
    @DisplayName("Cuando mutantes y humanos son iguales, el ratio es 1.0")
    void ratioIgualACero() {

        when(dnaRecordRepository.countByIsMutant(true)).thenReturn(5L);
        when(dnaRecordRepository.countByIsMutant(false)).thenReturn(5L);

        StatsResponse res = statsService.getStats();

        assertEquals(5, res.getCountMutantDna());
        assertEquals(5, res.getCountHumanDna());
        assertEquals(1.0, res.getRatio(), 0.0001);
    }

    @Test
    @DisplayName("Cuando hay más mutantes que humanos, el ratio debe ser > 1.0")
    void ratioMayorAUno() {

        when(dnaRecordRepository.countByIsMutant(true)).thenReturn(100L);
        when(dnaRecordRepository.countByIsMutant(false)).thenReturn(40L);

        StatsResponse res = statsService.getStats();

        assertEquals(100, res.getCountMutantDna());
        assertEquals(40, res.getCountHumanDna());
        assertEquals(2.5, res.getRatio(), 0.0001);
    }


}
