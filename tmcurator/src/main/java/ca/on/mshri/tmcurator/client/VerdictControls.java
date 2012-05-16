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
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;

/**
 *
 * @author jweile
 */
public class VerdictControls extends BorderLayoutContainer{

    public VerdictControls() {
        
        BorderLayoutData borderData = new BorderLayoutData(300);
        
        HBoxLayoutContainer buttonPanel = new HBoxLayoutContainer();
        buttonPanel.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.STRETCH);
        buttonPanel.add(new TextButton("Switch"), BoxConfig.FLEX_MARGIN);
        buttonPanel.add(new TextButton("Negate"), BoxConfig.FLEX_MARGIN);
        buttonPanel.add(new TextButton("Action"), BoxConfig.FLEX_MARGIN);
        
        setEastWidget(buttonPanel, borderData);
        
        ContentPanel imageBox = new ContentPanel();
        imageBox.setHeaderVisible(false);
        
        HTML image = new HTML("Image here");
        
        imageBox.add(image);
        
        setCenterWidget(imageBox);
        
    }
    
    
    
}
