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
import ca.on.mshri.tmcurator.shared.Action;
import ca.on.mshri.tmcurator.shared.Effect;
import ca.on.mshri.tmcurator.shared.MentionVerdict;
import ca.on.mshri.tmcurator.shared.PairDataSheet;
import ca.on.mshri.tmcurator.shared.Verdict;
import ca.on.mshri.tmcurator.shared.VerdictSheet;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

//    private static final String DBFILE = 
//            System.getProperty("ca.on.mshri.tmcurator.db","tmcurator.db");
    
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
        
        return new DBAccess<Object, Double>() {

            @Override
            public Double transaction(Connection db, String user, Object in) {
                int curr = getProgress(db, user, Inc.CURR);
                int tot = getTotalPairNum(db);

                double progress = (double)curr / (double)tot;

                return progress;
            }
        }.run(user, null);
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
    
    public void saveVerdicts(String user, VerdictSheet sheet) {
        new DBAccess<VerdictSheet, Object>() {

            @Override
            public Object transaction(Connection db, String user, VerdictSheet in) {
                try {
                    _saveVerdicts(db, user, in);
                } catch (SQLException ex) {
                    throw new RuntimeException("Unable to query database!", ex);
                }
                return null;
            }

        }.run(user, sheet);
    }
    
    private void _saveVerdicts(Connection db, String user, VerdictSheet sheet) throws SQLException {
        
        Statement sql = db.createStatement();
        
        for (Verdict verdict : sheet.getVerdicts()) {
            
            int mentionId = verdict instanceof MentionVerdict ? 
                    ((MentionVerdict)verdict).getMentionId() : -1;
            
            String id = makeVerdictId(user, verdict.getPairId(), mentionId);
            
            ResultSet result = sql.executeQuery(
                    "SELECT COUNT(*) FROM verdicts WHERE id='"+id+"';");
            result.next();
            assert(result.getInt(1) <= 1);
            boolean exists = result.getInt(1) > 0;
            
            if (exists) {
                sql.executeUpdate(String.format(
                        "UPDATE verdicts SET action='%s', updown='%s', g1type='%s', g2type='%s' WHERE id='%s';",
                        verdict.getAction(),
                        verdict.getOrder(),
                        verdict.getG1Type(),
                        verdict.getG2Type(),
                        id));
            } else {
                sql.executeUpdate(String.format("INSERT INTO verdicts VALUES ('%s','%s','%s','%s','%s','%s','%s','%s');",
                        id,
                        verdict.getPairId(),
                        mentionId,
                        verdict.getAction(),
                        verdict.getOrder(),
                        verdict.getG1Type(),
                        verdict.getG2Type(),
                        user));
            }
        }
    }
    
    private String makeVerdictId(String user, int pairId, int mentionId) {
        return new StringBuilder()
                    .append(user)
                    .append("_")
                    .append(pairId)
                    .append("_")
                    .append(mentionId)
                    .toString();
    }
    
    private PairDataSheet queryPairData(String user, Inc inc) {
          
        return new DBAccess<Inc, PairDataSheet>() {

            @Override
            public PairDataSheet transaction(Connection db, String user, Inc inc) {
                int currPairId = getProgress(db,user,inc);
                int totPairNum = getTotalPairNum(db);

                if (currPairId <= totPairNum) {
                    PairDataSheet s = new PairDataSheet();

                    s.setPairNumber(currPairId);
                    s.setTotalPairNumber(totPairNum);

                    obtainPairInfo(currPairId, s,db);

                    s.setMentions(obtainMentions(currPairId, db));

                    return s;
                } else {
                    //FIXME: make sure frontend accounts for EOL case
                    return null;
                }
            }
        }.run(user, inc);
            
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

    private static final String mentionQuery = new StringBuilder()
            .append("SELECT pmid, sentence, citation, type1, type2, upstream, ")
            .append("downstream, actionType, updown, effect, close_connection ")
            .append("FROM pairs,mentions,articles,actiontypes ")
            .append("WHERE mentions.pair_id=pairs.id ")
            .append("AND mentions.actionType=actiontypes.name ")
            .append("AND mentions.article_id=articles.id ")
            .append("AND pairs.ROWID='%s';")
            .toString();
    
    private List<Map<String, String>> obtainMentions(int pairNum, Connection db) {
        List<Map<String,String>> list = new ArrayList<Map<String, String>>();
        try {
            Statement qry = db.createStatement();
            ResultSet result = qry.executeQuery(String.format(mentionQuery,pairNum));
            
            ResultSetMetaData rsmd = result.getMetaData();
            
            while (result.next()) {
                Map<String,String> map = new HashMap<String, String>();
                
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String cname = rsmd.getColumnName(i);
                    map.put(cname, result.getString(cname));
                }
                
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

    @Override
    public List<Action> getActions() {
            
        return new DBAccess<Object, List<Action>>() {

            @Override
            public List<Action> transaction(Connection db, String user, Object in) {
                return makeActionList(db);
            }
        }.run(null, null);
            
    }

    
    private List<Action> makeActionList(Connection db) {
        try {
            
            List<Action> list = new ArrayList<Action>();
            
            Statement qry = db.createStatement();
            ResultSet r = qry.executeQuery(
                    "SELECT name, parent, effect, close_connection FROM actiontypes;");
            
            while(r.next()) {
                String name = r.getString("name");
                String parent = r.getString("parent");
                int effect = r.getInt("effect");
                int close = r.getInt("close_connection");
                list.add(new Action(name,parent,Effect.fromInt(effect), close==1));
            }
            
            return list;
            
        } catch (SQLException ex) {
            throw new RuntimeException("Database query failed.", ex);
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
