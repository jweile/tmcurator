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
import ca.on.mshri.tmcurator.shared.GenePair;
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

    private static final Logger LOG = Logger.getLogger(DataProviderServiceImpl.class.getName());
    private static final String QRY_FAIL_MSG = "DB Query failed!";
    
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
    public PairDataSheet gotoPairSheet(String user, int pairNum) {
        return queryPairData(user, pairNum);
    }

    /**
     * Returns an integer array with exactly four elements
     * <ol>
     * <li> ID of gene pair the user has last curated</li>
     * <li> Total number of gene pairs</li>
     * <li> Number of pairs from the users quota for which he has already submitted verdicts.</li>
     * <li> The user's quota</li>
     * </ol>
     */
    @Override
    public int[] currProgress(String user) {
        
        return new DBAccess<Void, int[]>() {

            @Override
            public int[] transaction(Connection db, String user, Void in) {
                int curr = getProgress(db, user, Inc.CURR);
                int tot = getTotalPairNum(db);
//                int filled = getNumVerdicts(db, user);
                int filled = getFulfilment(db, user);
                int quota = getQuota(db);

                return new int[]{curr,tot,filled,quota};
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
                qry.close();
                return totalPairNum;
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, QRY_FAIL_MSG,e);
                throw new RuntimeException(QRY_FAIL_MSG,e);
            }
        } else {
            return totalPairNum;
        }
    }
    
    
    private int getNumVerdicts(Connection db, String user) {
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT COUNT(DISTINCT pairId) FROM verdicts WHERE user='"+user+"';");
            r.next();
            return r.getInt(1);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
    }
    
    private int getFulfilment(Connection db, String user) {
        try {
            Statement s = db.createStatement();
            //get all pairs from interval [start,start+quota] for which user has submitted verdicts or that have no sentences.
            ResultSet r = s.executeQuery(String.format("SELECT COUNT(*)  FROM pairs  WHERE "
                    + "(pairs.ROWID BETWEEN (SELECT start FROM users WHERE name='%s') "
                    + "AND (SELECT start FROM users WHERE name='%s') + (SELECT quota FROM config)) "
                    + "AND (pairs.ROWID IN (SELECT DISTINCT pairId FROM verdicts WHERE user='%s') "
                    + "OR pairs.ROWID IN (SELECT ROWID FROM pairs "
                    + "WHERE id NOT IN(SELECT DISTINCT pair_id FROM mentions))); ",
                    user,user,user));
            r.next();
            return r.getInt(1);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
    }
    
    private int getQuota(Connection db) {
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery("SELECT quota FROM config;");
            r.next();
            return r.getInt(1);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
    }
    
    @Override
    public void saveVerdicts(String user, VerdictSheet sheet) {
        new DBAccess<VerdictSheet, Void>() {

            @Override
            public Void transaction(Connection db, String user, VerdictSheet in) {
                try {
                    _saveVerdicts(db, user, in);
                } catch (SQLException ex) {
                    LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
                    throw new RuntimeException(QRY_FAIL_MSG,ex);
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
            result.close();
            
            String comment = verdict.getComment() == null ? "" : verdict.getComment();
            
            if (exists) {
                sql.executeUpdate(String.format(
                        "UPDATE verdicts SET action='%s', updown='%s', g1type='%s', g2type='%s', negative='%s', comment='%s', timestamp=datetime('now') WHERE id='%s';",
                        verdict.getAction(),
                        verdict.getOrder(),
                        verdict.getG1Type(),
                        verdict.getG2Type(),
                        verdict.isInvalid() ? 2 : (verdict.isNegative()?1:0),
                        verdict.getComment(),
                        id));
            } else {
                sql.executeUpdate(String.format("INSERT INTO verdicts "
                        + "(id, pairId, mentionId, action, updown, g1type, g2type, negative, comment, user, timestamp) "
                        + "VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',datetime('now'));",
                        id,
                        verdict.getPairId(),
                        mentionId,
                        verdict.getAction(),
                        verdict.getOrder(),
                        verdict.getG1Type(),
                        verdict.getG2Type(),
                        verdict.isInvalid() ? 2 : (verdict.isNegative()?1:0),//int value
                        verdict.getComment(),
                        user
                        ));
            }
            sql.close();
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
                    s.setNumVerdicts(getNumVerdicts(db, currPairId));

                    obtainPairInfo(currPairId, s,db);

                    s.setMentions(obtainMentions(currPairId, db, user));

                    return s;
                } else {
                    //FIXME: make sure frontend accounts for EOL case
                    return null;
                }
            }
        }.run(user, inc);
            
    }
    
    private PairDataSheet queryPairData(String user, int pairNum) {
          
        return new DBAccess<Integer, PairDataSheet>() {

            @Override
            public PairDataSheet transaction(Connection db, String user, Integer currPairId) {
                int totPairNum = getTotalPairNum(db);

                if (currPairId <= totPairNum) {
                    setPairId(db, user, currPairId);
                    
                    PairDataSheet s = new PairDataSheet();

                    s.setPairNumber(currPairId);
                    s.setTotalPairNumber(totPairNum);

                    obtainPairInfo(currPairId, s,db);

                    s.setMentions(obtainMentions(currPairId, db, user));

                    return s;
                } else {
                    //FIXME: make sure frontend accounts for EOL case
                    return null;
                }
            }
        }.run(user, pairNum);
            
    }

    private void setPairId(Connection db, String user, Integer currPairId) {
        try {
            Statement s = db.createStatement();
            s.executeUpdate("UPDATE users SET current='"+currPairId+"' WHERE name='"+user+"';");
            s.close();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
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
            qry.close();
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
    }

    private static final String mentionQuery = new StringBuilder()
            .append("SELECT mentions.ROWID AS mentionId, pmid, sentence, citation, non_sgd, type1, type2, upstream, ")
            .append("downstream, actionType, updown, effect, close_connection, negative ")
            .append("FROM pairs,mentions,articles,actiontypes ")
            .append("WHERE mentions.pair_id=pairs.id ")
            .append("AND mentions.actionType=actiontypes.name ")
            .append("AND mentions.article_id=articles.id ")
            .append("AND pairs.ROWID='%s';")
            .toString();
    private static final String mentionAltQuery = new StringBuilder()
            .append("SELECT mentions.ROWID AS mentionId, sentence, type1, type2, upstream, ")
            .append("downstream, actionType, updown, effect, close_connection, negative ")
            .append("FROM pairs,mentions,actiontypes ")
            .append("WHERE mentions.pair_id=pairs.id ")
            .append("AND mentions.actionType=actiontypes.name ")
            .append("AND pairs.ROWID='%s';")
            .toString();
    
    private List<Map<String, String>> obtainMentions(int pairId, Connection db, String user) {
        List<Map<String,String>> list = new ArrayList<Map<String, String>>();
        try {
            Statement qry = db.createStatement();
            ResultSet result = qry.executeQuery(String.format(mentionQuery,pairId));
            
            ResultSetMetaData rsmd = result.getMetaData();
            
            while (result.next()) {
                Map<String,String> map = new HashMap<String, String>();
                
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String cname = rsmd.getColumnName(i);
                    map.put(cname, result.getString(cname));
                }
                
                int mentionId = Integer.parseInt(map.get("mentionId"));
                updateMentionWithUserChanges(db, map, user, pairId, mentionId);
                
                list.add(map);
            }
            
            //if there is no article id... :/
            if (list.isEmpty()) {
                result = qry.executeQuery(String.format(mentionAltQuery,pairId));
            
                rsmd = result.getMetaData();

                while (result.next()) {
                    Map<String,String> map = new HashMap<String, String>();

                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        String cname = rsmd.getColumnName(i);
                        map.put(cname, result.getString(cname));
                    }

                    int mentionId = Integer.parseInt(map.get("mentionId"));
                    updateMentionWithUserChanges(db, map, user, pairId, mentionId);

                    list.add(map);
                }
            }
            
            qry.close();
            
            //load verdict
            Map<String,String> verdict = new HashMap<String,String>();
            updateMentionWithUserChanges(db, verdict, user, pairId, -1);
            if (verdict.size() > 0) {
                configureVerdict(db,verdict);
                list.add(verdict);
            }
            
            return list;
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
    }
    
    
    private void configureVerdict(Connection db, Map<String, String> verdict) {
        
        verdict.put("mentionId","-1");
        verdict.put("upstream","");
        try {
            Statement sql = db.createStatement();
            ResultSet result = sql.executeQuery("SELECT effect, close_connection FROM actiontypes WHERE name='"
                    + verdict.get("actionType")
                    + "';");
            result.next();
            
            verdict.put("effect",result.getString("effect"));
            verdict.put("close_connection",result.getString("close_connection"));
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
        
    }
    
    
    private void updateMentionWithUserChanges(Connection db, Map<String, String> map, String user, int pairId, int mentionId) {
        
        try {
            
            String verdictId = makeVerdictId(user, pairId, mentionId);
            
            Statement sql = db.createStatement();
            
            ResultSet result = sql.executeQuery(
                    "SELECT COUNT(*) FROM verdicts WHERE id='"+verdictId+"';");
            result.next();
            int rCount = result.getInt(1);
            result.close();
            
            assert(rCount <= 1);
            
            if (rCount == 1) {
            
                result = sql.executeQuery(
                        "SELECT * FROM verdicts WHERE id='" + verdictId + "';");
                result.next();
                
                map.put("actionType", result.getString("action"));
                map.put("updown", result.getString("updown"));
                map.put("type1", result.getString("g1type"));
                map.put("type2", result.getString("g2type"));
                map.put("negative", result.getString("negative"));
                map.put("comment", result.getString("comment"));
                map.put("hasVerdict", "1");
                
                result.close();
            }
            
            sql.close();
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
        
    }
    
    private int getNumVerdicts(Connection db, int currPairId) {
        try {
            Statement s = db.createStatement();
            ResultSet r = s.executeQuery(
                    "SELECT COUNT(DISTINCT user) FROM verdicts WHERE pairId='"
                    +currPairId+"';");
            r.next();
            return r.getInt(1);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
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
            int current = result.getInt("current");
            qry.close();
            return current;
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
    }

    @Override
    public List<Action> getActions() {
            
        return new DBAccess<Void, List<Action>>() {

            @Override
            public List<Action> transaction(Connection db, String user, Void in) {
                return makeActionList(db);
            }
        }.run(null, null);
            
    }

    
    private List<Action> makeActionList(Connection db) {
        try {
            
            List<Action> list = new ArrayList<Action>();
            
            Statement qry = db.createStatement();
            ResultSet r = qry.executeQuery(
                    "SELECT name, parent, effect, close_connection, updown FROM actiontypes;");
            
            while(r.next()) {
                String name = r.getString("name");
                String parent = r.getString("parent");
                int effect = r.getInt("effect");
                int close = r.getInt("close_connection");
                int updown = r.getInt("updown");
                list.add(new Action(name,parent,Effect.fromInt(effect), close==1, updown != 0));
            }
            
            qry.close();
            
            return list;
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
        
    }
    
    @Override
    public List<Action> getTopActions() {
        
        return new DBAccess<Void, List<Action>>() {

            @Override
            public List<Action> transaction(Connection db, String user, Void in) {
                return _getTopActions(db);
            }

        }.run(null, null);
    }

    private static final String top10query = new StringBuilder()
            .append("SELECT name, parent, effect, close_connection, updown ")
            .append("FROM actiontypes WHERE name IN (SELECT action FROM ")
            .append("(SELECT action, COUNT(*) AS freq FROM verdicts ")
            .append("GROUP BY action ORDER BY freq DESC LIMIT 10)) ORDER BY name;")
            .toString();
    
    private List<Action> _getTopActions(Connection db) {
        
        List<Action> actions = new ArrayList<Action>();
        Statement qry = null;
        try {
            
            qry = db.createStatement();
            ResultSet r = qry.executeQuery(top10query);
            
            while(r.next()) {
                String name = r.getString("name");
                int effect = r.getInt("effect");
                int close = r.getInt("close_connection");
                int updown = r.getInt("updown");
                actions.add(new Action(name,"DECOY",Effect.fromInt(effect), close==1, updown != 0));
            }
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        } finally {
            if (qry != null) {
                try {
                    qry.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DataProviderServiceImpl.class.getName())
                            .log(Level.WARNING, "Failed to close statement.", ex);
                }
            }
        }
        
        return actions;
        
    }
    
    @Override
    public List<GenePair> findPairs(String qry) {
        return new DBAccess<String, List<GenePair>>() {
            @Override
            public List<GenePair> transaction(Connection db, String user, String in) {
                return _findPairs(db,in);
            }
        }.run(null, qry);
    }

    private List<GenePair> _findPairs(Connection db, String in) {
        if (in != null) {
            in = in.replaceAll("'", "''");
        }
        List<GenePair> list = new ArrayList<GenePair>();
        try {
            Statement sql = db.createStatement();
            ResultSet r = sql.executeQuery(
                    "SELECT ROWID, g1sym, g2sym FROM pairs "
                    + "WHERE g1sym LIKE '%"+in+"%' OR g1sym LIKE '%"+in+"%';");
            while (r.next()) {
                GenePair p = new GenePair(r.getInt("ROWID"), 
                        r.getString("g1sym"), r.getString("g2sym"));
                list.add(p);
            }
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        }
        return list;
    }

    
    @Override
    public void addSentence(Map<String, String> sentenceData) throws Exception {
        
        final String articleId = getArticleId(sentenceData.get("pmid"));
        
        if (articleId == null) {
            throw new Exception("Unknown PMID!");
        }
        
        new DBAccess<Map<String,String>, Void>() {

            @Override
            public Void transaction(Connection db, String user, Map<String, String> sentenceData) {
                _addSentence(db, sentenceData, articleId);
                return null;
            }

        }.run(null, sentenceData);
        
    }
    
    private static final String insertSentence = new StringBuilder()
            .append("INSERT INTO mentions VALUES (")
            .append("(SELECT MAX(id) FROM mentions)+1, ")
            .append("(SELECT id FROM pairs WHERE ROWID='%s'), ")
            .append("%s, 'actiontype', '%s', '%s', ")
            .append("'UNKNOWN', 'UNKNOWN', '%s', '0', '-1.0');")
            .toString();
    
    private void _addSentence(Connection db, Map<String, String> sentenceData, String articleId) {
                
        String q = String.format(insertSentence, 
            sentenceData.get("pairId"),
            articleId,
            sentenceData.get("g1Sym"),
            sentenceData.get("g2Sym"),
            sentenceData.get("sentence")
        );
        
        Statement s = null;
        try {
            
            s = db.createStatement();
            s.executeUpdate(q);
            
        } catch (SQLException ex) {
            
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
            
        } finally {
            
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DataProviderServiceImpl.class.getName())
                            .log(Level.WARNING, "Unable to close connection", ex);
                }
            }
        }
        
    }

    private String getArticleId(String pmidStr) {
        int pmid;
        try {
            pmid = Integer.parseInt(pmidStr);
        } catch (NumberFormatException nfe) {
            return null;
        }
        
        return new DBAccess<Integer, String>() {

            @Override
            public String transaction(Connection db, String user, Integer pmid) {
                return _getArticleId(db, pmid);
            }

        }.run(null, pmid);
    }
    
    private String _getArticleId(Connection db, Integer pmid) {
        Statement s = null;
        try {
            s = db.createStatement();
            
            ResultSet r = s.executeQuery(String
                    .format("SELECT COUNT(*) FROM articles WHERE pmid='%s';",pmid));
            r.next();
            
            if (r.getInt(1) > 0) {
                
                r = s.executeQuery(String
                        .format("SELECT id FROM articles WHERE pmid='%s';",pmid));
                r.next();
                return r.getString(1);
                
            } else {
                return null;
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, QRY_FAIL_MSG,ex);
            throw new RuntimeException(QRY_FAIL_MSG,ex);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DataProviderServiceImpl.class.getName())
                            .log(Level.WARNING, "Unable to close connection!", ex);
                }
            }
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
