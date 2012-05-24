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

import ca.on.mshri.tmcurator.shared.PairDataSheet;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class CurationPanel extends BorderLayoutContainer {

    private static CurationPanel instance = null;
    
    private HTML titleBar;
    
    private VBoxLayoutContainer interpretationPanel;
    
    private VerdictControls verdictControls;
    
    
    private CurationPanel() {
        
        setHeight(600);
        
        BorderLayoutData borderData = new BorderLayoutData(50);
        borderData.setMargins(new Margins(10));
        
        titleBar = new HTML();
        setNorthWidget(titleBar,borderData);
        
        ScrollPanel scrollPanel = new ScrollPanel();
        VBoxLayoutContainer docPanel = new VBoxLayoutContainer();
        docPanel.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
        docPanel.setHeight(1000);
        docPanel.setStyleName("forcewhite", true);
        
        docPanel.add(new HTML("<b>Sentence interpretations</b>"),BoxConfig.MARGIN);
        
        ContentPanel interpretationFrame = new ContentPanel();
        interpretationFrame.setHeaderVisible(false);
        
        interpretationPanel = new VBoxLayoutContainer();
        interpretationPanel.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
        
        interpretationFrame.add(interpretationPanel);
        docPanel.add(interpretationFrame, BoxConfig.MARGIN);
        
        docPanel.add(new HTML("<br/><b>Verdict</b>"),BoxConfig.MARGIN);
        
        verdictControls = new VerdictControls();
        ContentPanel verdictControlBox = new ContentPanel();
        verdictControlBox.setHeight(100);
        verdictControlBox.add(verdictControls);
        docPanel.add(verdictControlBox, BoxConfig.MARGIN);
        
        docPanel.add(makeButtonPanel(), BoxConfig.MARGIN);
        
        scrollPanel.add(docPanel);
        setCenterWidget(scrollPanel);
    }
    
    public static CurationPanel getInstance() {
        if (instance == null) {
            instance = new CurationPanel();
        }
        return instance;
    }

    public void updatePairData(PairDataSheet pData) {
        
        StringBuilder b = new StringBuilder();
        b.append("<span style=\"font-size: small;\">Gene pair #")
                .append(pData.getPairNumber())
                .append(" of ")
                .append(pData.getTotalPairNumber())
                .append(":</span><br/><span class=\"sym1\">")
                .append("<a href=\"http://www.yeastgenome.org/cgi-bin/search/luceneQS.fpl?query=")
                .append(pData.getG1Sym())
                .append("\" target=\"_blank\">")
                .append(pData.getG1Sym())
                .append("</a></span> & <span class=\"sym2\">")
                .append("<a href=\"http://www.yeastgenome.org/cgi-bin/search/luceneQS.fpl?query=")
                .append(pData.getG2Sym())
                .append("\" target=\"_blank\">")
                .append(pData.getG2Sym())
                .append("</a></span>");
        
        titleBar.setHTML(b.toString());
        
        interpretationPanel.clear();
        
        for (Map<String,String> mention : pData.getMentions()) {
            
            MentionContainer mentionContainer = new MentionContainer(mention,
                    pData.getG1Sym(), pData.getG2Sym());
            interpretationPanel.add(mentionContainer, BoxConfig.MARGIN);
            
        }
        
        int height = pData.getMentions().size() * 210;
        
        interpretationPanel.setHeight(height);
        
        ContentPanel interpretationFrame = ((ContentPanel)interpretationPanel.getParent());
        interpretationFrame.setHeight(height);
        
        VBoxLayoutContainer docPanel = (VBoxLayoutContainer)interpretationFrame.getParent();
        docPanel.setHeight(height+300);
        
        verdictControls.setGenePair(pData.getG1Sym(), pData.getG2Sym());
        
        forceLayout();
    }


    private IsWidget makeButtonPanel() {
        HBoxLayoutContainer container = new HBoxLayoutContainer();
        container.setHeight(50);
        container.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.STRETCH);
        container.setPack(BoxLayoutPack.CENTER);
        
        //TODO: implement saving of progress
        container.add(new TextButton("< Previous", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                
                TmCurator.LOAD_DIALOG.show();
        
                DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
                dataService.prevPairSheet(TmCurator.MOCK_USER, new AsyncCallback<PairDataSheet>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        TmCurator.LOAD_DIALOG.hide();
                        displayError(caught);
                    }

                    @Override
                    public void onSuccess(PairDataSheet result) {

                        TmCurator.LOAD_DIALOG.hide();
                        CurationPanel.this.updatePairData(result);

                    }

                });
            }
            
        }), BoxConfig.MARGIN);
        
        container.add(new TextButton("Home", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                TmCurator.getInstance().loadGreetingPanel();
            }
            
        }), BoxConfig.MARGIN);
        
        container.add(new TextButton("Next >", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                
                TmCurator.LOAD_DIALOG.show();
        
                DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
                dataService.nextPairSheet(TmCurator.MOCK_USER, new AsyncCallback<PairDataSheet>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        TmCurator.LOAD_DIALOG.hide();
                        displayError(caught);
                    }

                    @Override
                    public void onSuccess(PairDataSheet result) {

                        TmCurator.LOAD_DIALOG.hide();
                        CurationPanel.this.updatePairData(result);

                    }

                });
            }
            
        }), BoxConfig.MARGIN);
        
        return container;
    }
    
    
    private void displayError(Throwable caught) {
        
        AlertMessageBox b = new AlertMessageBox("Error",caught.getMessage());
        RootPanel.get().add(b);
        b.show();
    }
    
    
}
