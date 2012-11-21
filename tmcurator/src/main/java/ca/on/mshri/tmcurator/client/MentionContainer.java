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

import ca.on.mshri.tmcurator.shared.MentionVerdict;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.Style.Side;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class MentionContainer extends ContentPanel {
    
    private VerdictControls verdictControls;
    
    private String g1Sym, g2Sym;
    
    private Map<String,String> mention;
    
    private int pairId;
    
    private CheckBox approvalBox;
    
    public MentionContainer(Map<String,String> mention, String g1sym, String g2sym, int pairId) {
        
        this.mention = mention;
        this.g1Sym = g1sym;
        this.g2Sym = g2sym;
        this.pairId = pairId;
        
        setCollapsible(true);
        setHeight("200px");
        
        StringBuilder b = new StringBuilder()
                .append("<span style=\"font-size:10px\">")
                .append("Sentence ID: ")
                .append(mention.get("mentionId"))
                .append(", PMID: ")
                .append(mention.get("pmid"));
        
        if (mention.get("non_sgd").equals("1")) {
            b.append("<span style=\"color:red\">")
                    .append(" -- Warning: Not an approved SGD publication!")
                    .append("</span>");
        }
                
        b.append("</span>");
        
        setHeadingHtml(b.toString());

        BorderLayoutContainer borderLayout = new BorderLayoutContainer();

        b = new StringBuilder();
        b.append("http://www.ncbi.nlm.nih.gov/entrez/query.fcgi")
         .append("?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=")
         .append(mention.get("pmid"));
        final String url = b.toString();

        TextButton pmButton = new TextButton("Pubmed");
        pmButton.setIcon(Resources.INSTANCE.document());
        pmButton.setIconAlign(IconAlign.TOP);
        pmButton.addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                Window.open(url, "_blank", "");
            }
        });
        
        ToolTipConfig tt = new ToolTipConfig("Citation", mention.get("citation"));
        tt.setCloseable(true);
        tt.setAnchor(Side.LEFT);
        pmButton.setToolTipConfig(tt);

        VBoxLayoutContainer westContainer = new VBoxLayoutContainer();
        westContainer.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);
        westContainer.add(pmButton, BoxConfig.MARGIN);
        
        approvalBox = new CheckBox();
        if (TmCurator.getInstance().getConfig().isApprovalEnabled()) {
            approvalBox.setBoxLabel("Approve");
            String hasVerdict = mention.get("hasVerdict");
            approvalBox.setValue(hasVerdict != null && hasVerdict.equals("1"));
            BoxLayoutData flex = new BoxLayoutData(new Margins(5,5,5,5));
            flex.setFlex(1);
            westContainer.add(approvalBox, flex);
        }

        borderLayout.setWestWidget(westContainer);

        VBoxLayoutContainer sentenceAndVerdictContainer = new VBoxLayoutContainer();
        sentenceAndVerdictContainer.setVBoxLayoutAlign(VBoxLayoutContainer.VBoxLayoutAlign.STRETCH);

        ContentPanel textBox = new ContentPanel();
        textBox.setHeaderVisible(false);
        textBox.setWidget(
                new ScrollPanel(
                        new HTML(mention.get("sentence"))
                        )
                );
        textBox.setHeight("2px");//don't ask me. it fixes the bug. no idea why.

        sentenceAndVerdictContainer.add(textBox, BoxConfig.FLEX_MARGIN);

        verdictControls = new VerdictControls();
        verdictControls.configure(mention,g1sym,g2sym);

        sentenceAndVerdictContainer.add(verdictControls, BoxConfig.FLEX_MARGIN);

        borderLayout.setCenterWidget(sentenceAndVerdictContainer);

        add(borderLayout);
    }
    
    public MentionVerdict extractData() {
        String action = verdictControls.getAction().getName();
        int order = verdictControls.getOrder().mod();
        String type1 = verdictControls.getG1Type().name();
        String type2 = verdictControls.getG2Type().name();
        boolean negative = verdictControls.isNegative();
        String comment = verdictControls.getComment();
        
        int mentionId = Integer.parseInt(mention.get("mentionId"));
        
        MentionVerdict v = new MentionVerdict(mentionId, pairId, action, order, type1, type2, negative, comment);
        v.setInvalid(verdictControls.isInvalid());
        return v;
    }
    
    public boolean isApproved() {
        return approvalBox.getValue();
    }
    
    
    
}
