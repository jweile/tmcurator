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

import ca.on.mshri.tmcurator.client.DataProviderService;
import ca.on.mshri.tmcurator.shared.PairDataSheet;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jweile
 */
public class DataProviderServiceImpl extends RemoteServiceServlet 
                                        implements DataProviderService {

    //FIXME: Find a way to configure DB location.
    private static final String DBFILE="/Users/jweile/tmp/tmcurator.db";
    
    @Override
    public PairDataSheet nextPairSheet(String user) {
        return queryPairData(user,Inc.NEXT);
    }
    
    
    @Override
    public PairDataSheet currPairSheet(String user) {
        return queryPairData(user,Inc.CURR);
    }

    @Override
    public PairDataSheet prevPairSheet(String user) {
        return queryPairData(user,Inc.PREV);
    }

    @Override
    public double currProgress(String user) {
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot load SQLite JDBC driver.",ex);
        }
        
        Connection db = null;
        
        try {
            
            db = DriverManager.getConnection("jdbc:sqlite:"+DBFILE);
            
            int curr = getProgress(db, user, Inc.CURR);
            int tot = getTotalPairNum(db);
            
            double progress = (double)curr / (double)tot;
            
            return progress;
            
            
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot connect to database!",ex);
        } finally {
            try {
                db.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataProviderServiceImpl.class.getName()).log(Level.SEVERE, 
                        "Unable to close database connection", ex);
            }
        }
    }
    

    private int totalPairNum = -1;
    
    private int getTotalPairNum(Connection db) {
        if (totalPairNum < 0) {
            try {
                Statement qry = db.createStatement();
                ResultSet result = qry.executeQuery("SELECT COUNT(*) FROM pairs;");
                result.next();
                totalPairNum = result.getInt(1);
                return totalPairNum;
            } catch (SQLException e) {
                throw new RuntimeException("Unable to query database!",e);
            }
        } else {
            return totalPairNum;
        }
    }
    
    
    private PairDataSheet queryPairData(String user, Inc inc) {
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot load SQLite JDBC driver.",ex);
        }
        
        Connection db = null;
        
        try {
            
            db = DriverManager.getConnection("jdbc:sqlite:"+DBFILE);
            
            
            int pairNum = getProgress(db,user,inc);
            int totPairNum = getTotalPairNum(db);
            
            if (pairNum <= totPairNum) {
                PairDataSheet s = new PairDataSheet();

                s.setPairNumber(pairNum);
                s.setTotalPairNumber(totPairNum);

                obtainPairInfo(pairNum, s,db);

                s.setMentions(obtainMentions(pairNum, db));

                return s;
            } else {
                //FIXME: make sure frontend accounts for EOL case
                return null;
            }
            
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot connect to database!",ex);
        } finally {
            try {
                db.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataProviderServiceImpl.class.getName()).log(Level.SEVERE, 
                        "Unable to close database connection", ex);
            }
        }
    }

    private void obtainPairInfo(int pairNum, PairDataSheet s, Connection db) {
        try {
            Statement qry = db.createStatement();
            ResultSet result = qry.executeQuery(
                    String.format("SELECT g1sym, g2sym FROM pairs WHERE ROWID=%s;",
                    pairNum));
            result.next();
            s.setG1Sym(result.getString("g1sym"));
            s.setG2Sym(result.getString("g2sym"));
            
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot query database.",ex);
        }
    }

    private List<Map<String, String>> obtainMentions(int pairNum, Connection db) {
        List<Map<String,String>> list = new ArrayList<Map<String, String>>();
        try {
            Statement qry = db.createStatement();
            ResultSet result = qry.executeQuery(
                    String.format("SELECT pmid,sentence "
                    + "FROM pairs,mentions,articles "
                    + "WHERE mentions.pair_id=pairs.id "
                    + "AND mentions.article_id=articles.id "
                    + "AND pairs.ROWID='%s';",
                    pairNum));
            
            while (result.next()) {
                Map<String,String> map = new HashMap<String, String>();
                
                map.put("pmid",result.getString("pmid"));
                map.put("sentence", result.getString("sentence"));
                
                list.add(map);
            }
            
            return list;
            
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot query database.",ex);
        }
    }

    private int getProgress(Connection db, String user, Inc inc) {
        
        try {
            Statement qry = db.createStatement();
            
            if (inc != Inc.CURR) {
                qry.executeUpdate(String.format(
                        "UPDATE users SET current=current%s WHERE name='%s';",
                        inc.mod(),
                        user));
            }
            
            ResultSet result = qry.executeQuery(
                    String.format("SELECT current FROM users WHERE name='%s';",
                    user));
            
            result.next();
            return result.getInt("current");
            
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot query database.",ex);
        }
    }

    private enum Inc {
        NEXT("+1"),CURR(""),PREV("-1");
        private String modifier;
        private Inc(String s) {
            modifier = s;
        }
        public String mod(){
            return modifier;
        }
    }
    
    
}
