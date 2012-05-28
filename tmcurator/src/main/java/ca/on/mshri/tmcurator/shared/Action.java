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
package ca.on.mshri.tmcurator.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jweile
 */
public class Action implements IsSerializable {
    
    private List<Action> parents;
    
    private String name;
    
    private String _id;

    private List<Action> children;
    
    private boolean close;
    
    private boolean directed;
    
    private Effect effect;
    
    private String parentStr;
    
    private Action() {
        //for serialization
    }
    
    public Action(String name, String parentStr, Effect effect, boolean close, boolean directed) {
        this.name = name;
        this.parentStr = parentStr;
        this.effect = effect;
        this.close = close;
        this.directed = directed;
        this._id = name;
    }
    
    private Action(String name, Effect effect, boolean close, boolean directed, String cloneIdSuffix) {
        this.name = name;
        this.effect = effect;
        this.close = close;
        this.directed = directed;
        this._id = name+cloneIdSuffix;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return _id;
    }

    public Effect getEffect() {
        return effect;
    }

    public boolean isDirected() {
        return directed;
    }

    
    
    public boolean isClose() {
        return close;
    }

    public String getParentStr() {
        return parentStr;
    }
    
    public List<Action> getParents() {
        return parents == null ? Collections.EMPTY_LIST : parents;
    }

    public void addParent(Action parent) {
        if (parents == null) {
            parents = new ArrayList<Action>();
        }
        parents.add(parent);
        
        if (parent.children == null) {
            parent.children = new ArrayList<Action>();
        }
        parent.children.add(this);
    }

    public List<Action> getChildren() {
        return children == null ? Collections.EMPTY_LIST : children;
    }
       
    
    /**
     * Call only on fully formed tree. 
     */
    public void splitIntoClones() {
        if (getParents().size() > 1) {
            for (int i = 1; i < getParents().size(); i++) {
                Action parent = getParents().get(i);
                parent.children.remove(this);
                Action clone = duplicateSubTree(this._id + i);
                clone.addParent(parent);
            }
            Action newParent = getParents().get(0);
            parents = new ArrayList();
            parents.add(newParent);
        }
    }

    private Action duplicateSubTree(String cloneIdSuffix) {
        Action a2 = new Action(this.name, this.effect, this.close, this.directed, cloneIdSuffix);
        
        for (Action c : getChildren()) {
            Action c2 = c.duplicateSubTree(cloneIdSuffix);
            c2.addParent(a2);
        }
        
        return a2;
    }
    
    
}
