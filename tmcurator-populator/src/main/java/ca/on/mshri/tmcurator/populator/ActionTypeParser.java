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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jweile
 */
public class ActionTypeParser {
    
    public List<String> parse() {
        
        InputStream in = ActionTypeParser.class.getClassLoader()
                .getResourceAsStream("action_types.csv");
        
        List<String> updates = new ArrayList<String>();
        
        BufferedReader b = null;
        try {
            b = new BufferedReader(new InputStreamReader(in));
            
            int lnum = 0;
            String[] colnames;
            String line = null;
            while ((line = b.readLine()) != null) {
                lnum++;
                
                if (line.trim().length() == 0) {
                    continue;
                }
                
                String[] cols = line.split("\t");
                
                if (lnum == 1) {
                    colnames = cols;
                    continue;
                }
                
                cols = adjust(cols);
                
                updates.add(String.format(
                        "INSERT INTO actiontypes VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');", (
                        Object[]) cols));
                
            }
            
            return updates;
            
        } catch (IOException ex) {
            throw new RuntimeException("Unable to read input stream",ex);
        } finally {
            try {
                b.close();
            } catch (IOException ex) {
                Logger.getLogger(ActionTypeParser.class.getName()).log(Level.SEVERE, 
                        "Unable to close Stream reader.", ex);
            }
        }
        
    }

    private String[] adjust(String[] cols) {
        String[] out = new String[8];
        
        int i = 0;
        for (; i < cols.length; i++) {
            String string = out[i];
            if (cols[i].length() == 0) {
                out[i] = "0";
            } else {
                out[i] = cols[i];
            }
        }
        for (; i < out.length; i++) {
            out[i] = "0";
        }
        
        return out;
    }
    
}
