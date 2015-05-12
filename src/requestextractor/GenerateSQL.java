package requestextractor;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import requestextractor.DataAccess;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author hiznis
 */
public class GenerateSQL {

    //private StringBuilder script = null;
    private Request request = null;
    private DataAccess data = null;
    private PrintStream ps = null;
    
    public GenerateSQL(Request r, DataAccess da, File scriptFile) throws FileNotFoundException {
       setRequest(r);
       setData(da);
       ps = new PrintStream(new FileOutputStream(scriptFile, true));
       //System.setOut(new PrintStream(new FileOutputStream(scriptFile, true)));
       ps.println("/**** requestextractor.Request for: " + r.getForename() + " " + r.getSurname().toString() + "****/");
       //script = new StringBuilder("-- ***** SQL script for: ").append(getRequest().getForename()).append(" ").append(getRequest().getSurname().toUpperCase());        
    }

    public void StartTransaction(){
        //begin transation statement
        ps.println("SET XACT_ABORT ON");
        ps.println("USE [theatre_prod]");
        ps.println("BEGIN TRAN");
    }
    
    public void EndTransaction(){
        ps.println("COMMIT TRAN");
        ps.println("IF @@ERROR <> 0");
        ps.println("ROLLBACK TRAN");
        ps.println("/**************** End of request *****************/");
    }
    public void AddTimsStaffDetails(){    

        ps.println("\n--adding staff record to TIMS\n");
        ps.println("insert into tims.staff (title, forename, surname, profession_code,");
        ps.println("contract_start_date, contract_end_date, security_badge_id) values (");
        ps.println("'" + getRequest().getTitle() + "',");
        ps.println("'" + getRequest().getSQLFriendlyForename() + "',");
        ps.println("'" + getRequest().getSQLFriendlySurname() + "',");
        ps.println("'" + getRequest().getProfession_code() + "',");

        //if no start date is given by default use current date
        if (getRequest().getStartDate() == null){                   
            ps.println("'" + new SimpleDateFormat("dd-MMM-yyyy").format(new Date()) + "',");
        } else { 
            ps.println("'" + getRequest().getStartDate() + "',");
        }
               
        //if not end date filled then leave blank
        if (getRequest().getEndDate() == null){
            ps.println("null,");
        } else {
            ps.println("'" + getRequest().getEndDate() + "',");
        }               
        ps.println("'" + getRequest().getSecurityBadge() + "'");
        ps.println(");");             
        ps.println("GO");
        ps.println("DECLARE @timsID INT;");
        ps.println("SET @timsID = @@identity");
              
        ps.flush();
    }
    
    //public String AddTimsStaffSysuser(String username){
    public void AddTimsStaffSysuser(String username){
        ps.println("\n-- adding staff account for user\n");        
        //ps.println("insert into tims.staff_sysuser (staff_id, uid, account_name) values (@timsID,@uid, '" + username + "');");   
        ps.println("insert into tims.staff_sysusers (staff_id, uid, account_name) select @timsID, uid, '" + username + "' from theatre_prod.dbo.sysusers where name like '" + username +"';");   
        ps.flush();
    }

    public void AddTimsStaffSysuserToExistingAccount(String username, String timsID){
        ps.println("\n-- adding staff account for user\n");        
        //ps.println("insert into tims.staff_sysuser (staff_id, uid, account_name) values (@timsID,@uid, '" + username + "');");   
        ps.println("insert into tims.staff_sysusers (staff_id, uid, account_name) select '" + timsID + "', uid, '" + username + "' from theatre_prod.dbo.sysusers where name like '" + username +"';");   
        ps.flush();
    }
    
    public void AddStaffLocationDropdown(){
         ps.println("\n-- adding staff to appear in TIMS dropdown\n");
        Iterator locIter = getRequest().getLocationCollection().iterator();
        while (locIter.hasNext()){
            String locationCode =  (String) locIter.next();
            ps.println("insert into tims.staff_theatre_groups (staff_id, location_group_id) values (@timsID," + locationCode +");\n");
        }
        ps.flush();    
    }
    
    //public String AddConsultantGrade() {
    public void AddConsultantGrade() {
        ps.println("\n-- adding staff role for consultant\n");
        ps.println("insert into tims.staff_grades (staff_id, grade_code) values (@timsID,'CON');");
        
        //return script.toString();        
        ps.flush();
       
    }

