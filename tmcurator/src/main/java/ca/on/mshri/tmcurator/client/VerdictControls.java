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

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class VerdictControls extends BorderLayoutContainer{

    private static final BorderLayoutData scaffoldLayout = new BorderLayoutData(40);
    
    private CenterLayoutContainer imageBoxCenter;
    
    public VerdictControls() {
        
        BorderLayoutData borderData = new BorderLayoutData(300);
        
        HBoxLayoutContainer buttonPanel = new HBoxLayoutContainer();
        buttonPanel.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.STRETCH);
        
        //TODO: implement verdict control functionality
        buttonPanel.add(new TextButton("Switch"), BoxConfig.FLEX_MARGIN);
        buttonPanel.add(new TextButton("Negate"), BoxConfig.FLEX_MARGIN);
        buttonPanel.add(new TextButton("Action"), BoxConfig.FLEX_MARGIN);
        
        setEastWidget(buttonPanel, borderData);
        
        ContentPanel imageBox = new ContentPanel();
        imageBox.setHeaderVisible(false);
        
        imageBoxCenter = new CenterLayoutContainer();
        
        imageBox.add(imageBoxCenter);
        
        setCenterWidget(imageBox);
        
    }
    
    
    public void configureImage(Map<String,String> data, String g1sym) {
        
        ImageResource left = getTypeImage(data.get("type1"));
        ImageResource right = getTypeImage(data.get("type2"));
        String legendText = makeLegend(data.get("updown"), data.get("downstream"), data.get("upstream"), data.get("actionType"));
        
        int orderInt, effectInt, closeInt;
        try {
            orderInt = Integer.parseInt(data.get("updown"));
            effectInt = Integer.parseInt(data.get("effect"));
            closeInt = Integer.parseInt(data.get("close_connection"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Attributes in mention have wrong format.",e);
        }
        
        if (!data.get("upstream").equalsIgnoreCase(g1sym)) {
            orderInt *= -1;
        }
        
        IsWidget canvas = drawImage(left, right, orderInt, effectInt, closeInt, legendText);
        imageBoxCenter.add(canvas);
        imageBoxCenter.forceLayout();
        
    }

    private ImageResource getTypeImage(String type) {
        if (type.equalsIgnoreCase("protein")) {
            return Resources.INSTANCE.protein();
        } else if (type.equalsIgnoreCase("gene")) {
            return Resources.INSTANCE.gene();
        } else {
            return Resources.INSTANCE.unknown();
        }
    }

    private String makeLegend(String order, String a, String b, String type) {
        boolean reverse = order.equals("-1");
        return new StringBuilder()
                .append(reverse ? b : a)
                .append(" ")
                .append(type)
                .append(" ")
                .append(reverse ? a : b)
                .toString();
    }
    
    private IsWidget drawImage(ImageResource lImage, ImageResource rImage, 
            int order, int effect, int close, String legend) {
        int imageW = 40;
        int imageH = 30;
        int arrowW = 70;
        int txtH = 40;
        int totW = imageW * 2 + arrowW;
        int totH = imageH + txtH;
        
        Canvas canvas = Canvas.createIfSupported();
        if (canvas == null) {
            return new HTML("Browser does not support HTML5 Canvas!");
        }
        
        canvas.setSize(totW+"px", totH+"px");
        canvas.setCoordinateSpaceWidth(totW);
        canvas.setCoordinateSpaceHeight(totH);
        
        Context2d g2 = canvas.getContext2d();
        
        int x = (imageW - lImage.getWidth())/2;
        int y = (imageH - lImage.getHeight())/2;
        ImageElement ie = ImageElement.as((new Image(lImage.getSafeUri())).getElement());
        g2.drawImage(ie, x, y);
        
        x = (imageW - rImage.getWidth())/2;
        y = (imageH - rImage.getHeight())/2;
        ie = ImageElement.as((new Image(lImage.getSafeUri())).getElement());
        g2.drawImage(ie, imageW + arrowW + x, y);
        
        g2.setLineWidth(.5);
        
        g2.setTextAlign(Context2d.TextAlign.CENTER);
        g2.setTextBaseline(Context2d.TextBaseline.MIDDLE);
        g2.fillText(legend, totW/2, imageH + txtH / 2);
        
        x = imageW + arrowW / 2;
        y = imageW / 2;
        int lw = close == 1 ? 20 : 30;
        if (effect != 0) {
            g2.setStrokeStyle("red");
            g2.setFillStyle("red");
        }
        g2.beginPath();
        g2.moveTo(x-lw, y);
        g2.lineTo(x+lw, y);
        g2.closePath();
        g2.stroke();
        
        if (order != 0) {
            x = imageW + arrowW / 2 + order * lw;
            if (effect == -1) {
                g2.beginPath();
                g2.arc(x,y, 4, 0, Math.PI * 2.0, true);
                g2.closePath();
                g2.fill();
            } else {
                g2.moveTo(x,y);
                g2.beginPath();
                g2.lineTo(x - order * 7, y-4);
                g2.lineTo(x - order * 7, y+4);
                g2.lineTo(x,y);
                g2.closePath();
                g2.fill();
            }
        }
        
        return canvas;
    }
    
    
    
}
