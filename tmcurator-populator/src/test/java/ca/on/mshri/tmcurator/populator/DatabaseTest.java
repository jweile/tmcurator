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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author jweile
 */
public class DatabaseTest extends TestCase {
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Populator.setupLogging();
    }
    
    public void testCreateTables() {
        
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
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        File dbfile = new File("tmcurator.db");
        dbfile.delete();
        
    }
    
}