    //public String AddStaffTheatreGroups() {
    public void AddStaffLocationAccess(String username) {
        ps.println("\n-- adding staff location access privilages\n");
        Iterator locIter = getRequest().getLocationCollection().iterator();
        while (locIter.hasNext()){
            String locationCode =  (String) locIter.next();
            ps.println("insert into tims.account_location_privilages (account_name, location_group_id) values ('" + username + "','" + locationCode + "');");
        }
        ps.flush();
    }
    
    public void UpdateStaffLocationAccess(String username) {
        if (getRequest().getLocationCollection().size() > 0) {
            ps.println("\n-- updating location access \n");
            Iterator locIter = getRequest().getLocationCollection().iterator();
            while (locIter.hasNext()) {
                String locationCode = (String) locIter.next();
                int doesExist = getData().CheckUserAccessLocationAssociationExist(getRequest().getTimsInternalID(), locationCode);
                if (doesExist == 0) {
                    ps.println("insert into tims.account_location_privilages (account_name, location_group_id) values ('" + username + "','" + locationCode + "');");
                } else if (doesExist == 1) {
                    ps.println("update tims.account_location_privilages set archive_flag = '01-JAN-1900' where account_name = '" + username + "' and location_group_id ='" + locationCode + "';");
                } else {
                    ps.println("-- Error - too many location group ID discovered for staff_username = '" + username + " and location_group_id ='" + locIter.next().toString() + "'");
                }
            }
        }
        ps.flush();
    }
  
    public void RetireStaffLocationAccess() {
        String username = getData().GetUsernameByTimsInternalID(getRequest().getTimsInternalID());

        if (getRequest().getLocationCollection().size() > 0) {
            ps.println("\n-- retire location access \n");
            Iterator locIter = getRequest().getLocationCollection().iterator();
            while (locIter.hasNext()) {
                String locationCode = locIter.next().toString();
                int doesExist = getData().CheckUserAccessLocationAssociationExist(getRequest().getTimsInternalID(), locationCode);
                if (doesExist == 0) {
                    ps.println("-- Error - Cannot retire since association does not exist for username = '" + username + " and location_group_id='" + locationCode + "'\n");
                } else if (doesExist == 1) {
                    //only retire location that matches the location from where the request was sent
                    if (locationCode.equals(request.getRequestingLocation())) {
                        String currentDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
                        ps.println("update tims.account_location_privilages set archive_flag = '" + currentDate + "' where account_name = '" + username + "' and location_group_id in ('" + getRequest().getRequestingLocation() + "');");
                    }
                } else {
                    ps.println("-- Error - too many location group ID discovered for staff_username = '" + username + "' and location_group_id ='" + locationCode + "'\n");
                }
            }
        }
        ps.flush();
    }
    

    public void AddStaffRoles() {        

        if (getRequest().getRoleCollection().size() > 0) {
            ps.println("\n-- adding staff role information");
            Iterator rolIter = getRequest().getRoleCollection().iterator();
            while (rolIter.hasNext()) {
                String roleCode = (String) rolIter.next();
                ps.println("insert into tims.staff_roles (staff_id, role_code) values (@timsID,'" + roleCode + "');");
            }
        }
        ps.flush();
    } 
    
    public void AddDoctorSpecialty() {        

        if (getRequest().isDoctor()) {
            ps.println("\n-- adding doctor specialty information");
            ps.println("insert into tims.doctor_specialty (staff_id, local_spec_code) values (@timsID,'" + getRequest().getSpecialty_code() + "');");
        }
        ps.flush();
    }   
    
    public void AddAccountPrivilage(String username) {        
//ps.println("insert into tims.staff_sysusers (staff_id, uid, account_name) ");
        if (getRequest().isAddNewAccount()) {
            ps.println("\n-- adding TIMS account privilage information");
            switch (getRequest().getPasswordPrivilage()){
                case READONLY:
                    ps.println("insert into TIMS.app_priv_group_members (user_id, group_id) select uid, '5' from theatre_prod.dbo.sysusers where name like '" + username +"';");        
                    break;
                case FULL:
                    ps.println("insert into TIMS.app_priv_group_members (user_id, group_id) select uid, '3' from theatre_prod.dbo.sysusers where name like '" + username +"';");
                case NONE:
                default:
                    break;
            }
        }
    }
        
