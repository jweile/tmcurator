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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jweile
 */
public abstract class DBAccess<I,O> {
    
    private static final String DBFILE = 
            System.getProperty("ca.on.mshri.tmcurator.db","tmcurator.db");
    
    public abstract O transaction(Connection db, String user, I in);
    
    public O run(String user, I in) {
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot load SQLite JDBC driver.",ex);
        }
        
        Connection db = null;
        
        try {
            
            db = DriverManager.getConnection("jdbc:sqlite:"+DBFILE);
            
            return transaction(db, user, in);
            
            
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
    
}
