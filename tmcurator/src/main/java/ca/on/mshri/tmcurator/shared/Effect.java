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

/**
 *
 * @author jweile
 */
 public enum Effect {
    INHIBIT, ACTIVATE, ENHANCE;

    public static Effect fromInt(int i) {
        if (i > 0) {
            return ENHANCE;
        } else if (i < 0) {
            return INHIBIT;
        } else {
            return ACTIVATE;
        }
    }
    
}