    public void UpdateAccountPrivilage(String username) {        

        if (!username.equals("") || username != null) {
            ps.println("-- adding/updating account access privilage information");
            int doesExist = getData().DoesUserAccountHaveAccountPrivilages(username);
            //int doesExist = getData().CheckUserAccountPrivilageAssociationExist(username, getRequest().getPasswordPrivilage());
            if (doesExist == 0){
                switch (getRequest().getPasswordPrivilage()){
                case READONLY:
                    ps.println("insert into TIMS.app_priv_group_members (user_id, group_id) select uid, '5' from theatre_prod.dbo.sysusers where name like '" + username +"';");        
                    break;
                case FULL:
                    ps.println("insert into TIMS.app_priv_group_members (user_id, group_id) select uid, '3' from theatre_prod.dbo.sysusers where name like '" + username +"';");
                case NONE:
                default:
                    break;
            }
            }
            else if (doesExist == 1){
                //reset all to 
                String currentDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
                ps.println("update tims.app_priv_group_members set archive_flag = '" + currentDate + "' where user_id in (select uid from theatre_prod.dbo.sysusers where name like '" + username +"');");
                switch (getRequest().getPasswordPrivilage()){
                case READONLY:
                        ps.println("update tims.app_priv_group_members set archive_flag = '01-JAN-1900' where group_id = '5' and user_id in ( select uid from theatre_prod.dbo.sysusers where name like '" + username +"');");
                    break;
                case FULL:
                     ps.println("update tims.app_priv_group_members set archive_flag = '01-JAN-1900' where group_id = '3' and user_id in ( select uid from theatre_prod.dbo.sysusers where name like '" + username +"');");
                case NONE:
                default:
                    break;
            }
                
            }
            else {
                ps.println("-- Error - too many of the same privilege code codes discovered for staff_id = '" + getRequest().getTimsInternalID() +"' and privilege_code='" + getRequest().getPasswordPrivilage() + "'");
            }
        }
        ps.flush();
    }   
    
    public void UpdateDoctorSpecialty() {        

        if (getRequest().isDoctor()) {
            ps.println("-- adding/updating doctor specialty information");
            
            int doesExist = getData().CheckUserSpecialityAssociationExist(getRequest().getTimsInternalID(), getRequest().getSpecialty_code());
            if (doesExist == 0){
                ps.println("insert into tims.doctor_specialty (staff_id, local_spec_code) values ('" + getRequest().getTimsInternalID() + "','" + getRequest().getSpecialty_code() + "');");
            }
            else if (doesExist == 1){
                String currentDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
                ps.println("update tims.doctor_specialty set archive_flag = '01-JAN-1900' where staff_id = '" + getRequest().getTimsInternalID() + "';");
            }
            else {
                ps.println("-- Error - too many of the same speciality codes discovered for staff_id = '" + getRequest().getTimsInternalID() +"' and specialty code='" + getRequest().getSpecialty_code() + "'");
            }
        }
        ps.flush();
    }   

    //public String UpdateTIMS_LocationAccessPrivilages(String username) {     
    public void UpdateStaffLocationDropdown() {   
        
        ps.println("\n-- adding/updating locations that appear in TIMS dropdown\n");
        Iterator locIter = getRequest().getLocationCollection().iterator();
        while (locIter.hasNext()) {
            String locationCode = (String) locIter.next();
            int doesExist = getData().CheckUserDropdownAssociationExist(getRequest().getTimsInternalID(), locationCode);
            if (doesExist == 0){
                ps.println("insert into tims.staff_theatre_groups (staff_id, location_group_id) values ('" + getRequest().getTimsInternalID() + "','" + locationCode +"');");
            }
            else if (doesExist == 1){
                ps.println("update tims.staff_theatre_groups set archive_flag = '01-JAN-1900' where staff_id = '" + getRequest().getTimsInternalID() + "' and location_group_id='" + locationCode + "';");
            }
            else{
                ps.println("-- Error - too many location codes discovered for staff_id = '" + getRequest().getTimsInternalID() +"' and location_code='" + locIter.next().toString()+ "'");
            }
        }
        ps.flush();
        
        
    }
    
    public void RetireStaffLocationDropdown() {
        ps.println("\n-- retiring locations that appear in TIMS dropdown");
        Iterator locIter = getRequest().getLocationCollection().iterator();
        while (locIter.hasNext()) {
            String locationCode = (String) locIter.next();
            int doesExist = getData().CheckUserDropdownAssociationExist(getRequest().getTimsInternalID(), locationCode);
            if (doesExist == 0){
                ps.println("-- Error - Cannot retire since association does not exist for staff_id = '" + getRequest().getTimsInternalID() +"' and location_group_id='" + locationCode + "'");
            }
            else if (doesExist == 1){
                //only retire location that matches the location from where the request was sent
                if (locationCode.equals(request.getRequestingLocation())) {
                    String currentDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
                    ps.println("update tims.staff_theatre_groups set archive_flag = '" + currentDate + "' where staff_id = '" + getRequest().getTimsInternalID() +"' and location_group_id in ('" + getRequest().getRequestingLocation() + "');");
                }
            }
            else{
                ps.println("-- Error - too many location codes discovered for staff_id = " + getRequest().getTimsInternalID() +" and location_code='" + locationCode + "'\n");
            }
        }
        ps.flush();
    }
        
