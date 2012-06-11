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

import ca.on.mshri.tmcurator.shared.Action;
import ca.on.mshri.tmcurator.shared.GenePair;
import ca.on.mshri.tmcurator.shared.PairDataSheet;
import ca.on.mshri.tmcurator.shared.VerdictSheet;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

/**
 *
 * @author jweile
 */
//FIXME: This path doesn't work with the async-autogeneration.
@RemoteServiceRelativePath("data")
public interface DataProviderService extends RemoteService {
    
    PairDataSheet nextPairSheet(String user);
    
    PairDataSheet currPairSheet(String user);
    
    PairDataSheet prevPairSheet(String user);
    
    PairDataSheet gotoPairSheet(String user, int pairNum);
    
    int[] currProgress(String user);
    
    List<Action> getActions();
    
    void saveVerdicts(String user, VerdictSheet sheet);
    
    List<GenePair> findPairs(String qry);
    
}
