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

/**
 *
 * @author jweile
 */
 public enum Order {
    FWD(1), BCK(-1), NONE(0);
    private int mod;

    private Order(int mod) {
        this.mod = mod;
    }

    public static Order fromInt(int i) {
        if (i > 0) {
            return FWD;
        } else if (i < 0) {
            return BCK;
        } else {
            return NONE;
        }
    }

    public Order flip() {
        return fromInt(mod() * -1);
    }

    public int mod() {
        return mod;
    }
    
}
