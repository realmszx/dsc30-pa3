/*
 * Name: Rachit Jain
 * PID: A18942551
 */

import java.util.NoSuchElementException;

/**
 * Transcribes DNA into RNA and translates RNA codons into amino-acid characters.
 *
 * @author 
 * @since 2026-10-20
 */
class ProteinSynthesis {
    private static final int CODON_LENGTH = 3;
    private static final String START_CODON = "AUG";
    private static final char STOP_AMINO_ACID = '*';
    private static final char INCOMPLETE_CODON = '-';

    /**
     * Replaces uppercase T with U and stores every character in a new queue.
     *
     * @param dna the sequence to transcribe
     * @return a new queue containing the transcribed RNA characters
     * @throws IllegalArgumentException if the length is not divisible by three
     */
    public CharQueue transcribeDNA(String dna) {
        if (dna.length() % CODON_LENGTH != 0) {
            throw new IllegalArgumentException("DNA length must be divisible by three.");
        }
        CharQueue rna = new CharQueue();
        for (int index = 0; index < dna.length(); index++) {
            char nucleotide = dna.charAt(index);
            rna.enqueue(nucleotide == 'T' ? 'U' : nucleotide);
        }
        return rna;
    }

    /**
     * Consumes RNA in groups of three and translates from AUG through a stop.
     * Appends a hyphen if an incomplete codon is encountered after translation
     * has started. Returns an empty queue if no complete AUG codon is found.
     *
     * @param rna the RNA queue to read, beginning with its front character
     * @return a new queue containing the translated protein sequence
     */
    public CharQueue translateRNA(CharQueue rna) {
        CharQueue protein = new CharQueue();
        boolean started = false;

        while (!rna.isEmpty()) {
            StringBuilder codon = new StringBuilder(CODON_LENGTH);
            try {
                for (int index = 0; index < CODON_LENGTH; index++) {
                    codon.append(rna.dequeue());
                }
            } catch (NoSuchElementException exception) {
                // A partial final codon cannot produce an amino acid.
                if (started) {
                    protein.enqueue(INCOMPLETE_CODON);
                }
                break;
            }

            String triplet = codon.toString();
            if (!started && START_CODON.equals(triplet)) {
                started = true;
            }
            if (started) {
                char aminoAcid = CodonMap.getAminoAcid(triplet);
                protein.enqueue(aminoAcid);
                if (aminoAcid == STOP_AMINO_ACID) {
                    break;
                }
            }
        }
        return protein;
    }
}
