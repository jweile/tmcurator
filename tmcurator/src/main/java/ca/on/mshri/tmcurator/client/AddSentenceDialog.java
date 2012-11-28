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

import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class AddSentenceDialog extends Dialog {
    
    private static AddSentenceDialog instance;
    
    private TextArea sentenceField;
    
    private TextField pmidField;

    private AddSentenceDialog() {
        
        setModal(true);
        
        setSize("400px", "250px");
        
        setHeadingText("Add new sentence");
        
        //define OK and Cancel buttons
        setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
        setHideOnButtonClick(true);
        getButtonById(PredefinedButton.OK.name()).addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                CurationPanel.getInstance()
                        .addSentence(sentenceField.getValue(), 
                        pmidField.getValue());
            }

        });
        
        //make contents
        VBoxLayoutContainer con = new VBoxLayoutContainer();
        con.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
        setWidget(con);
        
        sentenceField = new TextArea();
        sentenceField.setEmptyText("Enter sentence text...");
        con.add(sentenceField, BoxConfig.FLEX_MARGIN);
        
        pmidField = new TextField();
        pmidField.setEmptyText("Enter PubMed ID...");
        con.add(pmidField, BoxConfig.MARGIN);
        
    }

    public static AddSentenceDialog getInstance() {
        if (instance == null) {
            instance = new AddSentenceDialog();
        }
        return instance;
    }
    
    @Override
    public void show() {
        sentenceField.setValue("");
        pmidField.setValue("");
        super.show();
        setSize("400px", "250px");
    }
    
    
    
    
    
    
}
