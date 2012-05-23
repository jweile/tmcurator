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
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.AutoProgressBar;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.tree.Tree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class ActionSelectorDialog extends Dialog {
    
    private static ActionSelectorDialog instance;
    
    private Tree<Action,String> tree;
    
    private VerdictControls caller;

    private ActionSelectorDialog() {
        setSize("800px","600px");
        
        setHeadingText("Select action type");
        
        getButtonById(PredefinedButton.OK.name()).addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                caller.setAction(tree.getSelectionModel().getSelectedItem().getName());
                ActionSelectorDialog.this.hide();
            }
        });
        
        AutoProgressBar pb = new AutoProgressBar();
        pb.updateText("Loading...");
        
        CenterLayoutContainer pbCon = new CenterLayoutContainer();
        pbCon.add(pb);
        
        setWidget(pbCon);
        
        DataProviderServiceAsync service = DataProviderServiceAsync.Util.getInstance();
        service.getActions(new AsyncCallback<Map<String,String>>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                AlertMessageBox b = new AlertMessageBox("Error",caught.getMessage());
                b.show();
            }

            @Override
            public void onSuccess(Map<String,String> result) {
                
                TreeStore<Action> treeStore = new TreeStore<Action>(new ModelKeyProvider<Action>(){
                    @Override
                    public String getKey(Action item) {
                        return String.valueOf(item.getId());
                    }

                });

                fillTreeStore(result, treeStore);
                
                tree = new Tree<Action,String>(treeStore, new ValueProvider<Action,String>() {

                    @Override
                    public String getValue(Action a) {
                        return a.getName();
                    }

                    @Override
                    public void setValue(Action a, String value) {
                        //do nothing
                    }

                    @Override
                    public String getPath() {
                        return "name";
                    }
                });
                
                ContentPanel cp = new ContentPanel();
                cp.setHeaderVisible(false);
                cp.setWidget(tree);
                
                setWidget(cp);
                ActionSelectorDialog.this.forceLayout();
            }

        });
        pb.auto();
        
        
    }

    
    public static ActionSelectorDialog getInstance() {
        if (instance == null) {
            instance = new ActionSelectorDialog();
        }
        return instance;
    }

    public void show(VerdictControls caller) {
        this.caller = caller;
        String actionType = caller.getAction();
        if (actionType != null) {
            Action action = null;
            for (Action a : tree.getStore().getAll()) {
                if (a.getName().equals(actionType)) {
                    action = a;
                    break;
                }
            }
            if (action != null) {
                tree.getSelectionModel().select(action, false);
                tree.collapseAll();
                expandPath(tree,action);
            }
        }
        show();
    }
    
    
    private void fillTreeStore(Map<String, String> action2parents, TreeStore<Action> treeStore) {
        
        Map<String,Action> actions = new HashMap<String,Action>();
        
        //register each type
        for (String id : action2parents.keySet()) {
            actions.put(id, new Action(id));
        }
        
        //assign parents of each type
        for (String id: action2parents.keySet()) {
            String parentStr = action2parents.get(id);
            if (parentStr != null && parentStr.trim().length() > 0) {
                String[] parentIds = parentStr.split("; ");
                for (String parentId : parentIds) {
                    Action parent = actions.get(parentId);
                    if (parent != null) {
                        actions.get(id).addParent(parent);
                    }
                }
            }
            
        }
        
        //make clones of subtrees with multiple parents (to bypass GXT restrictions)
        for (Action a : actions.values()) {
            if (a.getParents().size() > 1) {
                a.splitIntoClones();
            }
        }
        
        //find roots
        List<Action> roots = new ArrayList<Action>();
        for (Action a : actions.values()) {
            if (a.getParents().isEmpty()) {
                roots.add(a);
            }
        }
        
        //enter tree into treestore
        for (Action root : roots) {
            treeStore.add(root);
            addChildrenOf(root, treeStore);
        }
    }

    private void addChildrenOf(Action a, TreeStore<Action> treeStore) {
        if (a.getChildren() != null) {
            for (Action c : a.getChildren()) {
                treeStore.add(a, c);
                addChildrenOf(c, treeStore);
            }
        }
    }

    private void expandPath(Tree<Action, String> tree, Action action) {
        for (Action p : action.getParents()) {
            expandPath(tree, p);
            tree.setExpanded(action, true);
        }
    }
    
}
