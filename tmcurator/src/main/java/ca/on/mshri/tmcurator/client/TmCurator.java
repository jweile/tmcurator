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

import ca.on.mshri.tmcurator.shared.PairDataSheet;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TmCurator implements EntryPoint {
    
    private static TmCurator instance = null;

    private String user = null;
    
    private ContentPanel mainPanel;
    
    TextButton login;

    public TmCurator() {
        //unfortunately can't make constructor private as GWT has to call it from the outside.
        instance = this;
    }

    public static final AutoProgressMessageBox LOAD_DIALOG = 
                        new AutoProgressMessageBox("Please wait.", 
                                "Loading. please wait."){{
                                    setModal(true);
                                    auto();
                                }};

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
        login.setText(user);
    }
    
    public static TmCurator getInstance() {
        return instance;
    }
    
    
    @Override
    public void onModuleLoad() {
        
        placeMainPanel();
        placeLoginButton();
        
        checkLogin();
        
//        loadGreetingPanel();
        
        
    }
    
    private void placeMainPanel() {
        mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(false);
        mainPanel.setHeight(600);
        
        RootPanel.get("main").add(mainPanel);
    }
    
    private void placeLoginButton() {
        
        login = new TextButton("Login");
        login.setIcon(Resources.INSTANCE.user());
        
        Menu loginMenu = new Menu();
        loginMenu.add(new MenuItem("Log out", new SelectionHandler<MenuItem>() {

            @Override
            public void onSelection(SelectionEvent<MenuItem> event) {
                user = null;
                login.setText("Login");
                Cookies.removeCookie("tmcurator.user");
                mainPanel.clear();
                LoginDialog.getInstance().show();
            }
        }));
        
        MenuItem deleteAccount = new MenuItem("Delete account");
        deleteAccount.setEnabled(false);
        loginMenu.add(deleteAccount);
        
        login.setMenu(loginMenu);
        
        RootPanel.get("login").add(login);
    }
    
    public void loadGreetingPanel() {
        
        LOAD_DIALOG.show();
        
        DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
        
        dataService.currProgress(user, new AsyncCallback<Double>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                displayError(caught);
            }

            @Override
            public void onSuccess(Double result) {
                TmCurator.LOAD_DIALOG.hide();
                
                GreetingPanel gp = GreetingPanel.getInstance();
                gp.setProgress(result);

                mainPanel.clear();
                mainPanel.add(gp);
                mainPanel.forceLayout();
            }

        });
        
        
    }
    
    
    public void loadCurationPanel() {
                
        LOAD_DIALOG.show();
        
        DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
        
        dataService.currPairSheet(user, new AsyncCallback<PairDataSheet>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                displayError(caught);
            }

            @Override
            public void onSuccess(PairDataSheet result) {
                
                TmCurator.LOAD_DIALOG.hide();
                
                CurationPanel p = CurationPanel.getInstance();
                p.updatePairData(result);
                
                mainPanel.clear();
                mainPanel.add(p);
                mainPanel.forceLayout();
            }

        });
    }

    
    private void checkLogin() {
        user = Cookies.getCookie("tmcurator.user");
        //FIXME: There should be a remote service call to verify user existence
        if (user == null) {
            LoginDialog.getInstance().show();
        } else {
            login.setText(user);
            loadGreetingPanel();
        }
    }
    
    
    private void displayError(Throwable caught) {
        new AlertMessageBox("Error",caught.getMessage()).show();
    }
    
}
