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
package ca.on.mshri.tmcurator.populator;

import java.util.HashSet;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class SGDRefReaderTest extends TestCase {
    
    
    public void test() {
        
        SGDRefReader reader = new SGDRefReader();
        
        HashSet<String> refs = reader.read();
        
        assertNotNull(refs);
        assertFalse(refs.isEmpty());
        
        System.out.println(refs.size()+" PMIDs obtained from file.");
        
        //Note: there are apparently 142 entries in the file that don't have a pmid.
        
//        for (String ref : refs) {
//            System.out.println(ref);
//        }
    }
    
}
