/*
 * Copyright (C) 2012 Department of Molecular Genetics, University of Toronto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.on.mshri.tmcurator.server;

import java.util.Map;
import junit.framework.TestCase;
import ca.on.mshri.tmcurator.shared.PairDataSheet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jweile
 */
public class DataProviderServiceImplTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("ca.on.mshri.tmcurator.db","src/test/resources/tmcurator.db");
    }
    
    
    /**
     * Test of nextPairSheet method, of class DataProviderServiceImpl.
     */
    @Test
    public void testNextPairSheet() {
        System.out.println("nextPairSheet");
        String user = "user";
        DataProviderServiceImpl instance = new DataProviderServiceImpl();
        PairDataSheet result = instance.nextPairSheet(user);
        
        
        assertNotNull("Null result",result);
        assertNotNull("Null mentions",result.getMentions());
        assertTrue("Empty mention list",result.getMentions().size() > 0);
        
        for (Map<String,String> mention : result.getMentions()) {
            assertTrue("Empty mention", mention.size() > 0);
        }

        printResult(result);
        
    }
    
    
    private void printResult(PairDataSheet result) {
        StringBuilder b = new StringBuilder();
        b.append("Pair #")
                .append(result.getPairNumber())
                .append(" of ")
                .append(result.getTotalPairNumber())
                .append("\n")
                .append(result.getG1Sym())
                .append(" & ")
                .append(result.getG2Sym())
                .append(":\n\n");
        for (Map<String,String> map : result.getMentions()) {
            b.append("PMID: ")
                .append(map.get("pmid"))
                .append("\n")
                .append(map.get("sentence"))
                .append("\n\n");
        }
        System.out.println(b.toString());
    }
}
