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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class CurationPanel extends BorderLayoutContainer {

    private static CurationPanel instance = null;
    
    private HTML titleBar;
    
    private VBoxLayoutContainer interpretationPanel;
    
    private static final BoxLayoutData margin = new BoxLayoutData(new Margins(5,5,5,5));
    
    private static final BoxLayoutData flexMargin = new BoxLayoutData(new Margins(5,5,5,5)){{
        setFlex(1);
    }};
    
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
        
        docPanel.add(new HTML("<b>Sentence interpretations</b>"),margin);
        
        ContentPanel interpretationFrame = new ContentPanel();
        interpretationFrame.setHeaderVisible(false);
        
        interpretationPanel = new VBoxLayoutContainer();
        interpretationPanel.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
        
        interpretationFrame.add(interpretationPanel);
        docPanel.add(interpretationFrame, margin);
        
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
                .append("<a href=\"http://www.yeastgenome.org/cgi-bin/search/luceneQS.fpl?query=\"")
                .append(pData.getG1Sym())
                .append("\">")
                .append(pData.getG1Sym())
                .append("</a></span> & <span class=\"sym2\">")
                .append("<a href=\"http://www.yeastgenome.org/cgi-bin/search/luceneQS.fpl?query=\"")
                .append(pData.getG2Sym())
                .append("\">")
                .append(pData.getG2Sym())
                .append("</a></span>");
        
        titleBar.setHTML(b.toString());
        
        interpretationPanel.clear();
        
        for (Map<String,String> mention : pData.getMentions()) {
            
            MentionContainer mentionContainer = new MentionContainer(mention);
            interpretationPanel.add(mentionContainer, margin);
            
        }
        
        int height = pData.getMentions().size() * 210;
        
        interpretationPanel.setHeight(height);
        
        ContentPanel interpretationFrame = ((ContentPanel)interpretationPanel.getParent());
        interpretationFrame.setHeight(height);
        
        VBoxLayoutContainer docPanel = (VBoxLayoutContainer)interpretationFrame.getParent();
        docPanel.setHeight(height+300);
        
        forceLayout();
    }
    
    
}
