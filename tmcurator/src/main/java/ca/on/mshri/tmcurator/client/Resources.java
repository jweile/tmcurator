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
    
    
    @Source("ca/on/mshri/tmcurator/client/gene.jpg")
    ImageResource gene();
    
    
    @Source("ca/on/mshri/tmcurator/client/protein.jpg")
    ImageResource protein();
    
    
    @Source("ca/on/mshri/tmcurator/client/unknown.jpg")
    ImageResource unknown();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_-1_-1_0.jpg")
    ImageResource arrowNegNegNull();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_-1_-1_1.jpg")
    ImageResource arrowNegNegPos();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_-1_0_0.jpg")
    ImageResource arrowNegNullNull();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_-1_0_1.jpg")
    ImageResource arrowNegNullPos();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_-1_1_0.jpg")
    ImageResource arrowNegPosNull();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_0_0_0.jpg")
    ImageResource arrowNullNullNull();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_0_0_1.jpg")
    ImageResource arrowNullNullPos();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_1_-1_0.jpg")
    ImageResource arrowPosNegNull();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_1_0_0.jpg")
    ImageResource arrowPosNullNull();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_1_0_1.jpg")
    ImageResource arrowPosNullPos();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_1_1_0.jpg")
    ImageResource arrowPosPosNull();
    
    
    @Source("ca/on/mshri/tmcurator/client/arrow_1_1_1.jpg")
    ImageResource arrowPosPosPos();
    
    
}
