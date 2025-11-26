package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.StatsResponse;
import org.example.repository.DnaRecordRepository;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de obtener las estadísticas de la base de datos (Nivel 3).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final DnaRecordRepository dnaRecordRepository;

    /**
     * Calcula y retorna las estadísticas de las verificaciones de ADN.
     * @return El DTO StatsResponse con los conteos y el ratio.
     */
    public StatsResponse getStats() {

        long countMutant = dnaRecordRepository.countByIsMutant(true);
        long countHuman = dnaRecordRepository.countByIsMutant(false);

        double ratio = calculateRatio(countMutant, countHuman);

        log.info("Estadísticas calculadas: Mutantes={}, Humanos={}, Ratio={}", countMutant, countHuman, ratio);

        return StatsResponse.builder()
                .countMutantDna(countMutant)
                .countHumanDna(countHuman)
                .ratio(ratio)
                .build();
    }

    /**
     * Calcula el ratio, manejando el caso de división por cero.
     * @param countMutant Número de mutantes.
     * @param countHuman Número de humanos.
     * @return El ratio (mutantes / humanos).
     */
    private double calculateRatio(long countMutant, long countHuman) {
        if (countHuman == 0) {
            // Si no hay humanos, el ratio es 0.0, o el mismo número de mutantes si es diferente de 0.
            // Devolvemos 0.0 para evitar Infinity, ya que el ratio debe ser finito.
            if (countMutant > 0) {
                return (double) countMutant; // Devolvemos el conteo de mutantes si humanos es 0.
            }
            return 0.0;
        }

        return (double) countMutant / countHuman;
    }
}