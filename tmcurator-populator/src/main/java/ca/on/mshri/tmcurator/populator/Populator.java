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
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author jweile
 */
public class Populator {
    
    public static void main(String[] args) {
        
        try {
            
            setupLogging();
            
            new Populator().run(args);
            
        } catch (Throwable t) {
            
            logProcessedError(t);
            System.exit(1);
        }
    }
    
    /**
     * 
     * @throws IOException 
     */
    public static void setupLogging() throws IOException {
        
        //setup CLI output
        for (Handler h : Logger.getLogger("").getHandlers()) {
            if (h instanceof ConsoleHandler) {
                ConsoleHandler ch = ((ConsoleHandler)h);
                ch.setLevel(Level.INFO);
                ch.setFormatter(new Formatter() {

                    @Override
                    public String format(LogRecord lr) {

                        StringBuilder b = new StringBuilder();

                        b.append(lr.getLevel().toString())
                                .append(": ");

                        b.append(lr.getMessage())
                                .append("\n");

                        return b.toString();
                    }

                });
                break;
            }
        }
        
        //setup log file writer
        File logFile = new File("tmcurator-populator.log");
        FileHandler fh = new FileHandler(logFile.getAbsolutePath());
        fh.setLevel(Level.ALL);
        fh.setFormatter(new SimpleFormatter());
        Logger.getLogger("").addHandler(fh);
        
    }
    
    /**
     * 
     * @param t 
     */
    private static void logProcessedError(Throwable t) {
        StringBuilder b = new StringBuilder(256);

        b.append(t.getMessage());
        Throwable cause = t;
        while ((cause = cause.getCause()) != null) {
            b.append("\nReason: ").append(cause.getMessage());
        }

        Logger.getLogger(Populator.class.getName()).log(Level.SEVERE, b.toString());
        
        Logger.getLogger(Populator.class.getName()).log(Level.SEVERE, t.getMessage(), t);
    }
    

    private void run(String[] args) {
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot load SQLite JDBC driver.",ex);
        }
        
        Connection db = null;
        
        try {
            
            db = DriverManager.getConnection("jdbc:sqlite:tmcurator.db");
            db.setAutoCommit(false);
            
            DBInit dbinit = new DBInit();
            
            dbinit.createTables(db);
            
            dbinit.readActionTypes(db);
            
            List<File> dirs = new ArrayList<File>();
            for (String filename : args) {
                dirs.add(new File(filename));
            }
            dbinit.readSentences(db, dirs);
            
            dbinit.createIndices(db);
            
            dbinit.createTestUser(db);
            
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot connect to database!",ex);
        } finally {
            try {
                db.close();
            } catch (SQLException ex) {
                Logger.getLogger(Populator.class.getName()).log(Level.SEVERE, 
                        "Unable to close database connection", ex);
            }
        }
        
    }
    
}
