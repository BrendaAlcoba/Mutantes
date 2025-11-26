package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.DnaRecord;
import org.example.repository.DnaRecordRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class MutantService {

    private final MutantDetector detector;
    private final DnaRecordRepository repo;

    public boolean process(String[] dna) {

        String hash = hashOf(dna);

        var maybeRecord = repo.findByDnaHash(hash);
        if (maybeRecord.isPresent()) {
            return maybeRecord.get().isMutant();
        }

        boolean isMutant = detector.isMutant(dna);

        DnaRecord rec = new DnaRecord();
        rec.setDnaHash(hash);
        rec.setMutant(isMutant);
        rec.setCreatedAt(LocalDateTime.now());
        repo.save(rec);

        return isMutant;
    }

    private String hashOf(String[] dna) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(String.join("", dna).getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing DNA");
        }
    }
}
