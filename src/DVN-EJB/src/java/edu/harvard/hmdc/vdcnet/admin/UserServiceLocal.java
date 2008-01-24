/*
 * Dataverse Network - A web application to distribute, share and analyze quantitative data.
 * Copyright (C) 2007
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation,Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

/*
 * UserServiceLocal.java
 *
 * Created on September 21, 2006, 1:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.harvard.hmdc.vdcnet.admin;

import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author roberttreacy
 */
@Local
public interface UserServiceLocal {
   

    public VDCUser find(Long id);

    public List findAll();
    
    public void remove(Long id);
    
    public VDCUser findByUserName(String userName);
    
    public VDCUser findByUserName(String userName, boolean activeOnly);

    public void addVdcRole(Long userId, Long vdcId, String roleName);
    
    public void addCreatorRequest(Long userId);

    public void addContributorRequest(Long userId, Long vdcId);
    
    public void setActiveStatus(Long userId, boolean active);
    
    public boolean validatePassword(Long userId, String password);
    
    public String encryptPassword(String plainText);
    
    public void clearAgreedTermsOfUse(); 
    
    public void setAgreedTermsOfUse(Long userId, boolean agreed);
    
    public void makeCreator(Long userId);
    
    public boolean hasUserCreatedDataverse(Long userId);
    
}
