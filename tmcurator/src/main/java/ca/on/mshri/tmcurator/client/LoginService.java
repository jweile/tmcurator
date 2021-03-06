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
package ca.on.mshri.tmcurator.client;

import ca.on.mshri.tmcurator.shared.Config;
import ca.on.mshri.tmcurator.shared.LoginException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 *
 * @author jweile
 */
@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
    
    boolean addUser(String user, String pwd) throws LoginException;
    
    boolean deleteUser(String user, String pwd) throws LoginException;
    
    boolean login(String user, String pwd) throws LoginException;
    
    Config getConfig() throws Exception;
    
    void setConfig(Config config) throws Exception;
    
    int assignNewContingent(String user) throws Exception;
    
}
