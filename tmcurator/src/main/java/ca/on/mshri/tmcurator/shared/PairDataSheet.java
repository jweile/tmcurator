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
import java.util.List;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class PairDataSheet implements IsSerializable {
    
    private int pairNumber;
    private int totalPairNumber;
    private String g1Sym;
    private String g2Sym;
    
    private List<Map<String, String>> mentions;

    public String getG1Sym() {
        return g1Sym;
    }

    public String getG2Sym() {
        return g2Sym;
    }

    public int getPairNumber() {
        return pairNumber;
    }

    public int getTotalPairNumber() {
        return totalPairNumber;
    }

    public List<Map<String, String>> getMentions() {
        return mentions;
    }

    public void setG1Sym(String g1Sym) {
        this.g1Sym = g1Sym;
    }

    public void setG2Sym(String g2Sym) {
        this.g2Sym = g2Sym;
    }

    public void setMentions(List<Map<String, String>> mentions) {
        this.mentions = mentions;
    }

    public void setPairNumber(int pairNumber) {
        this.pairNumber = pairNumber;
    }

    public void setTotalPairNumber(int totalPairNumber) {
        this.totalPairNumber = totalPairNumber;
    }

    
    
}
