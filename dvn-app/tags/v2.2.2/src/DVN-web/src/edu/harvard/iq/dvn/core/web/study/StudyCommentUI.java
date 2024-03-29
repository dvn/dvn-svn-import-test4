/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.iq.dvn.core.web.study;

import edu.harvard.iq.dvn.core.admin.VDCUser;
import edu.harvard.iq.dvn.core.study.StudyComment;
import edu.harvard.iq.dvn.core.util.PropertyUtil;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import java.util.Iterator;
import java.util.List;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author wbossons
 */
public class StudyCommentUI {

    protected boolean InFlaggedByUsers;
    protected StudyComment studyComment;
    protected String flaggedByUserNames = new String("");
    protected VDCUser user = null;


    public StudyCommentUI(StudyComment studyComment) {
        this.studyComment = studyComment;
    }

    public StudyCommentUI(StudyComment studyComment, VDCUser currentUser) {
        this.studyComment = studyComment;
        this.user         = VDCBaseBean.getVDCSessionBean().getUser();
    }

    public String getFlaggedByUserNames() {
        List<VDCUser> vdcUsers = (List<VDCUser>)studyComment.getFlaggedByUsers();
        Iterator iterator      = vdcUsers.iterator();
        while (iterator.hasNext()) {
            VDCUser vdcuser = (VDCUser)iterator.next();
            if (vdcUsers.indexOf(vdcuser) > 0)
                flaggedByUserNames += ", ";
            flaggedByUserNames += vdcuser.getUserName();
        }
        return flaggedByUserNames;
    }

    protected String flaggedByAccountLinks = new String();

    public String getFlaggedByAccountLinks() {
        List<VDCUser> vdcUsers = (List<VDCUser>)studyComment.getFlaggedByUsers();
        Iterator iterator      = vdcUsers.iterator();
        while (iterator.hasNext()) {
            VDCUser vdcuser = (VDCUser)iterator.next();
            if (vdcUsers.indexOf(vdcuser) > 0)
                flaggedByAccountLinks += ", ";
            flaggedByAccountLinks += "<a href=\"" + baseUrl + "/faces/login/AccountPage.xhtml?userId=" +
                                        vdcuser.getId() + "&vdcId=" + 
                                        studyComment.getStudyVersion().getStudy().getOwner().getId() + "\">" +
                                        vdcuser.getUserName() +
                                        "</a>";
        }
        return flaggedByAccountLinks;
    }

    public StudyComment getStudyComment() {
        return this.studyComment;
    }

    protected String baseUrl = new String();

    public String getBaseUrl() {
        FacesContext context            = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletRequest request      = (HttpServletRequest) externalContext.getRequest();
        baseUrl = request.getProtocol().substring(0, request.getProtocol().indexOf("/")).toLowerCase() + "://" +
                            getHostUrl() + request.getContextPath();
        return baseUrl;
    }

    public String getStudyPageLink() {
        String studyPageLink = new String("");
        studyPageLink = getBaseUrl() + "/dv/" +
                            studyComment.getStudyVersion().getStudy().getOwner().getAlias() +
                            "/faces/study/StudyPage.xhtml?globalId=" +
                            studyComment.getStudyVersion().getStudy().getGlobalId();
        return studyPageLink;
    }
    
    protected String userAccountPageLink = new String();

    public String getUserAccountPageLink() {
        userAccountPageLink = getBaseUrl() + "/faces/login/AccountPage.xhtml?userId=" +
                                studyComment.getCommentCreator().getId() +
                                "&vdcId=" + studyComment.getStudyVersion().getStudy().getOwner().getId();
        return userAccountPageLink;
    }

    protected String studyTabLink = new String();
    
    public String getStudyTabLink() {
        studyTabLink = getStudyPageLink() + "&tab=catalog";
        return studyTabLink;
    }

    protected String commentsTabLink = new String();

     public String getCommentsTabLink() {
        commentsTabLink = getStudyPageLink() + "&tab=comments#comment" + studyComment.getId();
        return commentsTabLink;
    }

     /**
     *  for url of dataverse home page.
     * @return hostUrl (based on inet Address)
     */
    private String getHostUrl() {
        return PropertyUtil.getHostUrl();
    }



    /**
     * Get the value of InFlaggedByUsers
     *
     * @return the value of InFlaggedByUsers
     */
    public boolean isInFlaggedByUsers() {
        if (getFlaggedByUserNames().indexOf(VDCBaseBean.getVDCSessionBean().getUser().getUserName()) != -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the value of InFlaggedByUsers
     *
     * @param InFlaggedByUsers new value of InFlaggedByUsers
     */
    public void setInFlaggedByUsers(boolean InFlaggedByUsers) {
        this.InFlaggedByUsers = InFlaggedByUsers;
    }

    protected boolean reportAbuseEnabled;

    /**
     * Get the value of reportAbuseEnabled
     *
     * @return the value of reportAbuseEnabled
     */
    public boolean isReportAbuseEnabled() {
        if (studyComment.getStatus().toString().equals("FLAGGED") && isInFlaggedByUsers()) {
            reportAbuseEnabled = false;
        } else {
            reportAbuseEnabled = true;
        }
        return reportAbuseEnabled;
    }



    /**
     * Set the value of reportAbuseEnabled
     *
     * @param reportAbuseEnabled new value of reportAbuseEnabled
     */
    public void setReportAbuseEnabled(boolean reportAbuseEnabled) {
        this.reportAbuseEnabled = reportAbuseEnabled;
    }


}
