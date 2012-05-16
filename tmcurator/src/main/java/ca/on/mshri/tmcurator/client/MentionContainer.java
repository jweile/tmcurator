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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class MentionContainer extends ContentPanel {
    
    private static final BoxLayoutData margin = new BoxLayoutData(new Margins(5,5,5,5));
    
    private static final BoxLayoutData flexMargin = new BoxLayoutData(new Margins(5,5,5,5)){{
        setFlex(1);
    }};
    
    public MentionContainer(Map<String,String> mention) {
            setCollapsible(true);
            setHeight(200);
            
            BorderLayoutContainer borderLayout = new BorderLayoutContainer();
            
            StringBuilder b = new StringBuilder();
            b.append("http://www.ncbi.nlm.nih.gov/entrez/query.fcgi")
             .append("?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=")
             .append(mention.get("pmid"));
            final String url = b.toString();
            
            TextButton pmButton = new TextButton("Pubmed");
            pmButton.setIcon(Resources.INSTANCE.document());
            pmButton.setIconAlign(IconAlign.TOP);
            pmButton.addSelectHandler(new SelectHandler() {

                @Override
                public void onSelect(SelectEvent event) {
                    Window.open(url, "_blank", "");
                }
            });
            
            VBoxLayoutContainer pmButtonContainer = new VBoxLayoutContainer();
            pmButtonContainer.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
            pmButtonContainer.add(pmButton,margin);
            
            borderLayout.setWestWidget(pmButtonContainer);
            
            VBoxLayoutContainer innerContainer = new VBoxLayoutContainer();
            innerContainer.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
            
            ContentPanel textBox = new ContentPanel();
            textBox.setHeaderVisible(false);
            textBox.add(new HTML(mention.get("sentence")));
            
            innerContainer.add(textBox, flexMargin);
            
            innerContainer.add(makeCurationControls(),flexMargin);
            
            borderLayout.setCenterWidget(innerContainer);
            
            add(borderLayout);
    }
    
    private IsWidget makeCurationControls() {
        BorderLayoutContainer controls = new BorderLayoutContainer();
        BorderLayoutData borderData = new BorderLayoutData(300);
        
        HBoxLayoutContainer buttonPanel = new HBoxLayoutContainer();
        buttonPanel.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.STRETCH);
        buttonPanel.add(new TextButton("Switch"),flexMargin);
        buttonPanel.add(new TextButton("Negate"),flexMargin);
        buttonPanel.add(new TextButton("Action"),flexMargin);
        
        controls.setEastWidget(buttonPanel, borderData);
        
        ContentPanel imageBox = new ContentPanel();
        imageBox.setHeaderVisible(false);
        
        HTML image = new HTML("Image here");
        
        imageBox.add(image);
        
        controls.setCenterWidget(imageBox);
        
        return controls;
    }
    
}
