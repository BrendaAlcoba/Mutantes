package org.example.controller;

import org.example.entity.DnaRecord;
import org.example.repository.DnaRecordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.example.dto.DnaRequest;
import org.example.dto.StatsResponse;
import org.example.dto.ErrorResponse;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración de los endpoints /mutant y /stats.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MutantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DnaRecordRepository dnaRecordRepository;

    // Requests JSON de uso común
    private final String MUTANTE = json("""
        ["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA","TCACTG"]
    """);

    private final String HUMANO = json("""
        ["ATGCGA","CAGTGC","TTATTT","AGACGG","GCGTCA","TCACTG"]
    """);

    private final String NO_CUADRADO = json("""
        ["ATGC","CAGT","TTAT"]
    """);

    private final String CON_CARACTER_INVALIDO = json("""
        ["ATXCG","CAGTG","TTATG","AGAGG","CCCCT"]
    """);
    private final String SIN_SECUENCIA = json("""
        ["ATGC","CAGT","TTAT","AGAC"]
    """);


    /** Limpio la base después de cada test para evitar interferencias. */
    @AfterEach
    void limpiarBase() {
        dnaRecordRepository.deleteAll();
    }

    // POST /mutant

    @Test
    @DisplayName("Guarda mutante y devuelve 200")
    void guardaMutanteYDevuelveOk() throws Exception {

        postDna(MUTANTE)
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertEquals(1, dnaRecordRepository.count());
        assertTrue(dnaRecordRepository.findAll().get(0).isMutant());
    }

    @Test
    @DisplayName("Guarda humano y devuelve 403")
    void guardaHumanoYDevuelveForbidden() throws Exception {

        postDna(HUMANO)
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        assertEquals(1, dnaRecordRepository.count());
        assertFalse(dnaRecordRepository.findAll().get(0).isMutant());
    }

    @Test
    @DisplayName("Si mando el mismo ADN dos veces, usa la caché")
    void usaCacheCuandoElAdnYaExiste() throws Exception {

        postDna(MUTANTE).andExpect(status().isOk());
        postDna(MUTANTE).andExpect(status().isOk());

        assertEquals(1, dnaRecordRepository.count(),
                "No debería guardar dos veces el mismo ADN");
    }

    @Test
    @DisplayName("Detecta mutante con diagonal ascendente")
    void mutanteDiagonalAscendente() throws Exception {

        String dna = json("""
            ["ATGCGA","CAGTGC","TTATGT","AGAAAG","CCCAAA","TCACTG"]
        """);

        postDna(dna).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Detecta mutante con secuencia vertical")
    void mutanteVertical() throws Exception {

        String dna = json("""
            ["ATGCGA","AAGTGC","ATATGT","AGAAGG","ACCCTA","TCACTG"]
        """);

        postDna(dna).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Guarda humano (0 secuencias) y devuelve 403")
    void guardaHumanoSinSecuenciasYDevuelveForbidden() throws Exception {

        postDna(SIN_SECUENCIA)
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));

        assertEquals(1, dnaRecordRepository.count());
        assertFalse(dnaRecordRepository.findAll().get(0).isMutant(),
                "Debe guardar el registro como humano (isMutant=false)");
    }


    // Validaciones


    @Test
    @DisplayName("Devuelve 400 si el ADN no es NxN")
    void devuelveBadRequestSiElAdnNoEsNxN() throws Exception {

        postDna(NO_CUADRADO)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("debe ser NxN")));

        assertEquals(0, dnaRecordRepository.count());
    }

    @Test
    @DisplayName("Devuelve 400 si el ADN trae caracteres inválidos")
    void devuelveBadRequestSiElAdnTieneCaracteresInvalidos() throws Exception {

        postDna(CON_CARACTER_INVALIDO)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("solo A")));
    }

    @Test
    @DisplayName("Devuelve 400 si el campo dna es null o falta")
    void devuelveBadRequestDnaNull() throws Exception {

        postDna("{ }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Devuelve 400 si el ADN es menor a 4x4")
    void devuelveBadRequestSiEsMuyPequenio() throws Exception {

        String pequeno = json("""
            ["AAA","AAA","AAA"]
        """);

        postDna(pequeno)
                .andExpect(status().isBadRequest());
    }


    // GET /stats


    @Test
    @DisplayName("Devuelve las estadísticas correctas")
    void devuelveEstadisticasCorrectas() throws Exception {

        dnaRecordRepository.save(nuevoRegistro("h1", true));
        dnaRecordRepository.save(nuevoRegistro("h2", true));
        dnaRecordRepository.save(nuevoRegistro("h3", false));

        mockMvc.perform(get("/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count_mutant_dna", is(2)))
                .andExpect(jsonPath("$.count_human_dna", is(1)))
                .andExpect(jsonPath("$.ratio", is(2.0)));
    }


    // Cobertura de Entidad y DTOs


    @Test
    @DisplayName("El registro guardado en BD tiene hash, fecha y valor mutante")
    void verificaCamposDeLaEntidad() throws Exception {

        postDna(MUTANTE).andExpect(status().isOk());

        DnaRecord rec = dnaRecordRepository.findAll().get(0);

        assertNotNull(rec.getDnaHash());
        assertNotNull(rec.getCreatedAt());
        assertTrue(rec.isMutant());
    }

    @Test
    @DisplayName("ErrorResponse se genera correctamente desde el ExceptionHandler")
    void verificaErrorResponseGenerado() throws Exception {

        postDna(CON_CARACTER_INVALIDO)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("solo A")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("StatsResponse serializa correctamente los campos")
    void verificaSerializacionDeStatsResponse() throws Exception {

        dnaRecordRepository.save(nuevoRegistro("h1", true));
        dnaRecordRepository.save(nuevoRegistro("h2", false));

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count_mutant_dna", is(1)))
                .andExpect(jsonPath("$.count_human_dna", is(1)))
                .andExpect(jsonPath("$.ratio", is(1.0)));
    }

    @Test
    @DisplayName("Cobertura: Cubre DnaRecord equals, hashCode, getters y setters")
    void testDnaRecordCoverage() {
        LocalDateTime now = LocalDateTime.now();

        DnaRecord r1 = new DnaRecord();
        r1.setId(1L);
        r1.setDnaHash("hash_test");
        r1.setMutant(true);
        r1.setCreatedAt(now);

        assertEquals(1L, r1.getId());
        assertEquals("hash_test", r1.getDnaHash());
        assertTrue(r1.isMutant());
        assertEquals(now, r1.getCreatedAt());

        DnaRecord r2 = new DnaRecord();
        r2.setDnaHash("hash_test");
        r2.setMutant(true);
        r2.setCreatedAt(now);

        // No tienen el mismo ID → no deben ser iguales
        assertNotEquals(r1, r2);

        DnaRecord r3 = new DnaRecord();
        r3.setDnaHash("otro_hash");
        assertNotEquals(r1.hashCode(), r3.hashCode());

        assertNotNull(r1.toString());
    }

    @Test
    @DisplayName("Cobertura: Cubre DTOs (Error, Stats, DnaRequest)")
    void testDTOCoverageLombok() throws Exception {

        // DnaRequest
        DnaRequest req1 = new DnaRequest();
        req1.setDna(new String[]{"A", "T"});
        assertNotNull(req1.getDna());
        assertNotNull(req1.toString());

        // StatsResponse
        StatsResponse stats = StatsResponse.builder()
                .countMutantDna(10L)
                .countHumanDna(50L)
                .ratio(0.2)
                .build();
        assertEquals(10L, stats.getCountMutantDna());
        assertNotNull(stats.toString());

        // ErrorResponse
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(), 400, "Error", "Mensaje", "/path");
        assertNotNull(error.toString());

        // Rama faltante: enviar un dna=null
        postDna("{\"dna\": null}")
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Cobertura: DnaRequest equals/hashCode y toString")
    void testDnaRequestCoverage() {
        String[] dna1 = {"ATGC", "CGTA"};
        String[] dna2 = {"ATGC", "CGTA"};
        String[] dna3 = {"AGCT", "TCGA"};

        DnaRequest req1 = new DnaRequest();
        req1.setDna(dna1);

        DnaRequest req2 = new DnaRequest();
        req2.setDna(dna2);

        DnaRequest req3 = new DnaRequest();
        req3.setDna(dna3);

        // 1. Cubrir equals() (true y false) y hashCode()
        assertEquals(req1, req2, "Dos DnaRequests con el mismo ADN deben ser iguales.");
        assertEquals(req1.hashCode(), req2.hashCode(), "El hash code debe ser el mismo.");
        assertNotEquals(req1, req3, "Dos DnaRequests con diferente ADN no deben ser iguales.");
        assertNotEquals(req1.hashCode(), req3.hashCode(), "Los hash codes deben ser diferentes.");

        // 2. Cubrir toString()
        assertNotNull(req1.toString(), "El método toString no debe ser nulo.");
    }


    @Test
    @DisplayName("Cobertura: Llama a todos los paths de equals() y hashCode() de ErrorResponse")
    void testErrorResponseLombokCoverage() {
        LocalDateTime now = LocalDateTime.now();

        // Objeto base
        ErrorResponse e1 = new ErrorResponse(now, 400, "Bad", "Msg", "/path");

        // Objeto con mismos valores
        ErrorResponse e2 = new ErrorResponse(now, 400, "Bad", "Msg", "/path");

        // Objeto con diferente status
        ErrorResponse e3 = new ErrorResponse(now, 500, "Bad", "Msg", "/path");

        // 1. Cubrir hashCode() y equals(true)
        assertEquals(e1, e2, "Objetos con mismos valores deben ser iguales.");
        assertEquals(e1.hashCode(), e2.hashCode(), "El hash code debe ser el mismo.");

        // 2. Cubrir equals(obj) y equals(null)
        assertTrue(e1.equals(e1), "Objeto debe ser igual a sí mismo."); // Cubre una rama
        assertFalse(e1.equals(null), "Objeto no debe ser igual a null."); // Cubre la rama de null

        // 3. Cubrir equals(diferente clase)
        assertFalse(e1.equals("diferente"), "Debe ser diferente a una cadena."); // Cubre la rama de ClassCheck

        // 4. Cubrir equals(false)
        assertNotEquals(e1, e3, "Objetos con diferente status deben ser diferentes.");

        // 5. Cubrir toString()
        assertNotNull(e1.toString(), "El método toString no debe ser nulo.");
    }



    @Test
    @DisplayName("Cobertura: Fuerza ramas de equals(Object) en ErrorResponse")
    void testErrorResponseInheritanceBranches() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse e1 = new ErrorResponse(now, 400, "Bad", "Msg", "/path");

        assertFalse(e1.equals(null), "Debe retornar false al comparar con null.");
        Object differentClass = new Object();
        assertFalse(e1.equals(differentClass), "Debe retornar false al comparar con diferente clase.");

        assertTrue(e1.equals(e1), "Debe retornar true al comparar consigo mismo.");


        e1.setStatus(500);
        e1.setTimestamp(now.minusSeconds(1));
    }





    // Helpers


    /** Construye JSON válido para DnaRequest. */
    private static String json(String array) {
        return "{ \"dna\": " + array + " }";
    }

    /** Helper simple para no repetir código en POST /mutant. */
    private org.springframework.test.web.servlet.ResultActions postDna(String json)
            throws Exception {

        return mockMvc.perform(post("/mutant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    /** Crea un registro básico para pruebas de stats. */
    private DnaRecord nuevoRegistro(String hash, boolean mutante) {
        DnaRecord r = new DnaRecord();
        r.setDnaHash(hash);
        r.setMutant(mutante);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }
}
