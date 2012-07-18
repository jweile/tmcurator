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
import com.google.gwt.user.client.Event;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;

/**
 *
 * @author jweile
 */
public class EntitySelectorDialog extends Dialog {
    
    private static EntitySelectorDialog instance;
    
    private VerdictControls verdictControls;
    
    private ComboBox<EntityType> combo1, combo2;

    private EntitySelectorDialog() {
        setHeadingText("Select entity types");
        setSize("280px", "200px");
        
        //OK button action
        getButtonById(PredefinedButton.OK.name())
                .addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                EntityType l = combo1.getCurrentValue();
                EntityType r = combo2.getCurrentValue();
                verdictControls.setEntityTypes(
                        l != null ? l : EntityType.UNKNOWN, 
                        r != null ? r : EntityType.UNKNOWN);
                EntitySelectorDialog.this.hide();
            }
        });
        
        
        
        //LabelProvider for comboboxes
        LabelProvider<EntityType> labelProvider = new LabelProvider<EntityType>() {

            @Override
            public String getLabel(EntityType item) {
                return item.name();
            }
        };
        
        //setup comboboxes
        combo1 = new ComboBox<EntityType>(makeStore(), labelProvider);
        combo1.setForceSelection(true);
        combo1.setTriggerAction(TriggerAction.ALL);
        combo2 = new ComboBox<EntityType>(makeStore(), labelProvider);
        combo2.setForceSelection(true);
        combo2.setTriggerAction(TriggerAction.ALL);
        
        //layout
        VBoxLayoutContainer c = new VBoxLayoutContainer();
        c.add(new FieldLabel(combo1, "Left entity"),BoxConfig.MARGIN);
        c.add(new FieldLabel(combo2, "Right entity"),BoxConfig.MARGIN);
        
        setWidget(c);
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        //FIXME: Only works if dialog has keyboard focus
        if (event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
            hide();
        }
    }

    public static EntitySelectorDialog getInstance() {
        if (instance == null) {
            instance = new EntitySelectorDialog();
        }
        return instance;
    }

    public void show(VerdictControls verdictControls) {
        this.verdictControls = verdictControls;
        combo1.select(verdictControls.getG1Type());
        combo1.setValue(verdictControls.getG1Type());
        combo2.select(verdictControls.getG2Type());
        combo2.setValue(verdictControls.getG2Type());
        show();
    }

    private ListStore<EntityType> makeStore() {
        ListStore<EntityType> store = new ListStore<EntityType>(new ModelKeyProvider<EntityType>() {

            @Override
            public String getKey(EntityType item) {
                return item.name();
            }
        });
        store.add(EntityType.GENE);
        store.add(EntityType.TRANSCRIPT);
        store.add(EntityType.PROTEIN);
        store.add(EntityType.UNKNOWN);
        return store;
    }
    
    
}
