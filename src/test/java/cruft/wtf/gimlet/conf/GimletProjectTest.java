package cruft.wtf.gimlet.conf;

import org.junit.Test;

import static org.junit.Assert.*;

public class GimletProjectTest {

    @Test
    public void read() throws Exception {
        GimletProject gp = GimletProject.read(GimletProjectTest.class.getResourceAsStream("/project.xml"));
        assertNotNull(gp);
        assertNotNull(gp.getName());
        assertNotNull(gp.getDescription());
        assertEquals(2, gp.getAliases().size());
        assertEquals(1, gp.getQueries().size());
    }

}