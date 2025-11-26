package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MutantDetectorTest {

    private MutantDetector detector;

    @BeforeEach
    void setup() {
        detector = new MutantDetector();
    }


    // 1) CASOS MUTANTES (TRUE)


    @Test
    @DisplayName("Mutante: secuencia horizontal")
    void mutanteHorizontal() {
        String[] dna = {
                "AAAAGT", // Contiene AAAA
                "CAGTGC",
                "TAGGTC",
                "AGTGGC",
                "CTCTGC",
                "CCCCCC" // Contiene CCCC
        };
        assertTrue(detector.isMutant(dna), "Debe ser mutante: Horizontal AAAA y Horizontal CCCC");
    }

    @Test
    @DisplayName("Mutante: secuencia vertical")
    void mutanteVertical() {
        String[] dna = {
                "ATGCGA",
                "ATGTGA",
                "ATATGA",
                "ATACGA",
                "ATCCTA",
                "TCACTG"
        };
        assertTrue(detector.isMutant(dna), "Debe ser mutante: Columna 1 tiene 4 T's y otra secuencia vertical.");
    }

    @Test
    @DisplayName("Mutante: diagonal descendente (↘) y ascendente (↗)")
    void detectaMutanteConDiagonales() {
        String[] dna = {
                "ATGCGA",
                "AAGCCA",
                "AAACTA",
                "AAGCCA",  // AAAA descendente
                "GCCTTA",  // TTTT ascendente
                "TCCATG"
        };
        assertTrue(detector.isMutant(dna), "Debe ser mutante: Dos diagonales (Ascendente/Descendente) encontradas.");
    }

    @Test
    @DisplayName("Mutante: dos horizontales distintas")
    void dosHorizontales() {
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AAAAAG", // Secuencia 1
                "CCCCGA", // Secuencia 2
                "TCACTG"
        };
        assertTrue(detector.isMutant(dna), "Debe ser mutante: Dos secuencias horizontales distintas.");
    }

    @Test
    @DisplayName("Mutante: solapamiento en diagonal descendente (AAAAA)")
    void solapamientoDiagonalDescendente() {
        String[] dna = {
                "ATGCGA",
                "CATGTC",
                "GCATGT",
                "TACATG",
                "AGTATT", // La diagonal AAAAA empieza en (0,3)
                "TCACTG"
        };
        assertTrue(detector.isMutant(dna), "Debe ser mutante: AAAAA en diagonal descendente cuenta como dos secuencias.");
    }

    @Test
    @DisplayName("Mutante: solapamiento en diagonal ascendente (GGGGG)")
    void solapamientoDiagonalAscendente() {
        String[] dna = {
                "ATGCGA", // G en (0,5)
                "CAGTGG", // G en (1,4)
                "TTATGG", // G en (2,3)
                "AGAGGG", // G en (3,2)
                "CCCCTG", // G en (4,1)
                "TCACTG"  // La secuencia de GGGGG está en ascendente
        };
        assertTrue(detector.isMutant(dna), "Debe ser mutante: GGGGG en diagonal ascendente cuenta como dos secuencias.");
    }




    @Test
    @DisplayName("Mutante: una diagonal descendente y una secuencia vertical")
    void diagonalYVertical() {
        String[] dna = {
                "ATGCGA",
                "CAGTGA",
                "TACGTA",
                "AGGGGA", // ← Vertical GGGG empieza aquí
                "GCCTGA",
                "TCTCTG"  // ← Diagonal descendente AAAA desde (2,2)
        };
        assertTrue(detector.isMutant(dna), "Debe detectar mutante: combinación diagonal + vertical.");
    }

    @Test
    @DisplayName("Humano: matriz grande sin ninguna secuencia de 4 iguales")
    void humanoMatrizGrandeSinSecuencias() {
        String[] dna = {
                "ATGCGT",
                "CAGTAC",
                "TATGCA",
                "GTCAGT",
                "CACGTG",
                "TGTACA"
        };
        assertFalse(detector.isMutant(dna), "No debe detectar mutante en una matriz grande sin secuencias.");
    }




    @Test
    @DisplayName("Mutante: Caso 4x4 mínimo con dos secuencias")
    void mutanteMinimo4x4() {
        String[] dna = {
                "AAAA", // Secuencia 1
                "TGCG",
                "GATT",
                "CCCC" // Secuencia 2
        };
        assertTrue(detector.isMutant(dna), "Matriz 4x4 debe ser mutante si tiene 2 secuencias.");
    }

    @Test
    @DisplayName("Mutante: dos secuencias en las esquinas inferiores")
    void mutanteEnBordesInferiores() {
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGACGT",
                "GCTAGG",
                "GCTAAA" // AAAA horizontal y GGGG vertical
        };
        assertTrue(detector.isMutant(dna), "Debe ser mutante: Secuencia AAAA horizontal en última fila y GGGG vertical en última columna.");
    }



    //HUMANO

    @Test
    @DisplayName("Humano: solo 1 secuencia horizontal (GGGG)")
    void unaHorizontal() {
        String[] dna = {
                "ATGCGA",
                "CAGTAC",
                "TTATGT",
                "AGGGGA", // <--- UNA ÚNICA secuencia (GGGG)
                "CTATGT",
                "TCACTG"
        };
        assertFalse(detector.isMutant(dna), "Una única secuencia de GGGG debe resultar en Humano (FALSE).");
    }

    @Test
    @DisplayName("Inválido: matriz con filas de longitud distinta")
    void filasDistintas() {
        String[] dna = {
                "ATGCGA", // Longitud 6
                "CAGT",   // Longitud 4
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        assertFalse(detector.isMutant(dna), "Filas de distinta longitud deben ser inválidas.");
    }

    @Test
    @DisplayName("Humano: Caso diagonal aislado (NO debe ser mutante)")
    void diagonalAislada() {
        String[] dna = {
                "ATGCGA",
                "CAAGCT",
                "TGACAC",
                "ACGTCA",
                "GCTACT",
                "CATGCT"
        };
        assertFalse(detector.isMutant(dna), "El caso diagonal aislado debe ser Humano (FALSE).");
    }


    //  OTROS CASOS DE PRUEBA


    @Test
    @DisplayName("Humano: sin secuencias mutantes")
    void humanoSinSecuencias() {
        String[] dna = {
                "ATGCAT",
                "CTAGCC",
                "TATGTC",
                "GTAGGT",
                "GTCACG",
                "TCACTG"
        };
        assertFalse(detector.isMutant(dna), "Un ADN sin secuencias de 4 debe ser Humano.");
    }

    @Test
    @DisplayName("Inválido: matriz no es NxN")
    void noNxN() {
        String[] dna = {
                "ATGC",
                "CAGT",
                "TTAT" // Solo 3 filas
        };
        assertFalse(detector.isMutant(dna), "Matriz no cuadrada (3x4) debe ser inválida.");
    }

    @Test
    @DisplayName("Inválido: caracteres no permitidos")
    void caracteresInvalidos() {
        String[] dna = {
                "ATXCGA", // X es inválido
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        assertFalse(detector.isMutant(dna), "Caracteres fuera de A,T,C,G deben ser inválidos.");
    }


    @Test
    @DisplayName("Inválido: null → humano")
    void nullEsHumano() {
        assertFalse(detector.isMutant(null), "Input nulo debe ser inválido.");
    }

    @Test
    @DisplayName("Inválido: arreglo vacío → humano")
    void emptyEsHumano() {
        assertFalse(detector.isMutant(new String[]{}), "Input vacío debe ser inválido.");
    }




    @Test
    @DisplayName("Mutante: solapamiento (AAAAA → 2 secuencias)")
    void solapamiento() {
        String[] dna = {
                "AAAAAG",
                "CTGTGC",
                "GTATGC",
                "AGTGCC",
                "CTCTGT",
                "TCACTG"
        };
        assertTrue(detector.isMutant(dna), "El solapamiento (AAAAA) debe contar como 2 secuencias.");
    }

    @Test
    @DisplayName("Inválido: arreglo con una fila nula")
    void arregloConFilaNula() {
        String[] dna = {
                "ATGCGA",
                null,       // Fila nula
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        assertFalse(detector.isMutant(dna), "El arreglo no debe ser procesado si contiene una fila nula.");
    }


}