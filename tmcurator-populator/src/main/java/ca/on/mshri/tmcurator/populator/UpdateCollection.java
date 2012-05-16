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
package ca.on.mshri.tmcurator.populator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jweile
 */
public class UpdateCollection implements Iterable<String> {
    
    private Map<String,String> pairs = new HashMap<String,String>();
    private Map<String,String> articles = new HashMap<String,String>();
    private Map<String,String> mentions = new HashMap<String,String>();

    void addPair(String pairId, String qry) {
        pairs.put(pairId, qry);
    }

    void addArticle(String get, String qry) {
        articles.put(get, qry);
    }

    void addMention(String get, String qry) {
        mentions.put(get, qry);
    }

    @Override
    public Iterator<String> iterator() {
        
        List<String> list = new ArrayList<String>();
        
        list.addAll(pairs.values());
        list.addAll(articles.values());
        list.addAll(mentions.values());
        
        return list.iterator();
    }

    public void addAll(UpdateCollection other) {
        
        pairs.putAll(other.pairs);
        articles.putAll(other.articles);
        mentions.putAll(other.mentions);
        
    }
    
}
