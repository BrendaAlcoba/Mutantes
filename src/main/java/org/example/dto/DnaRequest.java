package org.example.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import org.example.validation.ValidDnaSequence;
import jakarta.validation.constraints.NotEmpty;

@Data
public class DnaRequest {

    @NotNull(message = "El array de ADN no puede ser nulo.")
    @NotEmpty(message = "El array de ADN no puede estar vacío.")
    @ValidDnaSequence
    private String[] dna;
}