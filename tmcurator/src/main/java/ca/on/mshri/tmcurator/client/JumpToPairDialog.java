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

import ca.on.mshri.tmcurator.shared.GenePair;
import ca.on.mshri.tmcurator.shared.PairDataSheet;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.AutoProgressBar;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Slider;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer.VBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.form.SpinnerField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jweile
 */
public class JumpToPairDialog extends Dialog {

    private int totalPairs;
    
    private int pair;
    
    private Grid<GenePair> resultGrid;
    
    private FieldSet resultFieldSet;
    
    private VBoxLayoutContainer resultContainer;
    
    private Slider pairSlider;
    
    private SpinnerField<Integer> pairSpinner;
    
    TextField searchField;
    
    private static JumpToPairDialog instance;
    
    private JumpToPairDialog(int totalPairs) {
        
        this.totalPairs = totalPairs;
        
        setHeadingText("Jump to gene pair");
        setSize("300px","300px");
        
        setPredefinedButtons(new PredefinedButton[0]);
        
        addCancelButton();
        addGoButton();
        
        BorderLayoutContainer con = new BorderLayoutContainer();
        
        BorderLayoutData topLayout = new BorderLayoutData(20);
        topLayout.setMargins(new Margins(5,5,0,5));
        
        con.setNorthWidget(makeSearchBar(),topLayout);
        
        
        BorderLayoutData centerLayout = new BorderLayoutData();
        centerLayout.setMargins(new Margins(5,5,0,5));
        
        con.setCenterWidget(makeResultsField(),centerLayout);
        
        BorderLayoutData bottomLayout = new BorderLayoutData(60);
        bottomLayout.setMargins(new Margins(5,5,5,5));
        
        con.setSouthWidget(makeSliderField(),bottomLayout);
        
        setWidget(con);
        
    }

    public static JumpToPairDialog getInstance(int totalPairs) {
        if (instance == null) {
            instance = new JumpToPairDialog(totalPairs);
        }
        return instance;
    }
    
    public void show(int pair) {
        this.pair = pair;
        resetFields();
        super.show();
    }
    
    

