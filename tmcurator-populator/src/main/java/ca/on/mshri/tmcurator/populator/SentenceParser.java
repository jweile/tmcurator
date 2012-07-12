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
package ca.on.mshri.tmcurator.populator;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author jweile
 */
public class SentenceParser {
    
//    private final Logger log = Logger.getLogger(Parser.class.getName());
    private HashSet<String> sgdSet;

    public SentenceParser() {
        sgdSet = new SGDRefReader().read();
    }
    
    
    
    public UpdateCollection parse(InputStream in) {
        
        UpdateCollection updates = new UpdateCollection();
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        
        try {
            
            doc = builder.build(new BufferedInputStream(in));
            
        } catch (Exception ex) {
            throw new RuntimeException("Unable to parse XML stream.",ex);
        } 
        
        Element pairTag = doc.getRootElement();
        Map<String,String> pairAs = attributes(pairTag);
        String pairId = String.format("%s-%s", pairAs.get("g1Id"), pairAs.get("g2Id"));
        
        String qry = String.format("INSERT INTO pairs VALUES ('%s', '%s', '%s', '%s', '%s');",
                pairId,
                pairAs.get("g1Id"),
                pairAs.get("g2Id"),
                pairAs.get("g1Sym"),
                pairAs.get("g2Sym"));
        updates.addPair(pairId, qry);
        
        for (Object o: pairTag.getChildren("article")) {
            Element articleTag = (Element)o;
            
            Map<String,String> articleAs = attributes(articleTag);
            
            String citation = buildCitation(articleAs);
            
            int nonSGD = (!sgdSet.contains(articleAs.get("pmid"))) ? 1 : 0;
            
            qry = String.format("INSERT INTO articles VALUES ('%s', '%s', '%s', '%s');",
                    articleAs.get("arid"),
                    articleAs.get("pmid"),
                    citation,
                    nonSGD);
            updates.addArticle(articleAs.get("pmid"), qry);
            
            for (Object o2: articleTag.getChildren("actionMention")) {
                
                Element actionTag = (Element)o2;
                
                Map<String,String> actionAs = attributes(actionTag);
                
                Element sTag = actionTag.getChild("S");
                String sentence = "";
                if (sTag != null) {
                    Element sentenceTag = sTag.getChild("MARKEDUP_TEXT");
                    sentence = processSentence(sentenceTag, pairAs.get("g1Sym"), pairAs.get("g2Sym"));
                }

                qry = String.format("INSERT INTO mentions VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                        actionAs.get("amid"),
                        pairId,
                        articleAs.get("arid"),
                        actionAs.get("actionType"),
                        actionAs.get("upstreamTermMentionStr"),
                        actionAs.get("downstreamTermMentionStr"),
                        actionAs.get("typeFirst"),
                        actionAs.get("typeSecond"),
                        sentence,
                        actionAs.get("negative").equalsIgnoreCase("true") ? 1 : 0);
                updates.addMention(actionAs.get("amid"), qry);
                    
            }
            
        }
        
        return updates;
        
    }
    
    private Map<String,String> attributes(Element e) {
        Map<String,String> map = new HashMap<String, String>();
        for (Object o : e.getAttributes()) {
            Attribute a = (Attribute)o;
            map.put(a.getName(), a.getValue());
        }
        return map;
    }

    private String buildCitation(Map<String, String> a) {
        
        String year = a.get("pubdate").split("/")[2];
        
        StringBuilder b = new StringBuilder();
        
        b.append(a.get("journal"))
         .append(" (")
         .append(year)
         .append("), ")
         .append(a.get("volume"))
         .append("(")
         .append(a.get("issue"))
         .append("), pp")
         .append(a.get("page"));
        
        return b.toString();
        
    }
    
    private String processSentence(Element sentenceTag, String sym1, String sym2) {
        
        StringBuilder b = new StringBuilder();
        
        for (Object o: sentenceTag.getContent()) {
            
            if (o instanceof Text) {
                
                Text text = (Text)o;
                b.append(text.getText());
                
            } else if (o instanceof Element) {
                //<phr p="1.0" sem="mrna" t="CHA1" tp="1">CHA1 mRNA</phr>
                Element e = (Element) o;
                if (e.getAttributeValue("t").equalsIgnoreCase(sym1)) {
                    b.append("<span class=\"sym1\">");
                } else if (e.getAttributeValue("t").equalsIgnoreCase(sym2)) {
                    b.append("<span class=\"sym2\">");
                } else {
                    b.append("<span class=\"symOther\">");
                }
                b.append(e.getText())
                     .append("</span>");
                
            }
        }
        
        //escape quote characters
        return b.toString().replaceAll("'", "''");
        
    }
    
}
