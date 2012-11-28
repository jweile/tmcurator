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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer.VBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.SpinnerField;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class SettingsDialog extends Dialog {
    
    private static SettingsDialog instance = null;
    
    
    private SpinnerField<Integer> quotaSpinner, offsetSpinner;
    private CheckBox approvalEnabledBox;
    
    private SettingsDialog() {
        
        setSize("400px","500px");
        
        setHeadingText("Settings");
        
        setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
        setHideOnButtonClick(true);
        
        getButtonById(PredefinedButton.OK.name())
                .addSelectHandler(new SelectEvent.SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                
                //save settings locally
                TmCurator.getInstance().getConfig().setQuota(quotaSpinner.getValue());
                TmCurator.getInstance().getConfig().setOffset(offsetSpinner.getValue());
                TmCurator.getInstance().getConfig().setApprovalEnabled(approvalEnabledBox.getValue());
                
                //save settings to database
                LoginServiceAsync.Util.getInstance()
                        .setConfig(TmCurator.getInstance().getConfig(), 
                        new AsyncCallback<Void>(){

                    @Override
                    public void onFailure(Throwable caught) {
                        ErrorDialog.getInstance().showError(
                                new RuntimeException("Unable to save settings!",caught));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        //succeed silently
                    }
                });
                
            }
        });
        
        //set tabbed panel
        TabPanel tabPanel = new TabPanel();
        setWidget(tabPanel);
        
        //container for the first tab
        VBoxLayoutContainer optionsTab = new VBoxLayoutContainer();
        optionsTab.setPadding(new Padding(5));
        optionsTab.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        
        //first section in first tab
        FieldSet contingentOptions = new FieldSet();
//        contingentOptions.setHeight("200px");
        contingentOptions.setHeadingText("Contingent Options");
        VBoxLayoutContainer con = new VBoxLayoutContainer();
        con.setHeight("200px");
        
        quotaSpinner = new SpinnerField<Integer>(new NumberPropertyEditor.IntegerPropertyEditor());
        quotaSpinner.setMinValue(1);
        quotaSpinner.setMaxValue(1000);
        con.add(new FieldLabel(quotaSpinner, "Quota"), BoxConfig.MARGIN);
        
        offsetSpinner = new SpinnerField<Integer>(new NumberPropertyEditor.IntegerPropertyEditor());
        offsetSpinner.setMinValue(0);
        offsetSpinner.setMaxValue(1000);
        con.add(new FieldLabel(offsetSpinner, "Offset"), BoxConfig.MARGIN);
        
        contingentOptions.setWidget(con);
        optionsTab.add(contingentOptions);
        
        //second section of first tab
        FieldSet generalOptions = new FieldSet();
//        generalOptions.setHeight("100px");
        generalOptions.setHeadingText("Curation modes");
        con = new VBoxLayoutContainer();
        con.setHeight("100px");
        
        approvalEnabledBox = new CheckBox();
        approvalEnabledBox.setBoxLabel("Enabled");
        con.add(new FieldLabel(approvalEnabledBox, "Approval"), BoxConfig.MARGIN);
        
        generalOptions.setWidget(con);
        optionsTab.add(generalOptions);
        
        //add first tab to tabbed panel
        tabPanel.add(optionsTab, "Options");
        
        
        //load settings
        quotaSpinner.setValue(TmCurator.getInstance().getConfig().getQuota());
        offsetSpinner.setValue(TmCurator.getInstance().getConfig().getOffset());
        approvalEnabledBox.setValue(TmCurator.getInstance().getConfig().isApprovalEnabled());
        
        
    }

    @Override
    public void show() {
        super.show();
        setSize("400px","500px");
    }
    
    
        
    public static SettingsDialog getInstance() {
        if (instance == null) {
            instance = new SettingsDialog();
        }
        return instance;
    }
    
}