    private void addGoButton() {
        addButton(new TextButton("Go",new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                jump();
            }

        }));
    }

    private void addCancelButton() {
        addButton(new TextButton("Cancel", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                hide();
            }
            
        }));
    }

    private IsWidget makeSearchBar() {
        BorderLayoutContainer con = new BorderLayoutContainer();
        
        searchField = new TextField() {
            @Override
            public void onBrowserEvent(Event event) {
                super.onBrowserEvent(event);
                if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    triggerSearch();
                }
            }
        };
        searchField.setEmptyText("Enter query...");
        con.setCenterWidget(searchField);
        
        BorderLayoutData layout = new BorderLayoutData(50);
        layout.setMargins(new Margins(0,0,0,5));
        
        con.setEastWidget(new TextButton("Search",new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                triggerSearch();
            }
        }),layout);
        
        return con;
    }
    
    private void triggerSearch() {
        resultContainer.clear();
        AutoProgressBar pb = new AutoProgressBar();
        pb.updateText("Searching...");
        pb.setWidth("230px");
        resultContainer.add(pb);
        resultFieldSet.expand();
        pb.auto();

        String qry = searchField.getText();
        DataProviderServiceAsync service = DataProviderServiceAsync.Util.getInstance();
        service.findPairs(qry, new AsyncCallback<List<GenePair>>() {

            @Override
            public void onFailure(Throwable caught) {
                resultContainer.clear();
                ErrorDialog.getInstance().showError(caught);
            }

            @Override
            public void onSuccess(List<GenePair> result) {
                resultContainer.clear();
                resultGrid.getStore().clear();
                resultGrid.getStore().addAll(result);
                resultContainer.add(resultGrid);
                resultGrid.getView().refresh(true);
            }
        });
    }

    private IsWidget makeSliderField() {
        FieldSet fs = new FieldSet();
        fs.setHeadingText("Selected pair number");
        BorderLayoutContainer con = new BorderLayoutContainer();
        con.setHeight("50px");
        
        pairSlider = new Slider();
        pairSlider.setMinValue(1);
        pairSlider.setMaxValue(totalPairs);
        pairSlider.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                pairSpinner.setValue(event.getValue());
            }
        });
        
        con.setCenterWidget(pairSlider);
        
        pairSpinner = new SpinnerField<Integer>(new IntegerPropertyEditor());
        pairSpinner.setMinValue(1);
        pairSpinner.setMaxValue(totalPairs);
        pairSpinner.addValueChangeHandler(new ValueChangeHandler<Integer>() {

            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                pairSlider.setValue(event.getValue());
            }
        });
        
        BorderLayoutData layout = new BorderLayoutData(70);
        layout.setMargins(new Margins(0,0,0,5));
        
        con.setEastWidget(pairSpinner, layout);
        fs.setWidget(con);
        return fs;
    }

    private void resetFields() {
        pairSpinner.setValue(pair);
        pairSlider.setValue(pair);
        searchField.setValue(null);
        resultGrid.getStore().clear();
        resultGrid.getView().refresh(true);
        resultFieldSet.collapse();
    }
    
    
    private IsWidget makeResultsField() {
        resultFieldSet = new FieldSet();
        resultFieldSet.setHeight("100px");
        resultFieldSet.setHeadingText("Results");
        resultFieldSet.setCollapsible(true);
        
        PairProps pairProps = GWT.create(PairProps.class);
        
        List<ColumnConfig<GenePair,?>> cols = new ArrayList<ColumnConfig<GenePair,?>>();
        cols.add(new ColumnConfig<GenePair,Integer>(pairProps.id(),90,"Pair #"));
        cols.add(new ColumnConfig<GenePair,String>(pairProps.g1Sym(), 90, "Gene 1"));
        cols.add(new ColumnConfig<GenePair,String>(pairProps.g2Sym(), 90, "Gene 2"));
        
        ColumnModel<GenePair> model = new ColumnModel<GenePair>(cols);
        
        ListStore<GenePair> resultStore = new ListStore<GenePair>(pairProps.key());
        
        resultGrid = new Grid<GenePair>(resultStore, model);
        resultGrid.getView().setEmptyText("No results");
        resultGrid.setHeight("100px");
        
        resultGrid.getSelectionModel()
                .addSelectionChangedHandler(new SelectionChangedHandler<GenePair>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<GenePair> event) {
                if (event.getSelection() != null && event.getSelection().size() > 0) {
                    GenePair pair = event.getSelection().get(0);
                    int pairNum = pair.getId();
                    System.out.println(pairNum);
                    pairSpinner.setValue(pairNum);
                    pairSlider.setValue(pairNum);
                }
            }
        });
        
        resultContainer = new VBoxLayoutContainer(VBoxLayoutAlign.CENTER);
        resultContainer.setHeight("100px");
        resultContainer.add(resultGrid);
        
        resultFieldSet.setWidget(resultContainer);
        resultFieldSet.collapse();
        
        return resultFieldSet;
    }
    
    public static interface PairProps extends PropertyAccess<GenePair> {
        @Path("id")
        ModelKeyProvider<GenePair> key();

        ValueProvider<GenePair, String> g1Sym();
        
        ValueProvider<GenePair, String> g2Sym();
        
        ValueProvider<GenePair, Integer> id();
    }
    
    
    private void jump() {
        hide();
        
        pair = pairSpinner.getValue();
        
        final TmCurator main = TmCurator.getInstance();
        TmCurator.LOAD_DIALOG.show();

        DataProviderServiceAsync dataService = DataProviderServiceAsync.Util.getInstance();

        dataService.gotoPairSheet(TmCurator.getInstance().getUser(), pair, new AsyncCallback<PairDataSheet>() {

            @Override
            public void onFailure(Throwable caught) {
                TmCurator.LOAD_DIALOG.hide();
                ErrorDialog.getInstance().showError(caught);
            }

            @Override
            public void onSuccess(PairDataSheet result) {

                TmCurator.LOAD_DIALOG.hide();

                CurationPanel p = CurationPanel.getInstance();
                p.updatePairData(result);

                main.getMainPanel().clear();
                main.getMainPanel().add(p);
                main.getMainPanel().forceLayout();
            }

        });
    }
}
