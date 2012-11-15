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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author jweile
 */
public class DBInit {
    
    private static final Logger LOG = Logger.getLogger(DBInit.class.getName());

    public void createTables(Connection db) {
        
        LOG.info("Creating database tables.");
        
        try {
            
            Statement statement = db.createStatement();
            
            statement.executeUpdate("CREATE TABLE pairs (id TEXT PRIMARY KEY, "
                    + "g1id TEXT, g2id TEXT, g1sym TEXT, g2sym TEXT);");
            statement.executeUpdate("CREATE TABLE articles "
                    + "(id INTEGER PRIMARY KEY, pmid INTEGER, citation TEXT, non_sgd INTEGER);");
            statement.executeUpdate("CREATE TABLE mentions "
                    + "(id INTEGER PRIMARY KEY, pair_id TEXT, article_id INTEGER, "
                    + "actionType TEXT, upstream TEXT, downstream TEXT, "
                    + "type1 TEXT, type2 TEXT, sentence TEXT, negative INTEGER, "
                    + "score REAL );");
            statement.executeUpdate("CREATE TABLE actiontypes "
                    + "(name TEXT PRIMARY KEY , pl TEXT, parent TEXT, nbgp INTEGER,"
                    + "updown INTEGER, effect INTEGER, same_process INTEGER, "
                    + "close_connection INTEGER);");
            statement.executeUpdate("CREATE TABLE users (name TEXT PRIMARY KEY, "
                    + "token TEXT, current INTEGER, password TEXT, start INTEGER);");
            statement.executeUpdate("CREATE TABLE verdicts (id TEXT PRIMARY KEY, "
                    + "pairId INTEGER, mentionId INTEGER, action TEXT, "
                    + "updown INTEGER, g1type TEXT, g2type TEXT, negative INTEGER, "
                    + "comment TEXT, user TEXT);");
            statement.executeUpdate("CREATE TABLE config (offset INTEGER, "
                    + "last_offset INTEGER, quota INTEGER);");
            
            db.commit();
            
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to create tables in database", ex);
        }
        
        
    }
    
    
    public void setupConfigData(Connection db) {
        try {
            Statement s = db.createStatement();
            
            s.executeUpdate("INSERT INTO config VALUES (100, -99, 300);");
            db.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to set up configuration parameters", ex);
        }
    }
    
    public void createTestUser(Connection db) {
        try {
            Statement s = db.createStatement();
            
            s.executeUpdate("INSERT INTO users VALUES ('user', '', 201, 'foo', '201');");
            db.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to create test user", ex);
        }
        
    }
    
    public void readSentences(Connection db, List<File> sentenceDirs) {
        
        List<File> sentenceFiles = new ArrayList<File>();
        
        for (File sentenceDir : sentenceDirs) {
            if (!sentenceDir.exists()) {
                throw new RuntimeException(sentenceDir.getName()+ "does not exist!");
            }

            if (!sentenceDir.isDirectory()) {
                throw new RuntimeException(sentenceDir.getName()+ "is not a directory!");
            }

            File[] files = sentenceDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File file, String string) {
                    return string.endsWith(".xml.gz");
                }

            });
            
            sentenceFiles.addAll(Arrays.asList(files));
        }
        
        SentenceParser parser = new SentenceParser();
        UpdateCollection updates = new UpdateCollection();
        
        for (File sentenceFile : sentenceFiles) {
            
            LOG.info("Reading file: "+sentenceFile);
            
            InputStream in = null;
            try {
                
                in = new GZIPInputStream(new FileInputStream(sentenceFile));
                updates.addAll(parser.parse(in));
                
            } catch (IOException ex) {
                Logger.getLogger(DBInit.class.getName()).log(Level.WARNING, 
                        "Unable to read file "+sentenceFile.getName(), ex);
            } catch (Exception ex) {
                Logger.getLogger(DBInit.class.getName()).log(Level.WARNING, 
                        "Error reading file "+sentenceFile.getName(), ex);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(DBInit.class.getName()).log(Level.SEVERE, 
                            "Unable to close input stream!", ex);
                }
            }
            
        }
        
        
        LOG.info("Writing to database.");
        
        try {
            Statement statement = db.createStatement();
            for (String update : updates) {
                try {
                    statement.executeUpdate(update);
                } catch (Exception e) {
                    throw new RuntimeException("Failed update: "+update, e);
                }
            }
            db.commit();
            
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to write to database", ex);
        }
    }
    
    
    public void createIndices(Connection db) {
        //TODO: write indexing function for database
    }

    public void readActionTypes(Connection db) {
        
        LOG.info("Reading action types.");
        
        ActionTypeParser p = new ActionTypeParser();
        
        List<String> updates = p.parse();
        
        LOG.info("Writing to database.");
        
        try {
            Statement statement = db.createStatement();
            for (String update : updates) {
                statement.executeUpdate(update);
            }
            db.commit();
            
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to write to database", ex);
        }
    }
    
}
