package requestextractor;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.List;

import requestextractor.RequestExtractor.LOCATIONS;

/**
 *
 * @author hiznis
 */
public class Request {
    //staff member details
    private String title;
    private String forename;
    private String surname;
    private String securityBadge;
    private String startDate;
    private String endDate;
    
    //staff role details
    private String profession_code;
    private boolean isConsultant = false;
    private String specialty_code;
    
    private List roleCollection = new ArrayList();
    private List locationCollection = new ArrayList();
    
    //TIMS account
    private boolean addNewAccount = false;
    private boolean editExistingAccount = false;
    private boolean removeExistingAccount = false;
    
    //TIMS password
    private boolean newPasswordNeeded = false;
    private boolean resetPassword = false;
    private boolean removePassword = false;
    
    //Password privilage
    private RequestExtractor.REQUEST_ACTION requestAction;

    private RequestExtractor.PASSWORD_PRIVILAGE passwordPrivilage;
    
    //Requesting location
    private String requestLocation;
    
    private String timsInternalID = "";
    
    private String username = "";
    private String password = "";
    
    
    private boolean checkDatabaseForExistingStaff(String securityBadge, String surname, String forename) {
           return false;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the forename
     */
    public String getForename() {
        return forename;
    }

    public String getSQLFriendlyForename() {
        return forename.replace("'","''");
    }
    
    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    public String getTidiedSurname() {
        return surname.replace("'", "").replace(" ", "").replace("-", "");
    }
    
    public String getSQLFriendlySurname() {
        return surname.replace("'","''");
    }
    /**
     * @return the securityBadge
     */
    public String getSecurityBadge() {
        return securityBadge;
    }

    /**
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * @return the endDate
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * @return the profession_code
     */
    public String getProfession_code() {
        return profession_code;
    }

    /**
     * @return the isConsultant
     */
    public boolean isConsultant() {
        return isConsultant;
    }

    /**
     * @return the specialty_code
     */
    public String getSpecialty_code() {
        return specialty_code;
    }

    /**
     * @return the roleCollection
     */
    public List getRoleCollection() {
        return roleCollection;
    }
    
    public void addRole(RequestExtractor.ROLES r) {
        if (r.equals(RequestExtractor.ROLES.SURGEON)){
            roleCollection.add("SURG");
            roleCollection.add("SURGA");
        }
        else if (r.equals(RequestExtractor.ROLES.ANAE)){
            roleCollection.add("ANAE");
            roleCollection.add("ANAEA");
        }
        else if (r.equals(RequestExtractor.ROLES.RADIO)) {
            roleCollection.add("RADIO");
        }
        else if (r.equals(RequestExtractor.ROLES.ANAE_PRAC)) {
            roleCollection.add("ANNRS");
        }
        else if (r.equals(RequestExtractor.ROLES.CIRC_PRAC)) {
            roleCollection.add("CINRS");
        }
        else if (r.equals(RequestExtractor.ROLES.RECOV_PRAC)) {
            roleCollection.add("RCNRS");
        }
        else if (r.equals(RequestExtractor.ROLES.SCRUB_PRAC)) {
            roleCollection.add("SCNRS");
        }
        else if (r.equals(RequestExtractor.ROLES.HAEM_PRAC)) {
            roleCollection.add("HAEMP");
        }
        else if (r.equals(RequestExtractor.ROLES.PERF)) {
            roleCollection.add("PERF");
        }
        else if (r.equals(RequestExtractor.ROLES.TR_PERF)) {
            roleCollection.add("PERFT");
        }
    }
    
    public void AddLocation(RequestExtractor.LOCATIONS loc) {
        if (loc.equals(RequestExtractor.LOCATIONS.CH)) {
            locationCollection.add("9");
        }
        else if (loc.equals(RequestExtractor.LOCATIONS.JR1)) {
            locationCollection.add("2");
        }
        if (loc.equals(RequestExtractor.LOCATIONS.JR2)) {
            locationCollection.add("1");
        }
        if (loc.equals(RequestExtractor.LOCATIONS.WW)) {
            locationCollection.add("17");
            locationCollection.add("18");
            locationCollection.add("19");
        }
        if (loc.equals(RequestExtractor.LOCATIONS.HH)) {
            locationCollection.add("10");
        }
    }

    //checking professions, since in the case of certain profession different
    //actions need to be taken
    public boolean isDoctor(){
        if (profession_code.equals("DOCT"))
            return true;
        else
            return false;
        
    }
    
    public boolean isOperatingTheatrePrac(){
        if (profession_code == "ODP")
            return true;
        else
            return false;
        
    }
    /**
     * @return the locationCollection
     */
    public List getLocationCollection() {
        return locationCollection;
    }

    /**
     * @return the addNewAccount
     */
    public boolean isAddNewAccount() {
        return addNewAccount;
    }

    /**
     * @return the editExistingAccount
     */
    public boolean isEditExistingAccount() {
        return editExistingAccount;
    }
    
    public boolean isRemoveExistingAccount(){
        return removeExistingAccount;
    }

    /**
     * @return the newPasswordNeeded
     */
    public boolean isNewPasswordNeeded() {
        return newPasswordNeeded;
    }

    /**
     * @return the resetPassword
     */
    public boolean isResetPassword() {
        return resetPassword;
    }

    /**
     * @return the removePassowrd
     */
    public boolean isRemovePassowrd() {
        return removePassword;
    }

    /**
     * @return the requestAction
     */
    public RequestExtractor.REQUEST_ACTION getRequestAction() {
        return requestAction;
    }



    /**
     * @return the passwordPrivilage
     */
    public RequestExtractor.PASSWORD_PRIVILAGE getPasswordPrivilage() {
        return passwordPrivilage;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param forename the forename to set
     */
    public void setForename(String forename) {
        this.forename = forename;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @param securityBadge the securityBadge to set
     */
    public void setSecurityBadge(String securityBadge) {
        this.securityBadge = securityBadge;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * @param profession_code the profession_code to set
     */
    public void setProfession_code(String profession_code) {
        this.profession_code = profession_code;
    }

    /**
     * @param isConsultant the isConsultant to set
     */
    public void setIsConsultant(boolean isConsultant) {
        this.isConsultant = isConsultant;
    }

    /**
     * @param specialty_code the specialty_code to set
     */
    public void setSpecialty_code(String specialty_code) {
        this.specialty_code = specialty_code;
    }

    /**
     * @param addNewAccount the addNewAccount to set
     */
    public void setAddNewAccount(boolean addNewAccount) {
        this.addNewAccount = addNewAccount;
    }

    /**
     * @param editExistingAccount the editExistingAccount to set
     */
    public void setEditExistingAccount(boolean editExistingAccount) {
        this.editExistingAccount = editExistingAccount;
    }

    /**
     * @param newPasswordNeeded the newPasswordNeeded to set
     */
    public void setNewPasswordNeeded(boolean newPasswordNeeded) {
        this.newPasswordNeeded = newPasswordNeeded;
    }
    
    /**
     * 
     * @param removeExistingAccount the removeExistingAccount to set
     */
    public void setRemoveExistingAccount(boolean removeExistingAccount) {
        this.removeExistingAccount = removeExistingAccount;
    }

    /**
     * @param resetPassword the resetPassword to set
     */
    public void setResetPassword(boolean resetPassword) {
        this.resetPassword = resetPassword;
    }

    public boolean basicInfoProvided(){
        if (getTitle() == null || getForename() == null || getSurname() == null)
            return false;
        else 
            return true;        
    }
    /**
     * @param removePassowrd the removePassowrd to set
     */
    public void setRemovePassword(boolean removePassowrd) {
        this.removePassword = removePassowrd;
    }

    /**
     * @param requestAction the requestAction to set
     */
    public void setRequestAction(RequestExtractor.REQUEST_ACTION requestAction) {
        this.requestAction = requestAction;
    }



    /**
     * @param passwordPrivilage the passwordPrivilage to set
     */
    public void setPasswordPrivilage(RequestExtractor.PASSWORD_PRIVILAGE passwordPrivilage) {
        this.passwordPrivilage = passwordPrivilage;
    }

    /**
     * @return the timsInternalID
     */
    public String getTimsInternalID() {
        return timsInternalID;
    }

    /**
     * @param timsInternalID the timsInternalID to set
     */
    public void setTimsInternalID(String timsInternalID) {
        this.timsInternalID = timsInternalID;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

     
    public String getRequestingLocation(){
        return this.requestLocation;
    }

    void setRequestingLocation(LOCATIONS requestingLocation) {
        switch(requestingLocation){
            case CH:
                requestLocation = "9";
                break;
            case HH:
                requestLocation = "10";
                break;
            case JR1:
                requestLocation = "2";
                break;
            case JR2:
                requestLocation = "1";
                break;
            case WW:
                requestLocation = "17,18,19";
                break;
                
        }

    }   
}
