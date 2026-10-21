import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProteinSynthesisTest {
    private final ProteinSynthesis synthesis = new ProteinSynthesis();

    @Test
    void transcribeDNAReplacesOnlyUppercaseT() {
        assertEquals("AUGAUCUCGUAA", drain(synthesis.transcribeDNA("ATGATCTCGTAA")));
        assertEquals("AUCAUGCGUUAGACC", drain(synthesis.transcribeDNA("ATCATGCGTTAGACC")));
        assertEquals("UUUUUU", drain(synthesis.transcribeDNA("TTTTTT")));
        assertEquals("ACGACG", drain(synthesis.transcribeDNA("ACGACG")));
    }

    @Test
    void transcribeDNAAcceptsEmptyAndNonDnaCharactersAtValidLengths() {
        assertTrue(synthesis.transcribeDNA("").isEmpty());
        assertEquals("AUU123", drain(synthesis.transcribeDNA("AUT123")));
        assertEquals("atg", drain(synthesis.transcribeDNA("atg")));
        assertEquals("!?U", drain(synthesis.transcribeDNA("!?T")));
    }

    @Test
    void transcribeDNAReturnsIndependentQueues() {
        CharQueue first = synthesis.transcribeDNA("ATG");
        CharQueue second = synthesis.transcribeDNA("ATG");
        assertNotSame(first, second);
        first.dequeue();
        assertEquals(3, second.size());
        assertEquals("AUG", drain(second));
    }

    @Test
    void transcribeDNARejectsLengthsNotDivisibleByThree() {
        assertThrows(IllegalArgumentException.class, () -> synthesis.transcribeDNA("A"));
        assertThrows(IllegalArgumentException.class, () -> synthesis.transcribeDNA("AT"));
        assertThrows(IllegalArgumentException.class, () -> synthesis.transcribeDNA("TACAT"));
        assertThrows(IllegalArgumentException.class, () -> synthesis.transcribeDNA("ATGC"));
    }

    @Test
    void translateRNAHandlesAssignmentExamples() {
        assertEquals("MIS*", translate("AUGAUCUCGUAA"));
        assertEquals("MR*", translate("AUCAUGCGUUAGACC"));
        assertEquals("MLC", translate("AUGCUAUGU"));
        assertEquals("", translate("CCCCUGUCAUAA"));
        assertEquals("", drain(synthesis.translateRNA(synthesis.transcribeDNA("AUT123"))));
    }

    @Test
    void translateRNAIncludesEachStopCodonAndStopsAtFirstStop() {
        assertEquals("M*", translate("AUGUAAAUG"));
        assertEquals("M*", translate("AUGUAGAUG"));
        assertEquals("M*", translate("AUGUGAAUG"));
        assertEquals("MF*", translate("AUGUUUUAAUGG"));
        assertEquals("M*", translate("AUGUAAA"));
    }

    @Test
    void translateRNASkipsCodonsUntilStartAndUsesFirstStart() {
        assertEquals("M", translate("UUUAUG"));
        assertEquals("MM", translate("AUGAUG"));
        assertEquals("M*", translate("UAAUAGUGAAUGUAA"));
        assertEquals("MF", translate("CCCAUGUUU"));
    }

    @Test
    void translateRNAReturnsEmptyWhenThereIsNoStart() {
        assertEquals("", translate(""));
        assertEquals("", translate("UUUCCCGGG"));
        assertEquals("", translate("UAAUAGUGA"));
        assertEquals("", translate("AU"));
        assertEquals("", translate("CCCUU"));
    }

    @Test
    void translateRNAReadsCodonsFromTheFirstQueueCharacter() {
        assertEquals("", translate("CAUGAA"));
        assertEquals("", translate("CCAUGA"));
        assertEquals("MK", translate("CCCAUGAAA"));
    }

    @Test
    void translateRNAAppendsHyphenOnlyForIncompleteCodonAfterStart() {
        assertEquals("M-", translate("AUGA"));
        assertEquals("M-", translate("AUGAU"));
        assertEquals("MI-", translate("AUGAUCUC"));
        assertEquals("MI-", translate("AUGAUCU"));
        assertEquals("M", translate("AUG"));
        assertEquals("MI", translate("AUGAUC"));
    }

    @Test
    void translateRNAUsesAllSixtyOneSenseCodons() {
        String[] codonGroups = {
            "UUUUUC", "UUAUUGCUUCUCCUACUG", "UCUUCCUCAUCGAGUAGC",
            "UAUUAC", "UGUUGC", "UGG", "CCUCCCCCACCG", "CAUCAC",
            "CAACAG", "CGUCGCCGACGGAGAAGG", "AUUAUCAUA", "AUG",
            "ACUACCACAACG", "AAUAAC", "AAAAAG", "GUUGUCGUAGUG",
            "GCUGCCGCAGCG", "GAUGAC", "GAAGAG", "GGUGGCGGAGGG"
        };
        String aminoAcids = "FLSYCWPHQRIMTNKVADEG";
        int codonCount = 0;
        for (int group = 0; group < codonGroups.length; group++) {
            String codons = codonGroups[group];
            for (int index = 0; index < codons.length(); index += 3) {
                String codon = codons.substring(index, index + 3);
                String expected = "M" + aminoAcids.charAt(group) + "*";
                assertEquals(expected, translate("AUG" + codon + "UAA"), codon);
                codonCount++;
            }
        }
        assertEquals(61, codonCount);
    }

    @Test
    void translateRNAHandlesLongInputAndOutputQueueGrowth() {
        StringBuilder rna = new StringBuilder("AUG");
        StringBuilder expected = new StringBuilder("M");
        for (int count = 0; count < 300; count++) {
            rna.append("GCU");
            expected.append('A');
        }
        rna.append("UGA");
        expected.append('*');
        CharQueue protein = synthesis.translateRNA(queueOf(rna.toString()));
        assertEquals(302, protein.size());
        assertEquals('M', protein.peek());
        assertEquals(expected.toString(), drain(protein));
    }

    private String translate(String rna) {
        return drain(synthesis.translateRNA(queueOf(rna)));
    }

    private CharQueue queueOf(String text) {
        CharQueue queue = new CharQueue();
        for (char value : text.toCharArray()) {
            queue.enqueue(value);
        }
        return queue;
    }

    private String drain(CharQueue queue) {
        StringBuilder result = new StringBuilder();
        while (!queue.isEmpty()) {
            result.append(queue.dequeue());
        }
        return result.toString();
    }
}
