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

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Slider;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.form.SpinnerField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jweile
 */
public class JumpToPairDialog extends Dialog {

    private int totalPairs;
    
    private int pair;
    
    private Grid<Pair> resultGrid;
    
    private FieldSet resultFieldSet;
    
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
                //TODO jump to selected pair
                hide();
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
        
        searchField = new TextField();
        searchField.setEmptyText("Enter query...");
        con.setCenterWidget(searchField);
        
        BorderLayoutData layout = new BorderLayoutData(50);
        layout.setMargins(new Margins(0,0,0,5));
        
        con.setEastWidget(new TextButton("Search",new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                searchField.getText();
                resultGrid.getStore().clear();
                resultGrid.getStore().add(new Pair(1,"foo","bar"));
                resultGrid.getView().refresh(true);
                resultFieldSet.expand();
            }
        }),layout);
        
        return con;
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
        resultFieldSet.collapse();
    }
    
    
    private IsWidget makeResultsField() {
        resultFieldSet = new FieldSet();
        resultFieldSet.setHeadingText("Results");
        resultFieldSet.setCollapsible(true);
        
        PairProps pairProps = GWT.create(PairProps.class);
        
        List<ColumnConfig<Pair,?>> cols = new ArrayList<ColumnConfig<Pair,?>>();
        cols.add(new ColumnConfig<Pair,Integer>(pairProps.id(),90,"Pair #"));
        cols.add(new ColumnConfig<Pair,String>(pairProps.g1Sym(), 90, "Gene 1"));
        cols.add(new ColumnConfig<Pair,String>(pairProps.g2Sym(), 90, "Gene 2"));
        
        ColumnModel<Pair> model = new ColumnModel<Pair>(cols);
        
        ListStore<Pair> resultStore = new ListStore<Pair>(pairProps.key());
        
        resultGrid = new Grid<Pair>(resultStore, model);
        resultGrid.getView().setEmptyText("No results");
        
        VBoxLayoutContainer con = new VBoxLayoutContainer();
        con.setHeight("220px");
        con.add(resultGrid);
        
//        ScrollPanel scroll = new ScrollPanel(con);
//        scroll.setHeight("220px");
        
        resultFieldSet.setWidget(con);
        resultFieldSet.collapse();
        
        return resultFieldSet;
    }

    public static class Pair {
        private String g1Sym;
        private String g2Sym;
        private int id;
        
        public Pair(int id, String g1Sym, String g2Sym) {
            this.id = id;
            this.g1Sym = g1Sym;
            this.g2Sym = g2Sym;
        }

        public String getG1Sym() {
            return g1Sym;
        }

        public void setG1Sym(String g1Sym) {
            this.g1Sym = g1Sym;
        }

        public String getG2Sym() {
            return g2Sym;
        }

        public void setG2Sym(String g2Sym) {
            this.g2Sym = g2Sym;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
        
    }
    
    public static interface PairProps extends PropertyAccess<Pair> {
        @Path("id")
        ModelKeyProvider<Pair> key();

        ValueProvider<Pair, String> g1Sym();
        
        ValueProvider<Pair, String> g2Sym();
        
        ValueProvider<Pair, Integer> id();
    }
}
