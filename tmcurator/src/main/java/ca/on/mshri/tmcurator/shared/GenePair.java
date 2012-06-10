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

/**
 *
 * @author jweile
 */
public class GenePair implements IsSerializable {
    
    private String g1Sym;
    private String g2Sym;
    private int id;

    public GenePair() {
        //for serialization
    }
    
    public GenePair(int id, String g1Sym, String g2Sym) {
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
