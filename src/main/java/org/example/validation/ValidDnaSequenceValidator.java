package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Implementa la interfaz para la anotación @ValidDnaSequence
public class ValidDnaSequenceValidator implements ConstraintValidator<ValidDnaSequence, String[]> {

    // Patrón regex para verificar que SOLO existan caracteres A, T, C, G
    private static final String VALID_CHARS_PATTERN = "^[ATCG]+$";
    private static final int MIN_SIZE = 4;

    @Override
    public void initialize(ValidDnaSequence constraintAnnotation) {

    }

    @Override
    public boolean isValid(String[] dna, ConstraintValidatorContext context) {

        // --- 1. Verificación básica (Null, Vacío o Demasiado Pequeño) ---
        if (dna == null || dna.length < MIN_SIZE) {
            return false;
        }

        final int N = dna.length; // N = número de filas

        // --- 2. Validación de Caracteres y Formato NxN ---
        for (String row : dna) {

            // Si alguna fila es nula o no tiene la longitud N (matriz no cuadrada)
            if (row == null || row.length() != N) {
                // Se podría usar ConstraintValidatorContext.buildConstraintViolationWithTemplate()
                return false;
            }

            // Verifica que el contenido de la fila solo sean caracteres válidos (A, T, C, G)
            if (!row.matches(VALID_CHARS_PATTERN)) {
                return false;
            }
        }

        // Si todas las filas pasan las validaciones NxN y ATCG, es válido.
        return true;
    }
}