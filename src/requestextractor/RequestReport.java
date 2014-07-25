package requestextractor;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import requestextractor.DataAccess;
import requestextractor.Request;
import requestextractor.RequestExtractor;
import requestextractor.RequestExtractor.LOCATIONS;

/**
 *
 * @author hiznis
 */
public class RequestReport {
    private String detail = "";
    private boolean generateSQL = false;
    private String username = "";
    private String password = "";

    private RequestExtractor.REQUEST_ACTION action;
    private Request request;
    private DataAccess data;
    
    private boolean alreadyExist = false;
    
    private RequestExtractor.LOCATIONS requestingLocation;
    
    
    public RequestReport() {
        
    }
    
    public RequestReport(RequestExtractor.REQUEST_ACTION action, Request request) {
        //this.generateManifest(action, request, da);
        setAction(action);
        setRequest(request);
    }

    public RequestReport(RequestExtractor.REQUEST_ACTION action, Request request, DataAccess da) {
        //this.generateManifest(action, request, da);
        setAction(action);
        setRequest(request);
        setData(da);
        
        generateManifestItem(action, request, da);
    }
    
    public void generateManifestItem(RequestExtractor.REQUEST_ACTION action, Request request, DataAccess da) {
        //StringBuilder manifest = new StringBuilder("");
        switch (da.CheckUserExistByTimsInternal(request.getTimsInternalID())){
            case 0:
                switch (da.CheckUserExistByBadge(request.getSecurityBadge())) {
                    case 0:
                        switch (da.CheckUserExistByName(request.getSurname(), request.getForename())) {
                            case 0:
                                //manifest.append("Brand new user.");
                                setDetail("Brand new user");
                                setAlreadyExist(false);
                                setGenerateSQL(true);
                                break;
                            case 1:
                                //manifest.append("User already exists on TIMS (matched on names)");
                                setDetail("User already exists on TIMS (matched on names)");
                                setAlreadyExist(true);
                                setGenerateSQL(true);
                                break;
                            default:                                
                                //manifest.append(getRequest().getRequestingLocation()).append("Error: Too many users encountered with same names: - ").append(request.getForename()).append(" ").append(request.getSurname().toUpperCase());
                                getRequest().setRequestingLocation(LOCATIONS.UNKNOWN);
                                setDetail("Error: Too many users encountered with same names!");
                                setGenerateSQL(false);
                                break;
                         }
                        break;
                    case 1:
                        //manifest.append("User already exists on TIMS (matched on security badge number) - ").append(request.getForename()).append(" ").append(request.getSurname().toUpperCase());
                        setDetail("User aleady exists on TIMS (matched on security badge number)");
                        setAlreadyExist(true);
                        setGenerateSQL(true);
                        break;
                    default:
                        //manifest.append("Error: Too many users encountered with same security badge number: - ").append(request.getSecurityBadge());
                        getRequest().setRequestingLocation(LOCATIONS.UNKNOWN);
                        setDetail("Error! Too many users encountered with same security badge number - " + request.getSecurityBadge());
                        setGenerateSQL(false);
                        break;
                }
                break;
            case 1:
                //manifest.append("Existing user (matched on TIMS Staff ID) - ").append(request.getForename()).append(" ").append(request.getSurname().toUpperCase());
                setDetail("A user already exists on TIMS (matched on TIMS staff ID)");
                getRequest().setRequestingLocation(LOCATIONS.UNKNOWN);
                setAlreadyExist(true);
                //setGenerateSQL(false);
                setGenerateSQL(true);
                break;
            default:
                getRequest().setRequestingLocation(LOCATIONS.UNKNOWN);
                //manifest.append("Error: Too many users with the same internal TIMS staff ID - ").append(request.getTimsInternalID());
                setDetail("Error: Too many users with the same internal TIMS staff ID - " + request.getTimsInternalID());
                setAlreadyExist(true);
                setGenerateSQL(false);
                break;
        }
        
        //setDetail(manifest.toString());        
        //detail = manifest.toString();   
    }


    /**
     * @return the detail
     */
    public String getDetail() {
        //return generateManifest(this.action, this.request, this.data);
        return detail;
    }

    /**
     * @param detail the detail to set
     */
    public void setDetail(String detail) {
        //StringBuilder sb = new StringBuilder(getDetail());
        //sb.append(detail);
        this.detail = this.detail + "\n" + detail;
    }

    /**
     * @return the generateSQL
     */
    public boolean isGenerateSQL() {
        return generateSQL;
    }

    /**
     * @param generateSQL the generateSQL to set
     */
    public void setGenerateSQL(boolean generateSQL) {
        this.generateSQL = generateSQL;
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

    /**
     * @return the action
     */
    public RequestExtractor.REQUEST_ACTION getAction() {
        return action;
    }

    /**
     * @return the request
     */
    public Request getRequest() {
        return request;
    }

    /**
     * @return the data
     */
    public DataAccess getData() {
        return data;
    }

    /**
     * @param action the action to set
     */
    public void setAction(RequestExtractor.REQUEST_ACTION action) {
        this.action = action;
    }

    /**
     * @param request the request to set
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * @param data the data to set
     */
    public void setData(DataAccess data) {
        this.data = data;
    }

    void setRequestingLocation(LOCATIONS requestingLocation) {
        this.requestingLocation = requestingLocation;
    }
    
    RequestExtractor.LOCATIONS getRequestingLocation(){
        return this.requestingLocation;
    }

    /**
     * @return the alreadyExist
     */
    public boolean isAlreadyExist() {
        return alreadyExist;
    }

    /**
     * @param alreadyExist the alreadyExist to set
     */
    public void setAlreadyExist(boolean alreadyExist) {
        this.alreadyExist = alreadyExist;
    }
    
    
}
