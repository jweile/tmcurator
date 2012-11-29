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

import ca.on.mshri.tmcurator.shared.Action;
import ca.on.mshri.tmcurator.shared.Effect;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.TextArea;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class VerdictControls extends BorderLayoutContainer{

    private static final BorderLayoutData scaffoldLayout = new BorderLayoutData(40);
    
    private static final Action ROOT = new Action("actiontype", "DECOY", Effect.ACTIVATE, false, false);
    
    private Canvas canvas;
    
    private TextArea commentBox;
    
    private Action action = ROOT; 
    private String g1Sym = "", g2Sym = "";
    
    private EntityType g1Type = EntityType.UNKNOWN, g2Type = EntityType.UNKNOWN;
    
    private Order order = Order.NONE;
    
    private boolean negative = false;
    
    private boolean invalid = false;
    
    private CheckBox approvalBox;
    
    public VerdictControls() {
                
        Margins margins = new Margins(5,5,5,5);
        
        HBoxLayoutContainer buttonPanel = new HBoxLayoutContainer();
        buttonPanel.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.STRETCH);
        
        TextButton button = new TextButton("Switch", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                order = order.flip();
                approvalBox.setValue(true);
                repaint();
            }
        });
//        button.setIcon(Resources.INSTANCE.switching());
//        button.setIconAlign(IconAlign.TOP);
        buttonPanel.add(button, BoxConfig.FLEX_MARGIN);
        
        button = new TextButton("Entities", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                approvalBox.setValue(true);
                EntitySelectorDialog.getInstance().show(VerdictControls.this);
            }
            
        });
//        button.setIcon(Resources.INSTANCE.entity());
//        button.setIconAlign(IconAlign.TOP);
        buttonPanel.add(button, BoxConfig.FLEX_MARGIN);
        
        button = new TextButton("Action", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                approvalBox.setValue(true);
                ActionSelectorDialog.getInstance().show(VerdictControls.this);
            }
            
        });
//        button.setIcon(Resources.INSTANCE.action());
//        button.setIconAlign(IconAlign.TOP);
        buttonPanel.add(button, BoxConfig.FLEX_MARGIN);
        
        button = new TextButton("Negate", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                negative = !negative;
                approvalBox.setValue(true);
                repaint();
            }
            
        });
//        button.setIcon(Resources.INSTANCE.negate());
//        button.setIconAlign(IconAlign.TOP);
        buttonPanel.add(button, BoxConfig.FLEX_MARGIN);
        
        button = new TextButton("Invalid", new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                invalid = !invalid;
                approvalBox.setValue(true);
                repaint();
            }
            
        });
