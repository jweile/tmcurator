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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author Jochen Weile <jochenweile@gmail.com>
 */
public class Config implements IsSerializable {
    
    private int quota;
    
    private int offset;
    
    private boolean approvalEnabled;

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isApprovalEnabled() {
        return approvalEnabled;
    }

    public void setApprovalEnabled(boolean approvalEnabled) {
        this.approvalEnabled = approvalEnabled;
    }
    
    
    
}
