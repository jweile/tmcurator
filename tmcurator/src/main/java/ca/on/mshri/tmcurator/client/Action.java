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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jweile
 */
public class Action {
    
    private static final KeyGen KG = new KeyGen();
    
    private List<Action> parents;
    
    private String name;
    
    private int _id;

    private List<Action> children;
    
    public Action(String name) {
        this.name = name;
        this._id = KG.nextId();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return _id;
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
    
//    public void purgeParents() {
//        parent = null;
//        if (children != null) {
//            for (Action c : children) {
//                c.purgeParents();
//            }
//        }
//    }
    
    
    /**
     * Call only on fully formed tree. 
     */
    public void splitIntoClones() {
        if (getParents().size() > 1) {
            for (int i = 1; i < getParents().size(); i++) {
                Action parent = getParents().get(i);
                parent.children.remove(this);
                Action clone = duplicateSubTree();
                clone.addParent(parent);
            }
            Action newParent = getParents().get(0);
            parents = new ArrayList();
            parents.add(newParent);
        }
    }

    private Action duplicateSubTree() {
        Action a2 = new Action(this.name);
        
        for (Action c : getChildren()) {
            Action c2 = c.duplicateSubTree();
            c2.addParent(a2);
        }
        
        return a2;
    }
    
    public static class KeyGen {
        int lastId = 0;
        public int nextId() {
            return ++lastId;
        }
    }
    
    
}
