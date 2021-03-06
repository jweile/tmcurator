/*
 *  Copyright (C) 2012 Jochen Weile, M.Sc. <jochenweile@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.on.mshri.tmcurator.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 *
 * @author jweile
 */
public interface Resources extends ClientBundle {
    
    public static final Resources INSTANCE = GWT.create(Resources.class);
    
    @Source("ca/on/mshri/tmcurator/client/user.png")
    ImageResource user();
    
    
    @Source("ca/on/mshri/tmcurator/client/document.png")
    ImageResource document();
    
    
    @Source("ca/on/mshri/tmcurator/client/action.png")
    ImageResource action();
    
    
    @Source("ca/on/mshri/tmcurator/client/entity.png")
    ImageResource entity();
    
    
    @Source("ca/on/mshri/tmcurator/client/invalid.png")
    ImageResource invalid();
    
    
    @Source("ca/on/mshri/tmcurator/client/negate.png")
    ImageResource negate();
    
    
    @Source("ca/on/mshri/tmcurator/client/switch.png")
    ImageResource switching();
    
    
    @Source("ca/on/mshri/tmcurator/client/gene.jpg")
    ImageResource gene();
    
    
    @Source("ca/on/mshri/tmcurator/client/rna.png")
    ImageResource rna();
    
    
    @Source("ca/on/mshri/tmcurator/client/protein.jpg")
    ImageResource protein();
    
    
    @Source("ca/on/mshri/tmcurator/client/unknown.jpg")
    ImageResource unknown();
    
}
