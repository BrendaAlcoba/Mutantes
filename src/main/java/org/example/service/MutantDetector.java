package org.example.service;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class MutantDetector {

    private static final int SEQ = 4;
    // Patrón de caracteres válidos (A, T, C, G)
    private static final Pattern VALID = Pattern.compile("^[ATCG]+$");

    public boolean isMutant(String[] dna) {
        if (dna == null || dna.length < SEQ) return false;

        final int N = dna.length;
        char[][] m = new char[N][N];

        // === VALIDACIÓN NxN + caracteres (Optimizaciones) ===
        for (int i = 0; i < N; i++) {
            if (dna[i] == null || dna[i].length() != N) return false;
            if (!VALID.matcher(dna[i]).matches()) return false;
            m[i] = dna[i].toCharArray(); // Conversión a char[][]
        }

        int count = 0;

        // === BÚSQUEDA DE SECUENCIAS (Single Pass y Early Termination) ===
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {

                char base = m[r][c];

                // --- HORIZONTAL ---
                if (c <= N - SEQ) {
                    if (checkHorizontal(m, r, c, base)) {
                        if (++count > 1) return true;
                    }
                }

                // --- VERTICAL ---
                if (r <= N - SEQ) {
                    if (checkVertical(m, r, c, base)) {
                        if (++count > 1) return true;
                    }
                }

                // --- DIAGONAL DESCENDENTE ↘ ---
                if (r <= N - SEQ && c <= N - SEQ) {
                    if (checkDiagDown(m, r, c, base)) {
                        if (++count > 1) return true;
                    }
                }

                // --- DIAGONAL ASCENDENTE ↗ ---
                if (r >= SEQ - 1 && c <= N - SEQ) {
                    if (checkDiagUp(m, r, c, base)) {
                        if (++count > 1) return true;
                    }
                }
            }
        }

        return false;
    }

    // === Comparaciones directas ===

    private boolean checkHorizontal(char[][] m, int r, int c, char b) {
        return m[r][c + 1] == b &&
                m[r][c + 2] == b &&
                m[r][c + 3] == b;
    }

    private boolean checkVertical(char[][] m, int r, int c, char b) {
        return m[r + 1][c] == b &&
                m[r + 2][c] == b &&
                m[r + 3][c] == b;
    }

    private boolean checkDiagDown(char[][] m, int r, int c, char b) {
        return m[r + 1][c + 1] == b &&
                m[r + 2][c + 2] == b &&
                m[r + 3][c + 3] == b;
    }

    private boolean checkDiagUp(char[][] m, int r, int c, char b) {
        return m[r - 1][c + 1] == b &&
                m[r - 2][c + 2] == b &&
                m[r - 3][c + 3] == b;
    }
}
