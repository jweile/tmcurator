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
package ca.on.mshri.tmcurator.host;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jweile
 */
public class BackupService  {
    
    public void start(File dbfile) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Failed to load JDBC driver",ex);
        }
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        //perform backup every 24h
        scheduler.scheduleAtFixedRate(new Backup(dbfile), 1, 1, TimeUnit.DAYS);
    }
    
    static class Backup implements Runnable {
        
        private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        
        private File dbFile;
        
        Backup(File dbFile) {
            this.dbFile = dbFile;
        }

        @Override
        public void run() {
            
            File backupDir = new File("backup");
            
            if (!backupDir.exists()) {
                backupDir.mkdir();
            }
            
            File backupFile = new File(backupDir, "tmcurator_"+DF.format(new Date())+".db.bak");
            
            Connection db = null;
        
            try {

                db = DriverManager.getConnection("jdbc:sqlite:"+dbFile.getAbsolutePath());
                Statement sql = db.createStatement();
                sql.executeUpdate("backup to "+backupFile.getAbsolutePath());
                
            } catch (SQLException ex) {
                    Logger.getLogger(Backup.class.getName()).log(Level.SEVERE, 
                            "Backup failed!", ex);
            } finally {
                try {
                    db.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Backup.class.getName()).log(Level.SEVERE, 
                            "Unable to close database connection", ex);
                }
            }
            
            for (File oldBackupFile : backupDir.listFiles()) {
                //         v9                 v28
                //tmcurator_2012-01-01_12:00:00.db.bak
                String dateStr = oldBackupFile.getName().substring(10, 29);
                try {
                    Date bckDate = DF.parse(dateStr);
                    if (bckDate != null && olderThan4Weeks(bckDate)) {
                        oldBackupFile.delete();
                    }
                } catch (ParseException e) {
                    Logger.getLogger(BackupService.class.getName())
                        .log(Level.SEVERE, "Date could not be recognized from backup file name!", e);
                }
            }
            
        }

        private boolean olderThan4Weeks(Date bckDate) {
            long week = TimeUnit.MILLISECONDS.convert(4 * 7, TimeUnit.DAYS);
            return System.currentTimeMillis() - bckDate.getTime() > week;
        }
        
    }
    
}
