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

import ca.on.mshri.tmcurator.shared.Config;
import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class LoginServiceImplTest extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("ca.on.mshri.tmcurator.db","src/test/resources/tmcurator.db");
    }
    
    public void testConfig() throws Exception {
        
        LoginServiceImpl service = new LoginServiceImpl();
        
        Config config = new Config();
        config.setApprovalEnabled(false);
        config.setOffset(2);
        config.setQuota(4);
        
        service.setConfig(config);
        Config config2 = service.getConfig();
        
        assertEquals(config.getQuota(), config2.getQuota());
        assertEquals(config.getOffset(), config2.getOffset());
        assertEquals(config.isApprovalEnabled(), config2.isApprovalEnabled());
        
        
        config.setApprovalEnabled(true);
        config.setOffset(150);
        config.setQuota(300);
        
        service.setConfig(config);
        config2 = service.getConfig();
        
        assertEquals(config.getQuota(), config2.getQuota());
        assertEquals(config.getOffset(), config2.getOffset());
        assertEquals(config.isApprovalEnabled(), config2.isApprovalEnabled());
        
    }
        
}
