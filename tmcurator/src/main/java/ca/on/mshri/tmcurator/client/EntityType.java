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

import com.google.gwt.resources.client.ImageResource;

/**
 *
 * @author jweile
 */
 public enum EntityType {
    PROTEIN(Resources.INSTANCE.protein()), TRANSCRIPT(Resources.INSTANCE.rna()), 
    GENE(Resources.INSTANCE.gene()), UNKNOWN(Resources.INSTANCE.unknown());
    
    private ImageResource r;

    private EntityType(ImageResource r) {
        this.r = r;
    }

    public static EntityType fromString(String s) {
        if (s.equalsIgnoreCase("protein")) {
            return PROTEIN;
        } else if (s.equalsIgnoreCase("gene")) {
            return GENE;
        } else if (s.equalsIgnoreCase("transcript")) {
            return TRANSCRIPT;
        } else {
            return UNKNOWN;
        }
    }

    public ImageResource getImage() {
        return r;
    }
    
}
