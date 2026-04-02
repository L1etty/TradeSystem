package de.codingair.tradesystem.spigot.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnvilTitleSupportTest {
    @Test
    public void usesAnvilMenuTypeFieldOnPaperMojangMappedServers() {
        assertEquals("ANVIL", AnvilTitleSupport.getAnvilContainerField(true, 21.08));
    }

    @Test
    public void usesLegacyMappingsOnNonMojangMappedServers() {
        assertEquals("i", AnvilTitleSupport.getAnvilContainerField(false, 20.04));
        assertEquals("h", AnvilTitleSupport.getAnvilContainerField(false, 17.0));
        assertEquals("ANVIL", AnvilTitleSupport.getAnvilContainerField(false, 16.0));
    }
}
