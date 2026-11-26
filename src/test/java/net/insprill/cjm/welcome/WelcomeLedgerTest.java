package net.insprill.cjm.welcome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WelcomeLedgerTest {
    @TempDir Path directory;
    @Test void durableIntentSurvivesRestartWithoutSuccessfulReceipt() throws Exception {
        Path file=directory.resolve("rewards.journal");
        UUID newcomer=UUID.randomUUID(), who=UUID.randomUUID();
        LocalDate today=LocalDate.of(2026,11,24);
        WelcomeLedger first=new WelcomeLedger(file);
        assertTrue(first.open(newcomer));
        assertTrue(first.reserve(newcomer,who,today,10,100));
        WelcomeLedger restarted=new WelcomeLedger(file);
        assertFalse(restarted.open(newcomer));
        assertFalse(restarted.reserve(newcomer,who,today,10,100));
        assertFalse(restarted.reserve(newcomer,newcomer,today,10,100));
    }
    @Test void eachWelcomerCanClaimButDailyCapAndUnknownNewcomersAreRejected() throws Exception {
        WelcomeLedger ledger=new WelcomeLedger(directory.resolve("rewards.journal"));
        UUID a=UUID.randomUUID(),b=UUID.randomUUID(),who=UUID.randomUUID(),other=UUID.randomUUID();
        LocalDate today=LocalDate.of(2026,11,24);
        assertFalse(ledger.reserve(a,who,today,1,100));
        ledger.open(a); ledger.open(b);
        assertTrue(ledger.reserve(a,who,today,1,100));
        ledger.finish(a,who,true);
        assertTrue(ledger.reserve(a,other,today,1,100));
        assertFalse(ledger.reserve(b,who,today,1,100));
        assertTrue(ledger.reserve(b,who,today.plusDays(1),1,100));
    }
    @Test void corruptJournalFailsClosed() throws Exception {
        Path file=directory.resolve("rewards.journal");
        Files.writeString(file,"P|truncated\n");
        assertThrows(java.io.IOException.class,()->new WelcomeLedger(file));
    }
}
