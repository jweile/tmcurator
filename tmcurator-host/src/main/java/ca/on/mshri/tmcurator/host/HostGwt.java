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
package ca.on.mshri.tmcurator.host;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

/**
 *
 * @author jweile
 */
public class HostGwt {
    
    private static final Logger LOG = Logger.getLogger(HostGwt.class.getName());
    
    private File dbfile;
    
    private static HostGwt instance;

    public static HostGwt getInstance() {
        if (instance == null) {
            instance = new HostGwt();
        }
        return instance;
    }

    private HostGwt() {
        //use getInstance()
    }
    
    
    
    public static void main(String[] args) throws Throwable {
        
        try {
            
            setupLogging();
           
            getInstance().run();
        
        } catch (Throwable t) {
            logProcessedError(t);
            Logger.getLogger(HostGwt.class.getName()).log(Level.FINE, t.getMessage(), t);
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
        File logFile = new File("tmcurator-host.log");
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

        LOG.log(Level.SEVERE, b.toString());
    }

    private void checkDatabase() {
        
        String dbpath = System.getProperty("ca.on.mshri.tmcurator.db","tmcurator.db");
        dbfile = new File(dbpath);
        if (!(dbfile.exists() && dbfile.canRead())) {
            throw new RuntimeException("Unable to read database at location "+dbpath+
                    "\nSet system property ca.on.mshri.tmcurator.dbca.on.mshri.tmcurator.db");
        }
    }

    private void run() throws Exception {
        
        checkDatabase();
        
        int port = Integer.parseInt(System.getProperty("ca.on.mshri.tmcurator.port", "8081"));
        Server server = new Server(port);
        
        LOG.info("Scheduling periodic backup service...");
        new BackupService().start(dbfile);

        LOG.info("Creating context...");
        WebAppContext handler = new WebAppContext();
        handler.setContextPath("/");
        handler.setWar("tmcurator-0.1.0-SNAPSHOT.war");

        LOG.info("Adding context...");
        server.setHandler(handler);
        server.setThreadPool(new QueuedThreadPool(20));

        LOG.info("Starting server...");
        server.start();
        server.join();
    }

    public File getDbfile() {
        return dbfile;
    }
    
    
    
}
