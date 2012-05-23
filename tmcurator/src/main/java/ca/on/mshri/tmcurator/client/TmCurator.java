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
    
    private ContentPanel mainPanel;

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
    
    //TODO: Implement real login functionality.
    public static final String MOCK_USER="user";

    public static TmCurator getInstance() {
        return instance;
    }
    
    
    @Override
    public void onModuleLoad() {
        
        placeMainPanel();
        placeLoginButton();
        
//        checkLogin();
        
        loadGreetingPanel();
        
        
    }
    
    private void placeMainPanel() {
        mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(false);
        mainPanel.setHeight(600);
        
        RootPanel.get("main").add(mainPanel);
    }
    
    private void placeLoginButton() {
        
        TextButton login = new TextButton("Login");
        login.setIcon(Resources.INSTANCE.user());
        
        Menu loginMenu = new Menu();
        loginMenu.add(new MenuItem("Log out"));
        loginMenu.add(new MenuItem("Delete account"));
        
        login.setMenu(loginMenu);
        
        RootPanel.get("login").add(login);
    }
    
    public void loadGreetingPanel() {
        
        LOAD_DIALOG.show();
        
        DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
        
        dataService.currProgress(MOCK_USER, new AsyncCallback<Double>() {

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
        
        dataService.currPairSheet(MOCK_USER, new AsyncCallback<PairDataSheet>() {

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
        
        LoginDialog ld = new LoginDialog();
        ld.show();
        
    }
    
    
    private void displayError(Throwable caught) {
        
        AlertMessageBox b = new AlertMessageBox("Error",caught.getMessage());
//        RootPanel.get().add(b);
        b.show();
    }
    
}
