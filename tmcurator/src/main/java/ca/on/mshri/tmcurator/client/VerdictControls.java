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
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class VerdictControls extends BorderLayoutContainer{

    private static final BorderLayoutData scaffoldLayout = new BorderLayoutData(40);
    
    private Canvas canvas;
    
    private String action = "actiontype"; 
    private String g1Sym = "", g2Sym = "";
    
    private Type g1Type = Type.UNKNOWN, g2Type = Type.UNKNOWN;
    
    private Order order = Order.NONE;
    private Effect effect = Effect.ACTIVATE;
    private boolean close = false;
    
    public VerdictControls() {
                
        BorderLayoutData borderData = new BorderLayoutData(300);
        
        HBoxLayoutContainer buttonPanel = new HBoxLayoutContainer();
        buttonPanel.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.STRETCH);
        
        //TODO: implement verdict control functionality
        buttonPanel.add(new TextButton("Switch", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                order = order.flip();
                repaint();
            }
        }), BoxConfig.FLEX_MARGIN);
        
        buttonPanel.add(new TextButton("Negate"), BoxConfig.FLEX_MARGIN);
        
        buttonPanel.add(new TextButton("Action", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                ActionSelectorDialog.getInstance().show(VerdictControls.this);
            }
            
        }), BoxConfig.FLEX_MARGIN);
        
        setEastWidget(buttonPanel, borderData);
        
        ContentPanel imageBox = new ContentPanel();
        imageBox.setHeaderVisible(false);
        CenterLayoutContainer imageBoxCenter = new CenterLayoutContainer();
        
        canvas = Canvas.createIfSupported();
        if (canvas != null) {
            canvas.setSize("150px", "70px");
            imageBoxCenter.setWidget(canvas);
        } else {
            imageBoxCenter.setWidget(new HTML("Browser does not support HTML5 Canvas!"));
        }
                
        imageBox.add(imageBoxCenter);
        setCenterWidget(imageBox);
        
        
    }
    
    public void setGenePair(String g1sym, String g2sym) {
        this.g1Sym = g1sym;
        this.g2Sym = g2sym;
        repaint();
    }
    
    public void configure(Map<String,String> data, String g1sym, String g2sym) {
        
        this.g1Sym = g1sym;
        this.g2Sym = g2sym;
        this.g1Type = Type.fromString(data.get("type1"));
        this.g2Type = Type.fromString(data.get("type2"));
        this.action = data.get("actionType");
                
        try {
            int orderInt = Integer.parseInt(data.get("updown"));
            order = Order.fromInt(orderInt);
            effect = Effect.fromInt(Integer.parseInt(data.get("effect")));
            close = Integer.parseInt(data.get("close_connection")) == 1;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Attributes in mention have wrong format.",e);
        }
        
        if (!data.get("upstream").equalsIgnoreCase(g1Sym)) {
            order = order.flip();
        }
                
        repaint();
    }


    private void repaint() {
        
        if (canvas == null) {
            return;
        }
        
        int imageW = 40;
        int imageH = 30;
        int arrowW = 70;
        int txtH = 40;
        int totW = imageW * 2 + arrowW;
        int totH = imageH + txtH;
        
        canvas.setCoordinateSpaceWidth(totW);
        canvas.setCoordinateSpaceHeight(totH);
        
        Context2d g2 = canvas.getContext2d();
        
        g2.setFillStyle("white");
        g2.fillRect(0, 0, totW, totH);
        
        g2.setFillStyle("black");
        g2.setLineWidth(.5);
        
        ImageResource lImage = g1Type.getImage();
        ImageResource rImage = g2Type.getImage();
        
        int x = (imageW - lImage.getWidth())/2;
        int y = (imageH - lImage.getHeight())/2;
        ImageElement ie = ImageElement.as((new Image(lImage.getSafeUri())).getElement());
        g2.drawImage(ie, x, y);
        
        x = (imageW - rImage.getWidth())/2;
        y = (imageH - rImage.getHeight())/2;
        ie = ImageElement.as((new Image(lImage.getSafeUri())).getElement());
        g2.drawImage(ie, imageW + arrowW + x, y);
        
        String legend = g1Sym + " - \"" + action + "\" - " + g2Sym;
        
        g2.setTextAlign(Context2d.TextAlign.CENTER);
        g2.setTextBaseline(Context2d.TextBaseline.MIDDLE);
        g2.fillText(legend, totW/2, imageH + txtH / 2);
        
        x = imageW + arrowW / 2;
        y = imageW / 2;
        int lw = close ? 20 : 30;
        if (effect != Effect.ACTIVATE) {
            g2.setStrokeStyle("red");
            g2.setFillStyle("red");
        }
        g2.beginPath();
        g2.moveTo(x-lw, y);
        g2.lineTo(x+lw, y);
        g2.closePath();
        g2.stroke();
        
        if (order != Order.NONE) {
            x = imageW + arrowW / 2 + order.mod() * lw;
            if (effect == Effect.INHIBIT) {
                g2.beginPath();
                g2.arc(x,y, 4, 0, Math.PI * 2.0, true);
                g2.closePath();
                g2.fill();
            } else {
                g2.moveTo(x,y);
                g2.beginPath();
                g2.lineTo(x - order.mod() * 7, y-4);
                g2.lineTo(x - order.mod() * 7, y+4);
                g2.lineTo(x,y);
                g2.closePath();
                g2.fill();
            }
        }
    }

    void setAction(String a) {
        //TODO: Actions should also determine 'close' and 'effect' values
        action = a;
        repaint();
    }

    String getAction() {
        return action;
    }
    
    private static enum Order {
        
        FWD(1),BCK(-1),NONE(0);
        
        private int mod;
        
        private Order(int mod) {
            this.mod = mod;
        }
        
        static Order fromInt(int i) {
            if (i > 0) {
                return FWD;
            } else if (i < 0) {
                return BCK;
            } else {
                return NONE;
            }
        }
        
        Order flip() {
            return fromInt(mod()*-1);
        }
        
        int mod() {
            return mod;
        }
    }
    
    private static enum Effect {
        
        INHIBIT,ACTIVATE,ENHANCE;
        
        static Effect fromInt(int i) {
            if (i > 0) {
                return ENHANCE;
            } else if (i < 0) {
                return INHIBIT;
            } else {
                return ACTIVATE;
            }
        }
    }
    
    private static enum Type {
        PROTEIN(Resources.INSTANCE.protein()), GENE(Resources.INSTANCE.gene()), UNKNOWN(Resources.INSTANCE.unknown());
        private ImageResource r;
        private Type(ImageResource r) {
            this.r = r;
        }
        static Type fromString(String s) {
            if (s.equalsIgnoreCase("protein")) {
                return PROTEIN;
            } else if (s.equalsIgnoreCase("gene")) {
                return GENE;
            } else {
                return UNKNOWN;
            }
        }
        private ImageResource getImage() {
            return r;
        }
    }
    
    
}