    public void UpdateStaffRoles() {         
        if (getRequest().getRoleCollection().size() > 0) {
            ps.println("-- adding/updating staff role information against roles selected on request form");
            
            //disabling all active staff roles for user
            String currentDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
            ps.println("update tims.staff_roles set archive_flag = '" + currentDate +"' where staff_id = '" + getRequest().getTimsInternalID() +"'");
            Iterator roleIter = getRequest().getRoleCollection().iterator();
            while (roleIter.hasNext()) {
                String roleCode = (String) roleIter.next();
                int doesExist = getData().CheckUserRoleAssociationExist(getRequest().getTimsInternalID(), roleCode);
                if (doesExist == 0){
                    ps.println("insert into tims.staff_roles (staff_id, role_code) values ('" + getRequest().getTimsInternalID() +"','" + roleCode + "');");
                }
                else if (doesExist == 1){
                    ps.println("update tims.staff_roles set archive_flag = '01-JAN-1900' where staff_id = '" + getRequest().getTimsInternalID() +"' and role_code='" + roleCode + "';");              
                }
                else{
                    ps.println("-- Error - too many role codes discovered for staff_id = '" + getRequest().getTimsInternalID() +"' and role_code='" + roleCode + "'");
                }
            }
            ps.flush();
       }
    }
     
    public void UpdateConsultantGrade() {            
        int doesExist = getData().CheckUserConsultantGradeAssociationExist(getRequest().getTimsInternalID());
        if (doesExist == 0){
            if (getRequest().isDoctor() && getRequest().isConsultant()){
                ps.println("\n-- adding/updating staff grade information\n");
                ps.println("insert into tims.staff_grades (staff_id, grade_code) values ("+ getRequest().getTimsInternalID() + ",'CON');");
            }
        }
        else if (doesExist == 1) {
            ps.println("\n-- updating staff grade information\n");
            if (getRequest().getRequestAction().equals(RequestExtractor.REQUEST_ACTION.REMOVEUSER)) {
                ps.println("update tims.staff_grades set archive_flag = '" + new SimpleDateFormat("dd-MMM-yyyy").format(new Date()) + "' where staff_id = '" + getRequest().getTimsInternalID() + "' and grade_code = 'CON');");
            } else if (getRequest().getRequestAction().equals(RequestExtractor.REQUEST_ACTION.AMEND)){
                if (getRequest().isDoctor()){
                    if (getRequest().isConsultant()){
                        ps.println("update tims.staff_grades set archive_flag = '01-JAN-1900' where staff_id = '" + getRequest().getTimsInternalID() + "','CON');");                        
                    }
                    else {
                        ps.println("update tims.staff_grades set archive_flag = '" + new SimpleDateFormat("dd-MMM-yyyy").format(new Date()) + "' where staff_id = '" + getRequest().getTimsInternalID() + "' and grade_code = 'CON');");
                    }             
                }
            }
        }
        else{
                ps.println("-- Error - too many consultant grades discovered for staff_id = '" + getRequest().getTimsInternalID() + "'\n");
        }        
        ps.flush();
    }
    
    public void RetireConsultantGrade() {
        ps.println("\n-- retire staff grade information\n");
        int doesExist = getData().CheckUserConsultantGradeAssociationExist(getRequest().getTimsInternalID());
        if (doesExist == 0){
            ps.println("-- Error - Cannot retire since staff grade association does not exist for staff_id = '" + getRequest().getTimsInternalID() + "'");
        }
        else if (doesExist == 1) {
            if (getRequest().getRequestAction().equals(RequestExtractor.REQUEST_ACTION.REMOVEUSER)) {
                ps.println("update tims.staff_grades set archive_flag = '" + new SimpleDateFormat("dd-MMM-yyyy").format(new Date()) + "' where staff_id = '" + getRequest().getTimsInternalID() + "' and grade_code = 'CON');");                
            }
        }
        else{
                ps.println("-- Error - too many consultant grades discovered for staff_id = '" + getRequest().getTimsInternalID() + "'");
        }        
        ps.flush();
    }
    
