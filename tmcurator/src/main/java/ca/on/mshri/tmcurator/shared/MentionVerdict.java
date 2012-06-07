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
public class MentionVerdict extends Verdict {

    private int mentionId;
    
    MentionVerdict() {
        //for serialization
    }

    public MentionVerdict(int mentionId, int pairId, String action, int order, String g1Type, String g2Type, boolean negative, String comment) {
        super(pairId, action, order, g1Type, g2Type, negative, comment);
        this.mentionId = mentionId;
    }
    
    public int getMentionId() {
        return mentionId;
    }
    
    
    
}
