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

import ca.on.mshri.tmcurator.shared.Action;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.cell.core.client.SimpleSafeHtmlCell;
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
        setSize("400px","400px");
        
        setHeadingText("Select action type");
        
        getButtonById(PredefinedButton.OK.name()).addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                caller.setAction(tree.getSelectionModel().getSelectedItem());
                ActionSelectorDialog.this.hide();
            }
        });
        
        AutoProgressBar pb = new AutoProgressBar();
        pb.updateText("Loading...");
        
        CenterLayoutContainer pbCon = new CenterLayoutContainer();
        pbCon.add(pb);
        
        setWidget(pbCon);
        
        DataProviderServiceAsync service = DataProviderServiceAsync.Util.getInstance();
        service.getActions(new AsyncCallback<List<Action>>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                AlertMessageBox b = new AlertMessageBox("Error",caught.getMessage());
                b.show();
            }

            @Override
            public void onSuccess(List<Action> result) {
                
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
                
                tree.setCell(new SimpleSafeHtmlCell<String>(SimpleSafeHtmlRenderer.getInstance(), "click") {

                    private long lastClickTime = 0;
                    
                    @Override
                    public void onBrowserEvent(Context context, Element parent, 
                            String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
                        super.onBrowserEvent(context, parent, value, event, valueUpdater);
                        if (event.getType().equals("click")) {
                            long time = System.currentTimeMillis();
                            if (time - lastClickTime < 500L) {
                                caller.setAction(tree.getSelectionModel().getSelectedItem());
                                ActionSelectorDialog.this.hide();
                            }
                            lastClickTime = time;
                        }
                    }
                    
                });
                
                setWidget(cp);
                ActionSelectorDialog.this.forceLayout();
                
                if (caller != null) {
                    select(caller.getAction());
                }
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
        if (tree != null) {
            select(caller.getAction());
        }
        show();
    }
    
    private void select(Action a) {
        if (a != null) {
            Action selectedAction = a;
            if (a.getParentStr() != null && a.getParentStr().equals("DECOY")) {
                for (Action aCurr : tree.getStore().getAll()) {
                    if (aCurr.getName().equals(a.getName())) {
                        selectedAction = aCurr;
                        break;
                    }
                }
            }
            if (selectedAction != null) {
                tree.getSelectionModel().select(selectedAction, false);
                tree.collapseAll();
                expandPath(tree,selectedAction);
            }
        }
    }
    
    
    private void expandPath(Tree<Action, String> tree, Action action) {
        for (Action p : action.getParents()) {
            expandPath(tree, p);
            tree.setExpanded(p, true);
        }
    }
    
    
    
    private void fillTreeStore(List<Action> actionList, TreeStore<Action> treeStore) {
        
        Map<String,Action> actions = new HashMap<String,Action>();
        
        //register each action by name
        for (Action a : actionList) {
            actions.put(a.getName(), a);
        }
        
        //assign parents of each type
        for (Action a : actionList) {
            if (a.getParentStr() != null && a.getParentStr().trim().length() > 0) {
                String[] parentIds = a.getParentStr().split("; ");
                for (String parentId : parentIds) {
                    Action parent = actions.get(parentId);
                    if (parent != null) {
                        a.addParent(parent);
                    }
                }
            }
            
        }
        
        //make clones of subtrees with multiple parents (to bypass GXT restrictions)
        for (Action a : actionList) {
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
            root.sort();
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

}
