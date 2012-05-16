/*
 *  Copyright (C) 2012 Jochen Weile, M.Sc. <jochenweile@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.on.mshri.tmcurator.client;

import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

/**
 *
 * @author jweile
 */
public class LoginDialog extends Dialog {

    public LoginDialog() {
        setHeadingText("Login with OpenID");
        setHideOnButtonClick(false);
        setPredefinedButtons(new PredefinedButton[0]);
        setPixelSize(650, 260);
        setModal(true);
        
        HBoxLayoutContainer hc = new HBoxLayoutContainer();
        
        BoxLayoutData layout = new BoxLayoutData(new Margins(5,5,5,5));
        
        SelectHandler handler = new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                LoginDialog.this.hide();
            }
            
        };
        
        hc.add(makeButton("Google", handler),layout);
        hc.add(makeButton("OID.org", handler),layout);
        hc.add(makeButton("Other", handler),layout);
        
        add(hc);
        
    }
    
    private IsWidget makeButton(String label, SelectHandler handler) {
        TextButton b = new TextButton(label, handler);
        b.setPixelSize(200, 200);
        return b;
    }
    
    
    
}
