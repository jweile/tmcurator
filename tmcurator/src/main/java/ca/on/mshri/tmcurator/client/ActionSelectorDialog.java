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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.cell.core.client.SimpleSafeHtmlCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.Style.LayoutRegion;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.AutoProgressBar;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.event.ExpandItemEvent;
import com.sencha.gxt.widget.core.client.event.ExpandItemEvent.ExpandItemHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.tree.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    
    private ComboBox<Action> searchBox;
    
    private ListView<Action,String> quickList;
    
    private BorderLayoutContainer mainPanel;

    private ActionSelectorDialog() {
        setSize("400px","500px");
        
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
                ErrorDialog.getInstance().showError(caught);
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
                
                searchBox = new ComboBox<Action>(makeListStore(result), new LabelProvider<Action>() {
                    @Override
                    public String getLabel(Action item) {
                        return item.getName();
                    }
                });
                searchBox.setEmptyText("Search...");
                searchBox.setTypeAhead(true);
                searchBox.setForceSelection(true);
                searchBox.setTriggerAction(TriggerAction.ALL);
                searchBox.addSelectionHandler(new SelectionHandler<Action>() {
                    @Override
                    public void onSelection(SelectionEvent<Action> event) {
                        select(event.getSelectedItem());
                    }
                }); 
                
                
                quickList = makeQuickList(); 
                ContentPanel qlPanel = new ContentPanel();
                qlPanel.setHeight("100px");
                qlPanel.setHeaderVisible(true);
                qlPanel.setHeadingText("Top 10 actions");
                qlPanel.setWidget(quickList);
                
                mainPanel = new BorderLayoutContainer();
                
                ContentPanel treePanel = new ContentPanel();
                treePanel.setHeaderVisible(false);
                treePanel.setWidget(tree);
                BorderLayoutData centerData = new BorderLayoutData();
                centerData.setMargins(new Margins(5,5,5,5));
                mainPanel.setCenterWidget(treePanel,centerData);
                
                BorderLayoutData northData = new BorderLayoutData(25);
                northData.setMargins(new Margins(5,5,5,5));
                mainPanel.setNorthWidget(searchBox,northData);
                
                BorderLayoutData southData = new BorderLayoutData(100);
                southData.setMargins(new Margins(5,5,5,5));
                southData.setCollapsible(true);
                mainPanel.setSouthWidget(qlPanel, southData);
                mainPanel.collapse(LayoutRegion.SOUTH);
                
                mainPanel.addExpandHandler(new ExpandItemHandler<ContentPanel>() {

                    @Override
                    public void onExpand(ExpandItemEvent<ContentPanel> event) {
                        loadQuickList();
                    }
                });
                
                setWidget(mainPanel);
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
        if (searchBox != null) {
            searchBox.setValue(null);
        }
        if (tree != null) {
            select(caller.getAction());
        }
        if (mainPanel != null) {
            mainPanel.collapse(LayoutRegion.SOUTH);
        }
        show();
        setSize("400px","500px");
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
                tree.collapseAll();
                expandPath(tree,selectedAction);
                tree.getSelectionModel().select(selectedAction, false);
                tree.scrollIntoView(selectedAction);
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
    
    
    private ListStore<Action> makeListStore(List<Action> result) {
        ListStore<Action> store = new ListStore<Action>(new ModelKeyProvider<Action>() {

            @Override
            public String getKey(Action item) {
                return item.getId();
            }
            
        });
        
        Collections.sort(result, new Comparator<Action>() {

            @Override
            public int compare(Action t, Action t1) {
                return t.getId().compareTo(t1.getId());
            }
        });
        
        store.addAll(result);
        
        return store;
    }
    
    private ListView<Action,String> makeQuickList() {
        ListStore<Action> qlStore = new ListStore<Action>(new ModelKeyProvider<Action>() {

            @Override
            public String getKey(Action item) {
                return item.getId();
            }

        });
        ListView<Action,String> quickList = new ListView<Action, String>(qlStore, new ValueProvider<Action, String>() {

            @Override
            public String getValue(Action object) {
                return object.getName();
            }

            @Override
            public void setValue(Action object, String value) {
                //not supported
            }

            @Override
            public String getPath() {
                return "name";
            }
        });
        quickList.getSelectionModel().addSelectionHandler(new SelectionHandler<Action>() {

            @Override
            public void onSelection(SelectionEvent<Action> event) {
                select(event.getSelectedItem());
            }
        });
        return quickList;
    }
    
    
    private void loadQuickList() {
        DataProviderServiceAsync serv = DataProviderServiceAsync.Util.getInstance();
        serv.getTopActions(new AsyncCallback<List<Action>>() {

            @Override
            public void onFailure(Throwable caught) {
                ErrorDialog.getInstance().showError(caught);
            }

            @Override
            public void onSuccess(List<Action> result) {
                quickList.getStore().clear();
                quickList.getStore().addAll(result);
                quickList.refresh();
            }
        });
    }

}
