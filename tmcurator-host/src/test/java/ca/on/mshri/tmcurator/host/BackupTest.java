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

import ca.on.mshri.tmcurator.host.BackupService.Backup;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author jweile
 */
public class BackupTest extends TestCase {
    
    private File dbFile = new File(new File(new File(new File("src"),"test"),"resources"),"test.db");
    private File bakDir = new File("backup");
    
    public void testBackupScheduling() {
        new BackupService().start(dbFile);
    }
    
    public void testBackup() throws InterruptedException {
        Backup bak = new Backup(dbFile);
        bak.run();
        
        assertTrue("No backup directory", bakDir.exists());
        assertTrue("No backup file", bakDir.list().length > 0);
    }
    
    public void testFileCopy() throws Exception {
        File outFile = new File(bakDir,"test.bak");
        if (outFile.exists()) {
            outFile.delete();
        }
        
        Backup bak = new Backup(dbFile);
        bak.filecopy(dbFile, outFile);
        
        assertTrue(outFile.exists());
        assertTrue(outFile.length() == dbFile.length());
        outFile.delete();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (bakDir.exists()) {
            bakDir.delete();
        }
    }
    
    
    
}