//        button.setIcon(Resources.INSTANCE.invalid());
//        button.setIconAlign(IconAlign.TOP);
        buttonPanel.add(button, BoxConfig.FLEX_MARGIN);
        
        BorderLayoutData eastLayout = new BorderLayoutData(350);
        setEastWidget(buttonPanel, eastLayout);
        
        ContentPanel imageBox = new ContentPanel();
        imageBox.setHeaderVisible(false);
        VBoxLayoutContainer imageBoxCenter = new VBoxLayoutContainer();
        imageBoxCenter.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.CENTER);
        
        canvas = Canvas.createIfSupported();
        if (canvas != null) {
            canvas.setSize("150px", "70px");
            imageBoxCenter.add(canvas);
        } else {
            imageBoxCenter.add(new HTML("Browser does not support HTML5 Canvas!"));
        }
        
        //add mouseclick listener to canvas
        canvas.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (event.getY() < 30) {
                    if (event.getX() < 40) {
                        //left entity
                        g1Type = g1Type.cycle();
                    } else if (event.getX() > 110) {
                        //right entity
                        g2Type = g2Type.cycle();
                    } else {
                        //arrow
                        order = order.flip();
                    }
                    repaint();
                } else {
                    ActionSelectorDialog.getInstance().show(VerdictControls.this);
                }
                approvalBox.setValue(true);
            }
        });
                
        imageBox.add(imageBoxCenter);
        BorderLayoutData centerLayout = new BorderLayoutData();
        centerLayout.setMargins(margins);
        setCenterWidget(imageBox,centerLayout);
        
        commentBox = new TextArea();
        commentBox.setEmptyText("Comments...");
        
        BorderLayoutData westLayout = new BorderLayoutData(200);
        westLayout.setMargins(margins);
        
        setWestWidget(commentBox, westLayout);
        
        
    }
    
    public void setGenePair(String g1sym, String g2sym) {
        this.g1Sym = g1sym;
        this.g2Sym = g2sym;
        repaint();
    }

    public EntityType getG1Type() {
        return g1Type;
    }

    public EntityType getG2Type() {
        return g2Type;
    }

    public void setEntityTypes(EntityType t1, EntityType t2) {
        this.g1Type = t1;
        this.g2Type = t2;
        repaint();
    }

    public Order getOrder() {
        return order;
    }
    
    public String getComment() {
        if (commentBox != null && commentBox.getText() != null 
                && !commentBox.getText().equals(commentBox.getEmptyText())) {
            return commentBox.getText();
        } else {
            return "";
        }
    }
    
    
    public void configure(Map<String,String> data, String g1sym, String g2sym) {
        
        this.g1Sym = g1sym;
        this.g2Sym = g2sym;
        this.g1Type = EntityType.fromString(data.get("type1"));
        this.g2Type = EntityType.fromString(data.get("type2"));
                
        try {
            int orderInt = Integer.parseInt(data.get("updown"));
            order = Order.fromInt(orderInt);
            Effect effect = Effect.fromInt(Integer.parseInt(data.get("effect")));
            boolean close = Integer.parseInt(data.get("close_connection")) == 1;
            this.negative = data.get("negative").equals("1");
            this.invalid = data.get("negative").equals("2");
            this.action = new Action(data.get("actionType"), "DECOY", effect, close, orderInt != 0);
            String comment = data.get("comment");
            if (comment != null && comment.length() > 0) {
                commentBox.setText(comment);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Attributes in mention have wrong format.",e);
        }
        
        //FIXME: direction is not recovered correctly after loading?
        if (data.get("upstream").equalsIgnoreCase(g2Sym)) {
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
        g2.setTextAlign(Context2d.TextAlign.CENTER);
        g2.setTextBaseline(Context2d.TextBaseline.MIDDLE);
        g2.setLineWidth(.5);
        
        if (invalid) {
            g2.setFillStyle("lightgray");
            g2.fillRect(0,0,totW, totH);
            g2.setFillStyle("black");
            g2.fillText("Invalid extraction", totW/2, totH / 2);
            return;
        }
        
        g2.setFillStyle("white");
        g2.fillRect(0, 0, totW, totH);
        
        g2.setFillStyle("black");
        
        ImageResource lImage = g1Type.getImage();
        ImageResource rImage = g2Type.getImage();
        
        //FIXME: Images are only displayed after second rendering/loading
        int x = (imageW - lImage.getWidth())/2;
        int y = (imageH - lImage.getHeight())/2;
        ImageElement ie = ImageElement.as((new Image(lImage.getSafeUri())).getElement());
        g2.drawImage(ie, x, y);
        
        x = (imageW - rImage.getWidth())/2;
        y = (imageH - rImage.getHeight())/2;
        ie = ImageElement.as((new Image(rImage.getSafeUri())).getElement());
        g2.drawImage(ie, imageW + arrowW + x, y);
        
        String legend = format(g1Sym,g1Type) + " - \"" + 
                (action != null ? action.getName() : "?") + 
                "\" - " + format(g2Sym,g2Type);
        
        g2.fillText(legend, totW/2, imageH + txtH / 2);
        
        if (action != null) {
            x = imageW + arrowW / 2;
            y = imageH / 2;
            int lw = action.isClose() ? 20 : 30;
            if (action.getEffect() != Effect.ACTIVATE) {
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
                if (action.getEffect() == Effect.INHIBIT) {
                    g2.beginPath();
                    g2.moveTo(x, y-4);
                    g2.lineTo(x, y+4);
                    g2.closePath();
                    g2.stroke();
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
            
            //negation
            if (negative) {
                g2.setStrokeStyle("red");
                g2.setLineWidth(2);
                //center point
                x = totW/2;
                y = imageH/2;
                
                int xOffset = (int)(10.0 * Math.cos(.25 * Math.PI));
                int yOffset = (int)(10.0 * Math.sin(.25 * Math.PI));
                
                g2.beginPath();
                g2.arc(x, y, 10, 0, Math.PI * 2.0, true);
                g2.closePath();
                g2.stroke();
                
                g2.beginPath();
                g2.moveTo(x - xOffset, y - yOffset);
                g2.lineTo(x + xOffset , y + yOffset);
                g2.closePath();
                g2.stroke();
            }
        }
    }

    void setAction(Action a) {
        action = a;
        order = a.isDirected() ? Order.FWD : Order.NONE;
        repaint();
    }

    Action getAction() {
        return action;
    }

    public boolean isNegative() {
        return negative;
    }

    public boolean isInvalid() {
        return invalid;
    }
    
    

    private String format(String sym, EntityType entityType) {
        if (entityType == EntityType.PROTEIN) {
            StringBuilder b = new StringBuilder();
            b.append(Character.toUpperCase(sym.charAt(0)));
            b.append(sym.substring(1, sym.length()));
            return b.toString();
        } else {
            return sym.toUpperCase();
        }
    }

    void linkApprovalBox(CheckBox approvalBox) {
        this.approvalBox = approvalBox;
    }
    
    
}
