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

import ca.on.mshri.tmcurator.shared.MentionVerdict;
import ca.on.mshri.tmcurator.shared.PairDataSheet;
import ca.on.mshri.tmcurator.shared.Verdict;
import ca.on.mshri.tmcurator.shared.VerdictSheet;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class CurationPanel extends BorderLayoutContainer {

    private static CurationPanel instance = null;
    
    private HTML titleBar;
    
    private VBoxLayoutContainer interpretationPanel;
    
//    private VerdictControls verdictControls;
    
    private int pairId;
    private String g1Sym, g2Sym;
    
    private TextButton nextButton, prevButton;
    
    private ScrollPanel scrollPanel;
    
    
    private CurationPanel() {
        
        setHeight(600);
        
        BorderLayoutData borderData = new BorderLayoutData(50);
        borderData.setMargins(new Margins(20));
        
        titleBar = new HTML();
        setNorthWidget(titleBar,borderData);
        
        scrollPanel = new ScrollPanel();
        VBoxLayoutContainer docPanel = new VBoxLayoutContainer();
        docPanel.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
        docPanel.setHeight(1000);
        docPanel.setStyleName("forcewhite", true);
        
        docPanel.add(new HTML("<b>Sentence interpretations</b>"),BoxConfig.MARGIN);
        
        docPanel.add(makeAddSentenceButtonPanel(), BoxConfig.MARGIN);
        
        ContentPanel interpretationFrame = new ContentPanel();
        interpretationFrame.setHeaderVisible(false);
        
        interpretationPanel = new VBoxLayoutContainer();
        interpretationPanel.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
        
        interpretationFrame.add(interpretationPanel);
        docPanel.add(interpretationFrame, BoxConfig.MARGIN);
        
//        docPanel.add(new HTML("<br/><b>Verdict</b>"),BoxConfig.MARGIN);
//        
//        verdictControls = new VerdictControls();
//        ContentPanel verdictControlBox = new ContentPanel();
//        verdictControlBox.setHeight(100);
//        verdictControlBox.add(verdictControls);
//        docPanel.add(verdictControlBox, BoxConfig.MARGIN);
        
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
        
        this.pairId = pData.getPairNumber();
        this.g1Sym = pData.getG1Sym();
        this.g2Sym = pData.getG2Sym();
        
        StringBuilder b = new StringBuilder();
        b.append("<span style=\"font-size: small;\">Gene pair #")
                .append(pData.getPairNumber())
                .append(" of ")
                .append(pData.getTotalPairNumber())
                .append(":</span><br/><span class=\"sym1\">")
                .append("<a href=\"http://www.yeastgenome.org/cgi-bin/search/luceneQS.fpl?query=")
                .append(g1Sym)
                .append("\" target=\"_blank\">")
                .append(g1Sym.toUpperCase())
                .append("</a></span> & <span class=\"sym2\">")
                .append("<a href=\"http://www.yeastgenome.org/cgi-bin/search/luceneQS.fpl?query=")
                .append(g2Sym)
                .append("\" target=\"_blank\">")
                .append(g2Sym.toUpperCase())
                .append("</a></span>");
        
        if (pData.getNumVerdicts() > 0) {
            b.append("<br/><span style=\"font-size: small;color:red;\">Existing curations: ")
                    .append(pData.getNumVerdicts())
                    .append("</span>");
        }
        
        titleBar.setHTML(b.toString());
        
//        verdictControls.setGenePair(pData.getG1Sym(), pData.getG2Sym());
        
        interpretationPanel.clear();
        
        int minusVerdict = 0;
        for (Map<String,String> mention : pData.getMentions()) {
            
            if (mention.get("mentionId").equals("-1")) {
                //then it's a verdict
//                verdictControls.configure(mention, pData.getG1Sym(), pData.getG2Sym());
                minusVerdict = 1;
            } else {
                //it's a mention
                MentionContainer mentionContainer = new MentionContainer(mention,
                        g1Sym, g2Sym, pairId);
                interpretationPanel.add(mentionContainer, BoxConfig.MARGIN);
            }
            
        }
        
        int h;
        
        if (!pData.getMentions().isEmpty()) {
            h = (pData.getMentions().size() - minusVerdict) * 210;
        } else {
            h=100;
            interpretationPanel.add(new HTML("No sentences are associated with this pair. Please click 'Next'."));
        }
        
        interpretationPanel.setHeight(h);
        ContentPanel interpretationFrame = ((ContentPanel)interpretationPanel.getParent());
        interpretationFrame.setHeight(h);
        
        VBoxLayoutContainer docPanel = (VBoxLayoutContainer)interpretationFrame.getParent();
        docPanel.setHeight(h+300);
        
        //disable next/previous buttons if not available
        prevButton.setEnabled(pData.getPairNumber() > 1);
        nextButton.setEnabled(pData.getPairNumber() < pData.getTotalPairNumber());
        
        forceLayout();
        scrollPanel.scrollToTop();
        
        
        //check if quota is fulfilled
        checkQuota();
    }

    private VerdictSheet extractData() {
        List<Verdict> verdicts = new ArrayList<Verdict>();
        for (IsWidget w : interpretationPanel) {
            if (w instanceof MentionContainer) {
                MentionContainer mention = (MentionContainer)w;
                if (mention.isApproved() || !TmCurator.getInstance().getConfig().isApprovalEnabled()) {
                    MentionVerdict verdict = mention.extractData();
                    verdicts.add(verdict);
                }
            }
        }
        
//        Verdict finalVerdict = new Verdict(pairId, 
//                verdictControls.getAction().getName(), 
//                verdictControls.getOrder().mod(), 
//                verdictControls.getG1Type().name(), 
//                verdictControls.getG2Type().name(),
//                verdictControls.isNegative(),
//                verdictControls.getComment());
//        finalVerdict.setInvalid(verdictControls.isInvalid());
//        verdicts.add(finalVerdict);
        
        VerdictSheet sheet = new VerdictSheet(pairId);
        sheet.setVerdicts(verdicts);
        return sheet;
    }

    private IsWidget makeButtonPanel() {
        HBoxLayoutContainer container = new HBoxLayoutContainer();
        container.setHeight(50);
        container.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.STRETCH);
        container.setPack(BoxLayoutPack.CENTER);
        
        prevButton = new TextButton("< Previous", new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                saveThenLoadPrevious();
            }
        });
        
        container.add(prevButton, BoxConfig.MARGIN);
        
        container.add(new TextButton("Home", new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                saveThenGreet();
            }
        }), BoxConfig.MARGIN);
        
        nextButton = new TextButton("Next >", new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                saveThenLoadNext();
            }
        });
        container.add(nextButton, BoxConfig.MARGIN);
        
        return container;
    }
    
    
    private void saveThenLoadPrevious() {
        TmCurator.LOAD_DIALOG.show();
        
        VerdictSheet sheet = extractData();
        
        final DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
        
        dataService.saveVerdicts(TmCurator.getInstance().getUser(), sheet, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                displayError(caught);
            }

            @Override
            public void onSuccess(Void result) {
                dataService.prevPairSheet(TmCurator.getInstance().getUser(), new AsyncCallback<PairDataSheet>() {

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
        });
    }
    
    private void saveThenLoadNext() {
        TmCurator.LOAD_DIALOG.show();
        
        VerdictSheet sheet = extractData();
        
        final DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
        
        dataService.saveVerdicts(TmCurator.getInstance().getUser(), 
                sheet, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                displayError(caught);
            }

            @Override
            public void onSuccess(Void result) {
                dataService.nextPairSheet(TmCurator.getInstance().getUser(), 
                        new AsyncCallback<PairDataSheet>() {

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
        });
    }
    
    private void saveThenGreet() {
        TmCurator.LOAD_DIALOG.show();
        
        VerdictSheet sheet = extractData();
        
        DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
        
        dataService.saveVerdicts(TmCurator.getInstance().getUser(), 
                sheet, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                displayError(caught);
            }

            @Override
            public void onSuccess(Void result) {
                TmCurator.LOAD_DIALOG.hide();
                TmCurator.getInstance().loadGreetingPanel();
            }
        });
    }
    
    
    private void displayError(Throwable caught) {
        
        AlertMessageBox b = new AlertMessageBox("Error",caught.getMessage());
        RootPanel.get().add(b);
        b.show();
    }

    void addSentence(String sentence, String pmid) {
        
        TmCurator.LOAD_DIALOG.show();
        
        Map<String,String> sentenceData = new HashMap<String, String>();
        sentenceData.put("sentence", sentence);
        sentenceData.put("pmid", pmid);
        sentenceData.put("g1Sym", g1Sym);
        sentenceData.put("g2Sym", g2Sym);
        sentenceData.put("pairId",pairId+"");
        
        final DataProviderServiceAsync serv = DataProviderServiceAsync.Util.getInstance();
        serv.addSentence(sentenceData, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                displayError(caught);
            }

            @Override
            public void onSuccess(Void result) {
                serv.currPairSheet(TmCurator.getInstance().getUser(), 
                        new AsyncCallback<PairDataSheet>() {

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
        });
        
        
    }

    private IsWidget makeAddSentenceButtonPanel() {
        
        HBoxLayoutContainer container = new HBoxLayoutContainer();
        container.setHeight(30);
        container.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.MIDDLE);
        container.setPack(BoxLayoutPack.END);
        
        container.add(new TextButton("Add sentence", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                AddSentenceDialog.getInstance().show();
            }
        }), new BoxLayoutData(new Margins(0, 5, 0, 0)));
        
        return container;
        
    }

    private void checkQuota() {
        DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
        
        dataService.currProgress(TmCurator.getInstance().getUser(), new AsyncCallback<int[]>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                displayError(caught);
            }

            @Override
            public void onSuccess(int[] result) {
                TmCurator.LOAD_DIALOG.hide();
                
                int curr = result[0],//current pair for user
                    total = result[1],//total number pairs in dataset
                    filled = result[2],//user's quota fulfilment
                    quota = result[3];//user's total quota
                                
                if (filled >= quota) {
                    showCongrats();
                }

            }

            

        });
    }
    
    /**
     * Show dialog on fulfilled quota.
     */
    private void showCongrats() {
        
        Dialog congrats = new Dialog();
        congrats.setHeadingText("Congratulations!");
        congrats.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
        congrats.add(new Label(
                "You have fulfilled your quota!\n"
                + "Would you like to be assigned a new contingent?"));
        congrats.setHideOnButtonClick(true);
        congrats.setWidth(300);
        
        congrats.getButtonById(PredefinedButton.YES.name()).addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                TmCurator.LOAD_DIALOG.show();
                
                //update contingent in user table
                LoginServiceAsync.Util.getInstance().assignNewContingent(TmCurator.getInstance().getUser(), new AsyncCallback<Integer>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        TmCurator.LOAD_DIALOG.hide();
                        displayError(caught);
                    }

                    @Override
                    public void onSuccess(Integer location) {
                        //jump to new location
                        DataProviderServiceAsync.Util.getInstance().gotoPairSheet(TmCurator.getInstance().getUser(), location, new AsyncCallback<PairDataSheet>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                TmCurator.LOAD_DIALOG.hide();
                                displayError(caught);
                            }

                            @Override
                            public void onSuccess(PairDataSheet result) {
                                TmCurator.LOAD_DIALOG.hide();
                                updatePairData(result);
                            }
                        });
                    }
                });
                
                
            }
        });
        
        congrats.show();
    }
    
}
