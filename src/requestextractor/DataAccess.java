package requestextractor;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hiznis
 */
public class DataAccess {

    private Connection conn = null;
    private Statement stmt = null;
    public DataAccess() {
        try {
            String userName = "tims";
            String password = "buckingham";

            String url = "jdbc:jtds:sqlserver://oxnettims01:1433/theatre_prod";
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            conn = DriverManager.getConnection(url, userName, password);
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Object ExecuteSQLReturnVariable(String sql, int recordsetLocation){
        Object retValue = null;
        try {            
            if (!getConnection().isClosed()) {
                    Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    ResultSet rs = stmt.executeQuery(sql);                
                    if (rs.next())                    
                        retValue = rs.getObject(recordsetLocation);                
            } else {
                    System.out.println("Connection is closed!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            return retValue;
        }
    }
    /**
     * ExecuteSQLReturnRecordset
     * Executes the SQL passed as a parameter and returns the recordset object to the
     * calling code. Useful when more than one
     * @param sql
     * @return 
     */
    public ResultSet ExecuteSQLReturnRecordset (String sql) {
        ResultSet rs = null;
        try {            
            if (!getConnection().isClosed()) {
                    Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    rs = stmt.executeQuery(sql);                             
            } else {
                    System.out.println("Connection is closed!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            return rs;
        }
    }
    
    /**
     * ExecuteSQL 
     * Nothing is returned from this method, so it is most useful when executing SQL
     * statements that require no return - ie: inserts or updates.
     * @param sql sql command to be executed. 
     */
    public void ExecuteSQL (String sql) {        
        try {            
            if (!getConnection().isClosed()) {
                    Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    ResultSet rs = stmt.executeQuery(sql);                             
            } else {
                    System.out.println("Connection is closed!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int CheckUserRoleAssociationExist(String timsID, String role_code ){
        int retVal = 0;
        try {
            String sql = "select count(staff_id) from TIMS.staff_roles where staff_id = '" + timsID + "' and role_code = '" + role_code + "'";
            if (!getConnection().isClosed()) {
                Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(sql);                
                if (rs.next())                    
                    retVal = rs.getInt(1);                
            } else {
                System.out.println("Connection is closed!");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        } 
    }
    
    public int CheckUserConsultantGradeAssociationExist(String timsID){
        int retVal = 0;
        try {
            String sql = "select count(staff_id) from TIMS.staff_grades where staff_id = '" + timsID + "' and grade_code = 'CON'";
            if (!getConnection().isClosed()) {
                Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(sql);                
                if (rs.next())                    
                    retVal = rs.getInt(1);                
            } else {
                System.out.println("Connection is closed!");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        } 
    }
    
    public int CheckUserDropdownAssociationExist(String timsID, String location_group_id ){
        int retVal = 0;
        try {
            String sql = "select count(staff_id) from TIMS.staff_theatre_groups where archive_flag = '01-JAN-1900' and staff_id = '" + timsID + "' and location_group_id = '" + location_group_id + "'";
            if (!getConnection().isClosed()) {
                Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(sql);                
                if (rs.next())                    
                    retVal = rs.getInt(1);                
            } else {
                System.out.println("Connection is closed!");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        } 
    }
    
    public int CheckUserAccessLocationAssociationExist(String timsID, String location_group_code ){
        int retVal = 0;
        try {            
            String sql = "select count(account_name) from TIMS.account_location_privilages where archive_flag = '01-JAN-1900' and account_name = '" + GetUsernameByTimsInternalID(timsID) + "' and location_group_id = '" + location_group_code + "'";
            if (!getConnection().isClosed()) {
                Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(sql);                
                if (rs.next())                    
                    retVal = rs.getInt(1);                
            } else {
                System.out.println("Connection is closed!");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        } 
    }
    
    public int CheckUserExistByBadge(String secBadgeNum) {
        int retVal = 0;
        try {
            String sql = "select count(staff_id) from TIMS.staff where archive_flag = '01-JAN-1900' and security_badge_id like '" + secBadgeNum + "'";
            //System.out.println("SQL to be executed (badge): " + sql);
            if (!getConnection().isClosed()) {
                Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(sql);                
                if (rs.next())                    
                    retVal = rs.getInt(1);                
            } else {
                System.out.println("Connection is closed!");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        }
    }
    
    public int CheckUserExistByName(String surname, String forename) {
        int retVal = 0;
        try {
            String sql = "select count(staff_id) from TIMS.staff where surname like '" + surname + "' and forename like '" + forename + "'";
            //System.out.println("SQL to be executed (name): " + sql);
            stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);           
            if (rs.next()) {
                retVal = rs.getInt(1);
            }           
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        }
    }
    
    public int CheckUserAccountPrivilageAssociationExist(String username, RequestExtractor.PASSWORD_PRIVILAGE priv) {
        int retVal = 0;
        try {
            String sql = "select count(user_id) from TIMS.app_priv_group_members where user_id in (select uid from theatre_prod.dbo.sysusers where name like '" + username +"') ";
            switch (priv) {
                case FULL:
                    sql += " and group_id = 3";
                    break;
                case READONLY:
                    sql += " and group_id = 5";
                    break;
                case NONE:
                default:
                    sql = "";
                    break;
            }

            //System.out.println("SQL to be executed (name): " + sql);
            stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                retVal = rs.getInt(1);

            }
        } catch (SQLException ex) {
            Logger.getLogger("Username " + username);
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        }
        
    }
    public int CheckUserSpecialityAssociationExist(String staffID, String specialtyCode){
        int retVal = 0;
        try {
            String sql = "select count(staff_id) from TIMS.doctor_specialty where local_spec_code like '" + specialtyCode + "' and staff_id like '" + staffID + "'";
            //System.out.println("SQL to be executed (name): " + sql);
            stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);           
            if (rs.next()) {
                retVal = rs.getInt(1);
            }           
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        }
        
    }
    public int CheckUserExistByTimsInternal(String timsID) {
        int retVal = 0;
        try {
            if (!timsID.equals("") || timsID != null) {
                String sql = "select count(staff_id) from TIMS.staff where staff_id like '" + timsID + "'";            
                stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery(sql);           
                if (rs.next()) {
                 retVal = rs.getInt(1);
                }   
            }           
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, e);
        }
        finally {
            return retVal;
        }
    }

    public String GetUsernameByTimsInternalID(String timsID) {
        String retVal = "";
        try {
            String sql = "select account_name from tims.staff_sysusers where staff_id = '" + timsID  + "'";
            
            //System.out.println("SQL to be executed (name): " + sql);
            stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);           
            if (rs.next()) {
                retVal = rs.getString(1);
            }           
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        }
    }
    
    public String GetTimsInternalID(String secBadgeNum) {
        String retVal = "";
        try {
            String sql = "select staff_id from TIMS.staff where security_badge_id like '" + secBadgeNum + "'";
            
            //System.out.println("SQL to be executed (name): " + sql);
            stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);           
            if (rs.next()) {
                retVal = rs.getString(1);
            }           
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        }
    }
    
    public String GetTimsInternalID(String surname, String forename) {
        String retVal = "";
        try {
            String sql = "select staff_id from TIMS.staff where surname like '" + surname + "' and forename like '" + forename + "'";
            
            //System.out.println("SQL to be executed (name): " + sql);
            stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);           
            if (rs.next()) {
                retVal = rs.getString(1);
            }           
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        }
    }

    public int CheckUsernameExist(String username) {
        int retVal = 0;
        try {
            String sql = "select count (name) from master.dbo.syslogins where name like '" + username + "'";
            //System.out.println("SQL to be executed (name): " + sql);
            stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);           
            if (rs.next()) {
                retVal = rs.getInt(1);
            }           
        } catch (SQLException ex) {
            Logger.getLogger(DataAccess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retVal;
        }
    }
    
    
    
    /**
     * @return the conn
     */
    public Connection getConnection() {
        return conn;
    }
    
    
    
    
}
