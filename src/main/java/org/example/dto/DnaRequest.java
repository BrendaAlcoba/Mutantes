package org.example.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import org.example.validation.ValidDnaSequence;

@Data
public class DnaRequest {

    @NotNull(message = "El array de ADN no puede ser nulo.")
    @ValidDnaSequence
    private String[] dna;
}