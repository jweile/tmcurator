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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
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
public class CreateUserDialog extends Dialog {

    private static CreateUserDialog instance;
    private TextField username;
    private PasswordField password;
    private PasswordField passwordRepeat;
    private HTML label;

    public CreateUserDialog() {
        setHeadingText("Create new account");
        setHideOnButtonClick(false);
        setPredefinedButtons(new PredefinedButton[0]);
        setPixelSize(300, 200);
        setModal(true);
        setClosable(false);
        setResizable(false);

        VBoxLayoutContainer con = new VBoxLayoutContainer();

        username = new TextField();
        password = new PasswordField();
        passwordRepeat = new PasswordField(){
            @Override
            public void onBrowserEvent(Event event) {
                super.onBrowserEvent(event);
                if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    ok();
                }
            }
        };
        
        label = new HTML();
        con.add(new FieldLabel(username, "User name"), BoxConfig.MARGIN);
        con.add(new FieldLabel(password, "Password"), BoxConfig.MARGIN);
        con.add(new FieldLabel(passwordRepeat, "Repeat password"), BoxConfig.MARGIN);
        con.add(label, BoxConfig.MARGIN);

        add(con);

        addButton(new TextButton("Cancel", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                CreateUserDialog.this.hide();
                LoginDialog.getInstance().show();
            }
        }));

        addButton(new TextButton("Login", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                ok();
            }
        }));

    }
    
    private void ok() {
        if (password.getText().equals(passwordRepeat.getText())) {
           createUser(username.getText(), password.getText()); 
        } else {
            error("Passwords do not match!");
        }
    }

    @Override
    public void show() {
        username.setText("");
        password.setText("");
        label.setHTML("");
        setPixelSize(300, 200);
        super.show();
    }

    public static CreateUserDialog getInstance() {
        if (instance == null) {
            instance = new CreateUserDialog();
        }
        return instance;
    }

    private void createUser(final String user, String pwd) {

        LoginServiceAsync.Util.getInstance().addUser(user, pwd, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
                error(caught.getMessage());
            }

            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    Cookies.setCookie("tmcurator.user", user);
                    TmCurator.getInstance().setUser(user);
                    CreateUserDialog.this.hide();
                    TmCurator.getInstance().loadGreetingPanel();
                } else {
                    error("Account not created!");
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
