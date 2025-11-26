package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * DTO para la respuesta del endpoint /stats (Nivel 3).
 * Utiliza @Value y @Builder de Lombok para ser inmutable y fácil de construir.
 */
@Value
@Builder
public class StatsResponse {


    @JsonProperty("count_mutant_dna")
    long countMutantDna;

    @JsonProperty("count_human_dna")
    long countHumanDna;

    @JsonProperty("ratio")
    double ratio;
}