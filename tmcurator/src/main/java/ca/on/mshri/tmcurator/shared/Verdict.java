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
public class Verdict implements IsSerializable {
    
    private int pairId;
    private String action;
    private int order;
    private String g1Type;
    private String g2Type;
    private boolean negative;
    private String comment;
    private boolean invalid;
    
    Verdict() {
        //for serialization
    }

    public Verdict(int pairId, String action, int order, String g1Type, String g2Type, boolean negative, String comment) {
        this.pairId = pairId;
        this.action = action;
        this.order = order;
        this.g1Type = g1Type;
        this.g2Type = g2Type;
        this.negative = negative;
        this.comment = comment;
    }

    public String getAction() {
        return action;
    }

    public String getG1Type() {
        return g1Type;
    }

    public String getG2Type() {
        return g2Type;
    }

    public int getOrder() {
        return order;
    }

    public int getPairId() {
        return pairId;
    }

    public boolean isNegative() {
        return negative;
    }

    public String getComment() {
        return comment;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }
    
    
}
