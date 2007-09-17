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
 * HarvestingDataverseServiceBean.java
 *
 * Created on April 5, 2007, 10:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.harvard.hmdc.vdcnet.vdc;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Ellen Kraffmiller
 */
@Stateless
public class HarvestingDataverseServiceBean implements edu.harvard.hmdc.vdcnet.vdc.HarvestingDataverseServiceLocal {
    @EJB VDCServiceLocal vdcService;
    @PersistenceContext(unitName="VDCNet-ejbPU")
    private EntityManager em;
    
    
    /**
     * Creates a new instance of HarvestingDataverseServiceBean
     */
    public HarvestingDataverseServiceBean() {
    }
    
    public List findAll() {
        return em.createQuery("select object(o) from HarvestingDataverse as o order by o.vdc.name").getResultList();
    }
    
    public HarvestingDataverse find(Long id) {
        HarvestingDataverse hd= em.find(HarvestingDataverse.class,id);
        em.refresh(hd);
        return hd;
    }
    
    public void edit(HarvestingDataverse harvestingDataverse) {
        em.merge(harvestingDataverse);
    }
    
    public void delete(Long hdId){
        HarvestingDataverse hd = em.find(HarvestingDataverse.class, hdId);
        em.refresh(hd);
        
        vdcService.delete(hd.getVdc().getId());
        em.remove(hd);
        
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void setHarvestingNow(Long hdId, boolean harvestingNow) {
        HarvestingDataverse hd = em.find(HarvestingDataverse.class,hdId);
        em.refresh(hd);
        hd.setHarvestingNow(harvestingNow);
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void setLastHarvestTime(Long hdId, Date lastHarvestTime) {
        HarvestingDataverse hd = em.find(HarvestingDataverse.class,hdId);
        em.refresh(hd);
        hd.setLastHarvestTime(lastHarvestTime);
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean getHarvestingNow(Long hdId) {
        HarvestingDataverse hd = em.find(HarvestingDataverse.class,hdId);
        em.refresh(hd);
        return hd.isHarvestingNow();
    }
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Date getLastHarvestTime(Long hdId) {
        HarvestingDataverse hd = em.find(HarvestingDataverse.class,hdId);
        em.refresh(hd);
        return hd.getLastHarvestTime();
    }
    
    
    public void resetHarvestingStatus() {
        List harvestingDataverses = findAll();
        for (Iterator it = harvestingDataverses.iterator(); it.hasNext();) {
            HarvestingDataverse hd = (HarvestingDataverse)it.next();
            hd.setHarvestingNow(false);
        }
    }
}
