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

import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.form.TextArea;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class ErrorDialog extends Dialog {

    private static ErrorDialog instance;
    
    private TextArea message;
    
    private ErrorDialog() {
        setHeadingText("Error");
        setSize("300px","200px");
        
        setPredefinedButtons(PredefinedButton.OK);
        setHideOnButtonClick(true);
        
        VBoxLayoutContainer con = new VBoxLayoutContainer();
        con.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
        
        con.add(new Label("An error occurred:"),BoxConfig.MARGIN);
        
        message = new TextArea();
        con.add(message,BoxConfig.FLEX_MARGIN);
        
        setWidget(con);
    }
    
    public static ErrorDialog getInstance() {
        if (instance == null) {
            instance = new ErrorDialog();
        }
        return instance;
    }
    
    public void showError(Throwable t) {
        
        StackTraceElement[] stackTrace = t.getStackTrace();
        
        StringBuilder b = new StringBuilder();
        
        b.append(t.getMessage());
        while ((t = t.getCause()) != null) {
            b.append("\nReason: ");
            b.append(t.getMessage());
        }
        
        b.append("\n\nStack trace:");
        int i = 0;
        for (StackTraceElement e : stackTrace) {
            b.append("\n").append(e);
            if (i++ > 50) {
                b.append("\n...");
            }
        }
        
        message.setText(b.toString());
        
        setSize("300px","200px");
        
        show();
    }
    
}
