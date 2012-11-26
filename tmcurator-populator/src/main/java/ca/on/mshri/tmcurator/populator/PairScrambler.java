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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class PairScrambler {
    
    public void scramble(Connection db) {
        
        //get number of gene pairs
        int nPairs;
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT COUNT(*) FROM pairs;");
            r.next();
            nPairs = r.getInt(1);
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to read pair table", ex);
        }
        
        //create lookup for new random indices (old->new)
        List<Integer> scrIdx = new ArrayList<Integer>();
        scrIdx.add(0);
        for (int i = 1; i <= nPairs; i++) {
            scrIdx.add(i);
        }
        Collections.shuffle(scrIdx);
        
//        //create reverse lookup table (new->old)
//        int[] revScrIdx = new int[nPairs+1];
//        for (int i = 1; i <= nPairs; i++) {
//            revScrIdx[scrIdx.get(i)] = i;
//        }
        
        //get all pair rows
        List<String> pairRows = new ArrayList<String>(nPairs);
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT * FROM pairs;");
            while (r.next()) {
                StringBuilder b = new StringBuilder("INSERT INTO pairs VALUES (");
                b.append("'").append(r.getString("id")).append("', ");
                b.append("'").append(r.getString("g1id")).append("', ");
                b.append("'").append(r.getString("g2id")).append("', ");
                b.append("'").append(r.getString("g1sym")).append("', ");
                b.append("'").append(r.getString("g2sym")).append("');");
                pairRows.add(b.toString());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to read pair table", ex);
        }
        
        //delete old pair table
        try {
            Statement s = db.createStatement();
            s.executeUpdate("DELETE FROM pairs;");
            db.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to delete pair table", ex);
        }
        
        //rewrite pair table in new order
        try {
            Statement s = db.createStatement();
            for (int i : scrIdx) {
                if (i == 0) {
                    continue;
                }
                s.executeUpdate(pairRows.get(i-1));
            }
            db.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to read pair table", ex);
        }
        
        //correct verdicts table, that has been affected
        //(mentions table luckily uses proper foreign keys)
        
        List<String> verdictCorrections = new ArrayList<String>();
        String qry = "SELECT verdicts.id AS verdict_id, verdicts.pairId AS old_pair_id, "
                + "pairs.ROWID AS new_pair_id FROM verdicts, mentions, pairs "
                + "WHERE verdicts.mentionId=mentions.id AND mentions.pair_id=pairs.id;";
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery(qry);
            while (r.next()) {
                
                //dissemble id
                String[] parts = r.getString("verdict_id").split("_");
//                int oldId = Integer.parseInt(parts[1]);
                int newId = r.getInt("new_pair_id");
                
                //assemble correction
                StringBuilder b = new StringBuilder("UPDATE verdicts SET ");
                b.append("id='").append(parts[0]).append("_").append(newId)
                        .append("_").append(parts[2]).append("', ");
                b.append("pairId='").append(newId).append("' ");
                b.append("WHERE id='").append(r.getString("verdict_id")).append("';");
                
                verdictCorrections.add(b.toString());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to read verdict table", ex);
        }
        
        //perform corrections in verdict table 
        try {
            Statement s = db.createStatement();
            for (String row : verdictCorrections) {
                s.executeUpdate(row);
            }
            db.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to update verdicts", ex);
        }
        
    }
    
}
