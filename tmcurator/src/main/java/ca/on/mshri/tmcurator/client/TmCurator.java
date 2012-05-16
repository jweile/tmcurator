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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.AutoProgressBar;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ProgressBar;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TmCurator implements EntryPoint {
    
    private ContentPanel mainPanel;
    
    @Override
    public void onModuleLoad() {
        
        placeLoginButton();
        
        placeGreetingPanel();
        
        showLoginDialog();
        
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
    
    private void placeGreetingPanel() {
        
        VBoxLayoutContainer greet = new VBoxLayoutContainer();
//        greet.setHeight(600);
        greet.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.CENTER);
        greet.setPack(BoxLayoutPack.CENTER);
        
        BoxLayoutData layout = new BoxLayoutData(new Margins(0,0,10,0));
        
        greet.add(new HTML("Welcome back!"),layout);
        
        ProgressBar progressbar = new ProgressBar();
        progressbar.updateProgress(.2, "Curation progress: 20%");
        greet.add(progressbar, layout);
        
        TextButton continueButton = new TextButton("Continue");
        continueButton.addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                loadCurationPanel();
            }

        });
        greet.add(continueButton, layout);
        
        mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(false);
        mainPanel.setHeight(600);
        
        mainPanel.add(greet);
        mainPanel.forceLayout();
        
        RootPanel.get("main").add(mainPanel);
    }
    
    //TODO: Implement real login functionality.
    private static final String MOCK_USER="user";
    
    private void loadCurationPanel() {
        
        CenterLayoutContainer panel = new CenterLayoutContainer();
        panel.setHeight(600);
        
        AutoProgressBar pb = new AutoProgressBar();
        pb.updateText("Loading...");
        panel.add(pb);
        
        mainPanel.clear();
        mainPanel.add(panel);
        mainPanel.forceLayout();
        
        pb.auto();
        
        DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();
        
        dataService.nextPairSheet(MOCK_USER, new AsyncCallback<PairDataSheet>() {

            @Override
            public void onFailure(Throwable caught) {
                displayError(caught);
            }

            @Override
            public void onSuccess(PairDataSheet result) {
                
                CurationPanel p = CurationPanel.getInstance();

                p.updatePairData(result);
                
                mainPanel.clear();
                mainPanel.add(p);
                mainPanel.forceLayout();
            }

        });
    }

    
    private void showLoginDialog() {
        
        LoginDialog ld = new LoginDialog();
        ld.show();
        
    }
    
    
    private void displayError(Throwable caught) {
        
        AlertMessageBox b = new AlertMessageBox("Error",caught.getMessage());
        RootPanel.get().add(b);
        b.show();
    }
    
}
