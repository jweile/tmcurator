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

import ca.on.mshri.tmcurator.client.LoginService;
import ca.on.mshri.tmcurator.shared.Config;
import ca.on.mshri.tmcurator.shared.LoginException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jweile
 */
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {

    @Override
    public boolean addUser(String user, String pwd) throws LoginException {
        user = escapeString(user);
        pwd = escapeString(pwd);
        
        return new DBAccess<String, Boolean>() {

            @Override
            public Boolean transaction(Connection db, String user, String pwd) {
                if (hasUser(db,user)) {
                    throw new LoginException("User already exists!");
                }
                return _addUser(db, user, pwd);
            }


        }.run(user, pwd);
    }

    @Override
    public boolean deleteUser(String user, String pwd) throws LoginException {
        user = escapeString(user);
        pwd = escapeString(pwd);
        return new DBAccess<String, Boolean>() {

            @Override
            public Boolean transaction(Connection db, String user, String pwd) {
                if (!hasUser(db,user)) {
                    throw new LoginException("Unknown username!");
                }
                if (!verifyUser(db, user, pwd)) {
                    throw new LoginException("Wrong password!");
                }
                return _deleteUser(db, user, pwd);
            }
        }.run(user, pwd);
    }

    @Override
    public boolean login(String user, String pwd) throws LoginException {
        user = escapeString(user);
        pwd = escapeString(pwd);
        return new DBAccess<String, Boolean>() {

            @Override
            public Boolean transaction(Connection db, String user, String pwd) {
                if (!hasUser(db,user)) {
                    throw new LoginException("Unknown username!");
                }
                return verifyUser(db, user, pwd);
            }

        }.run(user, pwd);
    }
    
    /**
     * Prevent SQL injection and cross-site scripting hacks.
     * @param s
     * @return 
     */
    private String escapeString(String s) {
        if (s == null) {
            return null;
        } else {
            return s.replaceAll("'", "''").replaceAll("&", "&amp;")
                    .replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
    }
    
    
    private boolean hasUser(Connection db, String user) {
        try {
            Statement sql = db.createStatement();
            ResultSet result = sql.executeQuery(
                    "SELECT COUNT(*) FROM users WHERE name='"
                    + user
                    + "';");
            result.next();
            boolean hasUser = result.getInt(1) > 0;
            sql.close();
            return hasUser;
        } catch (SQLException ex) {
            throw new RuntimeException("DB Query failed.");
        }
    }
    
    
    private boolean _addUser(Connection db, String user, String pwd) {
        try {
            Statement sql = db.createStatement();
            sql.executeUpdate(String.format(
                    "INSERT INTO users VALUES ('%s','',(SELECT last_offset+offset FROM config),'%s',(SELECT last_offset+offset FROM config));",
                    user,
                    pwd));
            sql.executeUpdate("UPDATE config SET last_offset=last_offset+offset;");
            sql.close();
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException("DB Query failed.");
        }
    }
    
    
    private boolean verifyUser(Connection db, String user, String pwd) {
         try {
            Statement sql = db.createStatement();
            ResultSet result = sql.executeQuery(
                    "SELECT password FROM users WHERE name='"
                    + user
                    + "';");
            result.next();
            boolean verified = result.getString("password").equals(pwd);
            sql.close();
            return verified;
        } catch (SQLException ex) {
            throw new RuntimeException("DB Query failed.");
        }
    }
    
    
    private boolean _deleteUser(Connection db, String user, String pwd) {
        try {
            Statement sql = db.createStatement();
            sql.executeUpdate(String.format(
                    "DELETE FROM users WHERE name='%s' AND password='%s';",
                    user,
                    pwd));
            //TODO: Also delete all of users verdicts?
            sql.close();
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException("DB Query failed.");
        }
    }

    @Override
    public Config getConfig() throws Exception {
        return new DBAccess<Void, Config>() {

            @Override
            public Config transaction(Connection db, String user, Void v) {
                return _getConfig(db);
            }

        }.run(null,null);
    }
    
    
    private Config _getConfig(Connection db) {
        try {
            Config config = new Config();
            Statement sql = db.createStatement();
            ResultSet result = sql.executeQuery("SELECT * FROM config;");
            result.next();
            
            config.setQuota(result.getInt("quota"));
            config.setOffset(result.getInt("offset"));
            config.setApprovalEnabled(result.getBoolean("approvalEnabled"));
            
            sql.close();
            return config;
        } catch (SQLException ex) {
            throw new RuntimeException("DB Query failed.");
        }
    }

    @Override
    public void setConfig(Config config) throws Exception {
        
        new DBAccess<Config,Void>() {

            @Override
            public Void transaction(Connection db, String user, Config in) {
                _setConfig(db, in);
                return null;
            }
            
        }.run(null,config);
    }
    

    private void _setConfig(Connection db, Config config) {
        try {
            Statement sql = db.createStatement();
            
            ResultSet result = sql.executeQuery("SELECT * FROM config;");
            result.next();
            
            if (result.getInt("last_offset") < 1) {
                
                sql.executeUpdate(String.format(
                    "UPDATE config SET quota='%s', offset='%s', approvalEnabled='%s', last_offset='%s';",
                    config.getQuota(),
                    config.getOffset(),
                    config.isApprovalEnabled(),
                    1-config.getOffset()
                ));
                
            } else {
            
                sql.executeUpdate(String.format(
                        "UPDATE config SET quota='%s', offset='%s', approvalEnabled='%s';",
                        config.getQuota(),
                        config.getOffset(),
                        config.isApprovalEnabled()
                ));
            }
            sql.close();
        } catch (SQLException ex) {
            throw new RuntimeException("DB Query failed.");
        }
    }
    
    @Override
    public int assignNewContingent(String user) throws Exception {
        return new DBAccess<Void,Integer>() {

            @Override
            public Integer transaction(Connection db, String user, Void in) {
                return _assignNewContingent(db,user);
            }

            
        }.run(user,null);
    }
    
    
    private int _assignNewContingent(Connection db, String user) {
        Statement sql = null;
        try {
            sql = db.createStatement();
            
            sql.executeUpdate(String.format(
                    "UPDATE users SET start=(SELECT last_offset+offset FROM config) WHERE name='%s';",
                    user));
            
            ResultSet r = sql.executeQuery(String.format(
                    "SELECT start FROM users WHERE name='%s';",user));
            r.next();
            return r.getInt(1);
            
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed",e);
        } finally {
            try {
                sql.close();
            } catch (SQLException ex) {
                Logger.getLogger(LoginServiceImpl.class.getName())
                        .log(Level.WARNING, "Unable to close DB statement", ex);
            }
        }
    }
}
