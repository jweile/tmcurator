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

import com.google.gwt.user.client.ui.HTML; 
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ProgressBar;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

/**
 *
 * @author jweile
 */
public class GreetingPanel extends VBoxLayoutContainer {

    private static GreetingPanel instance = null;
    
    private int pairNum, totalPairNum;
    
    private HTML greeting;

    private ProgressBar progressbar;
    /**
     * Get the value of instance
     *
     * @return the value of instance
     */
    public static GreetingPanel getInstance() {
        if (instance == null) {
            instance = new GreetingPanel();
        }
        return instance;
    }

    
    private GreetingPanel() {
        
        setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.CENTER);
        setPack(BoxLayoutPack.CENTER);
        
        BoxLayoutData layout = new BoxLayoutData(new Margins(0,0,10,0));
        
        greeting = new HTML("Hello "+TmCurator.getInstance().getUser()+"!");
        add(greeting,layout);
        
        progressbar = new ProgressBar();
        progressbar.updateProgress(0.0, "Curation progress: 0%");
        add(progressbar, layout);
                
        TextButton continueButton = new TextButton("Continue");
        continueButton.addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                TmCurator.getInstance().loadCurationPanel();
            }

        });
        
        TextButton jumpButton = new TextButton("Jump to pair", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                //TODO get actual number from database
                JumpToPairDialog.getInstance(totalPairNum).show(pairNum);
            }
        });
        HBoxLayoutContainer bb = new HBoxLayoutContainer();
        bb.setSize("150px", "30px");
        bb.add(continueButton, BoxConfig.FLEX_MARGIN);
        bb.add(jumpButton, BoxConfig.FLEX_MARGIN);
        add(bb, layout);
        
    }
    
    public void setProgress(int curr, int total, int filled, int quota) {
        
        this.pairNum = curr;
        this.totalPairNum = total;
        
        double share = (double) filled / (double) quota;
        
        progressbar.updateProgress(share, "Contingent fulfilment: "+filled+" of "+quota);
    }
    
    
    public void updateGreeting() {
        greeting.setHTML("Hello "+TmCurator.getInstance().getUser()+"!");
    }
    
}
