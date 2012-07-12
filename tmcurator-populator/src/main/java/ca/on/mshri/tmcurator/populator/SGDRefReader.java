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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class SGDRefReader {

    HashSet<String> read() {
        
        HashSet<String> pmids = new HashSet<String>();
        
        InputStream in = null;
        
        try {
            
            in = SGDRefReader.class.getClassLoader().getResourceAsStream("sgd_references.tsv");

            if (in == null) {
                File inFile = new File(new File(new File(new File("src"),"main"),"resources"),"sgd_references.tsv");
                in = new FileInputStream(inFile);
            }
            
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line; int lnum = 0;
            
            while ((line = r.readLine()) != null) {
                
                //skip first line
                if (++lnum < 2) continue;
                
                String[] cols = line.split("\t");
                
                //make sure line has value in first column
                if (cols.length < 1) {
                    Logger.getLogger(SGDRefReader.class.getName())
                            .log(Level.WARNING, "Invalid line: "+lnum);
                    continue;
                }
                
                //test if correct id format
                try {
                    Integer.parseInt(cols[0]);
                } catch (NumberFormatException e) {
                    Logger.getLogger(SGDRefReader.class.getName())
                            .log(Level.WARNING, "Invalid ID format in line: "+lnum);
                    continue;
                }
                
                pmids.add(cols[0]);
            }
            
        } catch (IOException e) {
            
            throw new RuntimeException("Error parsing SGD references file!",e);
            
        } finally {
            
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(SGDRefReader.class.getName())
                            .log(Level.SEVERE, "Unable to cose stream!", ex);
                }
            }
        }
        
        return pmids;
        
    }
    
}
