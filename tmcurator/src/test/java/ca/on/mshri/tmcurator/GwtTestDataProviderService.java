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
package ca.on.mshri.tmcurator;

import ca.on.mshri.tmcurator.client.DataProviderServiceAsync;
import ca.on.mshri.tmcurator.shared.PairDataSheet;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class GwtTestDataProviderService extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "ca.on.mshri.tmcurator.GwtTestDataProviderService";
    }
    
    public void testDataProviderService() {
        
        DataProviderServiceAsync service = DataProviderServiceAsync.Util.getInstance();
        
        delayTestFinish(10000);
        
        service.nextPairSheet("user", new AsyncCallback<PairDataSheet>() {

            @Override
            public void onFailure(Throwable caught) {
                fail(caught.getMessage());
            }

            @Override
            public void onSuccess(PairDataSheet result) {
                assertNotNull("Null result",result);
                assertNotNull("Null mentions",result.getMentions());
                assertTrue("Empty mention list",result.getMentions().size() > 0);
                for (Map<String,String> mention : result.getMentions()) {
                    assertTrue("Empty mention", mention.size() > 0);
                }
                
                printResult(result);
                
                finishTest();
            }

        });
        
    }
    
    
    private void printResult(PairDataSheet result) {
        StringBuilder b = new StringBuilder();
        b.append("Pair #")
                .append(result.getPairNumber())
                .append(" of ")
                .append(result.getTotalPairNumber())
                .append("\n")
                .append(result.getG1Sym())
                .append(" & ")
                .append(result.getG2Sym())
                .append(":\n\n");
        for (Map<String,String> map : result.getMentions()) {
            b.append("PMID: ")
                .append(map.get("pmid"))
                .append("\n")
                .append(map.get("sentence"))
                .append("\n\n");
        }
        System.err.println(b.toString());
    }
    
}