    public void RetireStaffRoles() {
        if (getRequest().getRoleCollection().size() > 0) {
            ps.println("\n-- adding/updating staff role information\n");
            Iterator roleIter = getRequest().getRoleCollection().iterator();
            while (roleIter.hasNext()) {
                String roleCode = (String) roleIter.next();
                int doesExist = getData().CheckUserRoleAssociationExist(getRequest().getTimsInternalID(), roleCode);
                if (doesExist == 0) {
                    ps.println("-- Error - Cannot retire since association does not exist for staff_id = " + getRequest().getTimsInternalID() + " and role_code='" + roleCode + "'");
                } else if (doesExist == 1) {
                    String currentDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
                    ps.println("update tims.staff_roles set archive_flag = '" + currentDate + "' where staff_id = '" + getRequest().getTimsInternalID() + "' and role_code='" + roleCode + "';");
                } else {
                    ps.println("-- Error - too many role codes discovered for staff_id = '" + getRequest().getTimsInternalID() + "' and role_code='" + roleIter.next().toString() + "'");
                }
            }
        }
        ps.flush();
    }
    
    public void EditTimsStaffSysuser(String username) {
        ps.println("\n-- adding staff account for user\n");
        ps.println("update tims.staff_sysuser sey account_name = '" + username + "' where staff_id = '" + getRequest().getTimsInternalID() + "'");
        ps.flush();
    }
    
    //public String EditTimsStaffDetails(){
    public void EditTimsStaffDetails() {
        ps.println("\n-- amend staff record to TIMS\n");
        ps.println("update tims.staff ");
        ps.println("set title = '" + getRequest().getTitle() + "',");
        ps.println("forename = '" + getRequest().getSQLFriendlyForename() + "',");
        ps.println("surname = '" + getRequest().getSQLFriendlySurname() + "',");
        if (!getRequest().getProfession_code().toString().equals("NONE")) {
            ps.println("profession_code = '" + getRequest().getProfession_code() + "',");
        }
        //if no start date is given by default use current date
        if (getRequest().getStartDate() == null) {
            ps.println("contract_start_date = '" + new SimpleDateFormat("dd-MMM-yyyy").format(new Date()) + "',");
        } else {
            ps.println("contract_start_date = '" + getRequest().getStartDate() + "',");
        }
        if (getRequest().getEndDate() == null) {
            ps.println("contract_end_date = null,");
        } else {
            ps.println("contract_end_date =  '" + getRequest().getEndDate() + "', ");
        }
        ps.println("archive_flag = '01-JAN-1900'");
        ps.println("where staff_id like '" + getRequest().getTimsInternalID() + "';");
        ps.flush();
    }
       
    public void RetireTimsStaffDetails() {
        ps.println("\n-- retire staff record from TIMS\n");
        ps.println("update tims.staff ");        
        //if no start date is given by default use current date
        ps.println("set archive_flag = '" + new SimpleDateFormat("dd-MMM-yyyy").format(new Date()));
        ps.println("where staff_id like '" + getRequest().getTimsInternalID() + "';");
        ps.flush();
    }
    
    public void CreateSQLServerAccount(String username, String password) {

        ps.println("\n-- create sql server account and associate it to role\n");
        ps.println("USE [master]");
        ps.println("GO");
        ps.println("EXEC master.dbo.sp_addlogin @loginame = N'" + username + "',@passwd = N'" + password + "', @defdb = N'theatre_prod'");
        ps.println("USE [theatre_prod]");
        ps.println("GO");
        ps.println("EXEC dbo.sp_grantdbaccess @loginame = N'" + username + "',@name_in_db = N'" + username + "'");
        ps.println("GO");
        ps.println("EXEC sp_addrolemember N'TIMS_USER', N'" + username + "'");
        ps.println("GO");
        ps.flush();
    }
    
    public void resetSQLServerAccount(String username, String password) {
        ps.println("\n-- reset SQL server password for named account\n");
        ps.println("USE [master]");
        ps.println("GO");
        ps.println("EXEC master.dbo.sp_password @old=NULL, @new='" + password + "', @loginame=[" + username + "]");
        ps.println("GO");
        ps.flush();
    }
        
    /**
     * @return the request
     */
    public Request getRequest() {
        return request;    }

    /**
     * @param request the request to set
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * @return the data
     */
    public DataAccess getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(DataAccess data) {
        this.data = data;
    }
    
}
