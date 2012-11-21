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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class VerdictParser {

    /**
     * returns list of database update statements
     * @return 
     */
    public List<String> parse(String verdictFile) {
        
        BufferedReader r = null;
        
        try {
            
            r = new BufferedReader(new FileReader(verdictFile));
            
            List<String> list = new ArrayList<String>();
            
            Map<String,Integer> idx = new HashMap<String, Integer>();
            
            String line; int lnum = 0;
            while ((line = r.readLine()) != null) {
                lnum++;
                
                String[] cols = line.split("\\|");
                
                if (cols.length < 10) {
                    Logger.getLogger(VerdictParser.class.getName()).log(Level.WARNING, "Inconsistency in verdict file. Line #"+lnum);
                    continue;
                }
                
                //process title line
                if (lnum == 1) {
                    for (int i = 0; i < cols.length; i++) {
                        idx.put(cols[i], i);
                    }
                    continue;
                }
                
                //id TEXT PRIMARY KEY, pairId INTEGER, mentionId INTEGER, action TEXT, updown INTEGER, g1type TEXT, g2type TEXT, negative INTEGER, comment TEXT, user TEXT, timestamp DATETIME
                
                //process body of file
                StringBuilder b = new StringBuilder();
                b.append("INSERT INTO verdicts VALUES (");
                b.append("'").append(cols[idx.get("id")]).append("',");
                b.append("'").append(cols[idx.get("pairId")]).append("',");
                b.append("'").append(cols[idx.get("mentionId")]).append("',");
                b.append("'").append(cols[idx.get("action")]).append("',");
                b.append("'").append(cols[idx.get("updown")]).append("',");
                b.append("'").append(cols[idx.get("g1type")]).append("',");
                b.append("'").append(cols[idx.get("g2type")]).append("',");
                b.append("'").append(cols[idx.get("negative")]).append("',");
                b.append("'").append(cols[idx.get("comment")]).append("',");
                b.append("'").append(cols[idx.get("user")]).append("',");
                b.append("(SELECT datetime('now'))");
                b.append(");");
                
                list.add(b.toString());
                
            }
            
            return list;
            
        } catch (IOException e) {
            throw new RuntimeException("Unable to read verdict file "+verdictFile, e);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException ex) {
                    Logger.getLogger(VerdictParser.class.getName())
                            .log(Level.WARNING, "Unable to close file reader", ex);
                }
            }
        }
    }
    
}
