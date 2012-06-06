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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;

/**
 *
 * @author jweile
 */
public class LoginDialog extends Dialog {

    private static LoginDialog instance;
    
    private TextField username;
    private PasswordField password;
    private HTML label;
    
    public LoginDialog() {
        setHeadingText("Please log in");
        setHideOnButtonClick(false);
        setPredefinedButtons(new PredefinedButton[0]);
        setPixelSize(300, 150);
        setModal(true);
        setClosable(false);
        setResizable(false);
        
        VBoxLayoutContainer con = new VBoxLayoutContainer();
        
        username = new TextField();
        password = new PasswordField();
        //FIXME: This doesn't work, due to a bug in GXT
        password.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    doLogin(username.getText(), password.getText());
                }
            }
        });
        
        label = new HTML();
        con.add(new FieldLabel(username, "User"), BoxConfig.MARGIN);
        con.add(new FieldLabel(password, "Password"), BoxConfig.MARGIN);
        con.add(label,BoxConfig.MARGIN);
        
        add(con);
        
        addButton(new TextButton("Create account", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                LoginDialog.this.hide();
                CreateUserDialog.getInstance().show();
            }
        }));
        
        addButton(new TextButton("Login", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                doLogin(username.getText(), password.getText());
            }
        }));
        
    }

    @Override
    public void show() {
        username.setText("");
        password.setText("");
        label.setHTML("");
        super.show();
    }
    
    
    
    public static LoginDialog getInstance() {
        if (instance == null) {
            instance = new LoginDialog();
        }
        return instance;
    }
    
    private void doLogin(final String user, String pwd) {
        
        LoginServiceAsync.Util.getInstance()
                .login(user, pwd, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
                error(caught.getMessage());
            }

            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    Cookies.setCookie("tmcurator.user", user);
                    TmCurator.getInstance().setUser(user);
                    LoginDialog.this.hide();
                    TmCurator.getInstance().loadGreetingPanel();
                } else {
                    error("Wrong password!");
                }
            }

        });
    }
    
    
    private void error(String message) {
        username.setText("");
        password.setText("");
        label.setHTML("<p style=\"font-size:10px;color:red;\">"
                + message
                + "</p>");
    }
}
