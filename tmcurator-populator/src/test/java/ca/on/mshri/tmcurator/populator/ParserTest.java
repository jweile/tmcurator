/*
 *  Copyright (C) 2012 Jochen Weile, M.Sc. <jochenweile@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.on.mshri.tmcurator.populator;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author jweile
 */
public class ParserTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Populator.setupLogging();
    }
    
    
    
    public void testSentenceParsing() throws Exception {
        
        InputStream in = ParserTest.class.getClassLoader()
                .getResourceAsStream("annot_850295---850787.xml");
        
        SentenceParser p = new SentenceParser();
        UpdateCollection queries = p.parse(in);
        
        in.close();
        
        for (String qry : queries) {
            Logger.getAnonymousLogger().info(qry);
        }
        
    }
    
    public void testActionTypeParser() throws Exception {
        
        ActionTypeParser p = new ActionTypeParser();
        
        List<String> queries = p.parse();
        
        for (String qry : queries) {
            Logger.getAnonymousLogger().info(qry);
        }
        
    }
    
}
