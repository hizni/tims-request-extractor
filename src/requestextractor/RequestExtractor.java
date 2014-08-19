package requestextractor;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author hiznis
 */
public class RequestExtractor {

    ArrayList headerList = new ArrayList();
    static ArrayList requestList = new ArrayList<Request>();
    static ArrayList manifestList = new ArrayList<RequestReport>();
    static ListMultimap<LOCATIONS, RequestReport> requestListWithRequestingLocation = ArrayListMultimap.create();
    static StringBuilder manifest = new StringBuilder();
    static StringBuilder sqlScript = new StringBuilder();
    static DataAccess da = null;
    static LOCATIONS requestingLocation;
    static String currentDate = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());

    static FormulaEvaluator evaluator = null;

    
    /*
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try 
        {

            //hardcoded directory details
            checkDirectoryExists(System.getProperty("user.dir") + "\\files\\requests\\");
            checkDirectoryExists(System.getProperty("user.dir") + "\\files\\output\\");
            File inputDirectory = new File(System.getProperty("user.dir") + "\\files\\requests\\");
            String outputDirectory = System.getProperty("user.dir") + "\\files\\output\\";

            
            if (!inputDirectory.exists()){
                //System.out.println("The directory that should hold the requests \\requests does not exist");
                System.out.println("The directory that should hold the requests \\requests does not exist");
                System.out.println("Looking for: " + System.getProperty("user.dir") + "\\files\\requests\\" );
            } else {
                checkDirectoryExists(System.getProperty("user.dir") + "\\files\\sql\\");
                File sqlOutputFile = new File(outputDirectory + "\\sql\\" + currentDate + "_sqlScript.sql");
                File[] listOfFiles = inputDirectory.listFiles();
                List<File> successfulProcessedFiles = new ArrayList<File>();
                List<File> erroredFiles = new ArrayList<File>();

                int numberOfEncounteredFiles = 0;

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        String files = listOfFiles[i].getName();
                        if (files.endsWith(".xlsx") || files.endsWith(".XLSX")) {
                            numberOfEncounteredFiles++;
                            //current file
                            File currentFile = listOfFiles[i];                            
                            System.out.println("Processing: " + currentFile.getName() + "....");
                            if (processSpreadsheet(currentFile, sqlOutputFile)) {
                                successfulProcessedFiles.add(currentFile);
                                checkDirectoryExists(System.getProperty("user.dir") + "\\files\\done\\");
                                //fixme temporarily removed
                                copyFile(currentFile,System.getProperty("user.dir") + "\\files\\done\\" + currentDate);
                            } else {
                                erroredFiles.add(currentFile);
                                checkDirectoryExists(System.getProperty("user.dir") + "\\files\\error\\");
                                //fixme temporarily removed
                                copyFile(currentFile,System.getProperty("user.dir") + "\\files\\error\\" + currentDate);
                                RequestReport m = new RequestReport();
                                m.setUsername("-");
                                m.setPassword("-");
                                m.setDetail("Error encountered when processing: " + currentFile.getName());
                                requestListWithRequestingLocation.put(LOCATIONS.UNKNOWN, m);
                            }
                        }
                    }
                }
 
                
                
                //delete all files with XLSX extension
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        String files = listOfFiles[i].getName();
                        if (files.endsWith(".xlsx") || files.endsWith(".XLSX")) {
                            File currentFile = listOfFiles[i];
                            //fixme temporarily removed next line
                           // currentFile.delete(); //removes this file from the original source location
                        }
                    }
                }
                if (numberOfEncounteredFiles == 0){
                    //System.setOut(System.out);
                    System.out.println("Nothing to do! No TIMS request files encountered");
                }

                //Producing manifest
                //CreateRequestReportDocument(requestListWithRequestingLocation);
                }
            } catch (IOException ioEx) {
                Logger.getLogger(RequestExtractor.class.getName()).log(Level.SEVERE, null, ioEx);
            }
            catch (Exception ex) {
                Logger.getLogger(RequestExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        
    }

    private static void CreateRequestReportDocument(ListMultimap<LOCATIONS, RequestReport> requestReportListWithRequestingLocation) {
        String fileName = System.getProperty("user.dir") + "\\files\\templates\\timsRequestTemplate.docx";

        POIFSFileSystem fs = null;
        FileOutputStream fos = null;
        try {
            

            //XWPFTable table = (XWPFTable) tabIter.next();
            //XWPFTableRow row = table.getRow(0);
            //XWPFTableCell cell = row.getCell(0);              
            //String identifyTable = cell.getText();

            Iterator locIter = requestReportListWithRequestingLocation.keySet().iterator();
            while (locIter.hasNext()) {
                XWPFDocument doc = new XWPFDocument(new FileInputStream(fileName));
                LOCATIONS currentLocation = (LOCATIONS) locIter.next();
                List<RequestReport> manifestList = requestReportListWithRequestingLocation.get(currentLocation);

                //table columns = 6; table rows = manifestList.size()
                if (manifestList.size() > 0) {

                    XWPFTable table = doc.createTable(manifestList.size()+1, 6);
                    XWPFTableCell cell = null;
                    for (int i = 0; i < 7; i++) {
                        switch (i) {
                            case 0:
                                table.getRow(0).getCell(i).setText("Request");
                                break;
                            case 1:
                                table.getRow(0).getCell(i).setText("Action taken");
                                break;
                            case 2:
                                table.getRow(0).getCell(i).setText("User");
                                break;
                            case 3:
                                table.getRow(0).getCell(i).setText("Username");
                                break;
                            case 4:
                                table.getRow(0).getCell(i).setText("Password");
                                break;
                            case 5:
                                table.getRow(0).getCell(i).setText("Trained date");
                                break;
                        };
                    }

                    //populate content rows
                    Iterator manIter = manifestList.iterator();
                    int currentRow = 1;
                    while (manIter.hasNext()) {
                        RequestReport man = (RequestReport) manIter.next();

                        if (man != null) {
                            for (int i = 0; i < 6; i++) {
                                switch (i) {
                                    case 0:
                                        if (man.getRequest().toString() != null) //table.getRow(currentRow).getCell(i).setText(man.getRequest().toString());
                                        {
                                            table.getRow(currentRow).getCell(i).setText(man.getRequest().getRequestAction().toString());
                                        }
                                        break;
                                    case 1:
                                        if (man.getDetail().toString() != null) {
                                            table.getRow(currentRow).getCell(i).setText(man.getDetail());
                                        }
                                        break;
                                    case 2:
                                        if (man.getRequest().getForename() != null) {
                                            table.getRow(currentRow).getCell(i).setText(man.getRequest().getTitle() + " " + man.getRequest().getForename() + " " + man.getRequest().getSurname().toUpperCase());
                                        }
                                        break;
                                    case 3:
                                        if (man.getUsername().toString() != null) {
                                            table.getRow(currentRow).getCell(i).setText(man.getUsername());
                                        }
                                        break;
                                    case 4:
                                        if (man.getPassword().toString() != null) {
                                            table.getRow(currentRow).getCell(i).setText(man.getPassword());
                                        }
                                        break;
                                    case 5:
                                    default:
                                        //left empty on purpose
                                        break;
                                };
                            }
                            currentRow++;
                        }
                        
                    }
                    fos = new FileOutputStream(System.getProperty("user.dir") + "\\files\\output\\requestReport\\timsRequests_" + currentLocation + "_" + currentDate + ".docx");
                    doc.write(fos);
                    fos.flush();
                    fos.close();
                    
                }
            }   
        }
        catch(NullPointerException npe){
            //really weird Null Pointer Exception thrown. Can't figure out what it is!! Line 179
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void copyFile(File f, String targetPath) {
        File moveDir = new File(targetPath);
        // if the directory does not exist, create it
        if (!moveDir.exists()) {
            boolean result = moveDir.mkdir();
        }
        InputStream in = null;
        try {
            in = new FileInputStream(f);
            OutputStream out = new FileOutputStream(targetPath + "\\" + f.getName());
            byte[] moveBuff = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(moveBuff)) > 0) {
                out.write(moveBuff, 0, bytesRead);
            }
        } catch (IOException ex) {
            Logger.getLogger(RequestExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(RequestExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     * @param inputSpreadsheet
     * @param outputScript
     * @return
     */
    //private static boolean processSpreadsheet(File inputSpreadsheet, File outputManifest, File outputScript) {
    private static boolean processSpreadsheet(File inputSpreadsheet, File outputScript) throws Exception {
        FileInputStream spreadsheetIS = null;
        FileOutputStream scriptOS = null;
//        FileOutputStream manifestOS = null;

        boolean success = true;
        try {
            spreadsheetIS = new FileInputStream(inputSpreadsheet);
            scriptOS = new FileOutputStream(outputScript, true);
            //manifestOS = new FileOutputStream(outputManifest);

            //read in TIMS request file
            //PrintStream scriptOut = new PrintStream(scriptOS);
            //PrintStream manifestOut = new PrintStream(manifestOS);


            //if xlsx file Office 2007+
            XSSFWorkbook wb = new XSSFWorkbook(spreadsheetIS);
            evaluator = wb.getCreationHelper().createFormulaEvaluator();
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow row;
            XSSFCell cell;

            /*
             * //if xls file Office 2003 Workbook wb = new HSSFWorkbook(file);
             * Workbook test = new HSSFWorkbook(); Sheet sheet =
             * wb.getSheetAt(0); Row row; Cell cell;
             */

            //test variables
            boolean headerFound = false;
            boolean correctVersion = false;
            
            da = new DataAccess();
            Iterator rows = sheet.rowIterator();
            //iterate over each row in Excel spreadsheet
            while (rows.hasNext()) {
                String requestLocation = "";
                row = (XSSFRow) (Row) rows.next();

                //look at content of cell in first column in each row. (zero index)
                cell = row.getCell(0);

                if (cell != null) {
                    //atempting to discover header row
                    int cellValue = cell.getCellType();

                    if (cellValue == Cell.CELL_TYPE_STRING) {
                        //to check if the spreadsheet being looked at is correct. Version info is held in cell A1 of spreadsheet
                        if (cell.getStringCellValue().contains("VERSION4")){
                            System.out.println("...version good [OK]");
                            correctVersion = true;
                        }
                        
                        //if it is the correct version
                        if (correctVersion) {
                            if (cell.getStringCellValue().toUpperCase().contains("HEADER")) {
                                headerFound = true;

                                //todo - removed need for checking if form unchanged. Locked form at Excel level
                                /*if (checkFormUnchanged(row)) {
                                } else {
                                    //System.setOut(manifestOut);
                                    //System.out.print(" requestextractor.Request form is NOT correct. Stopping processing of this file: " + inputSpreadsheet.getName());
                                    System.out.println("...form has been modified. [FAIL]");
                                    success = false;
                                    headerFound = false;
                                    break;
                                }*/
                            } else if (cell.getStringCellValue().toUpperCase().contains("SENDER")) {
                                requestLocation = row.getCell(1).getStringCellValue();
                                if (requestLocation.startsWith("CH")) {
                                    requestingLocation = LOCATIONS.CH;
                                } else if (requestLocation.startsWith("Horton")) {
                                    requestingLocation = LOCATIONS.HH;
                                } else if (requestLocation.startsWith("JR1")) {
                                    requestingLocation = LOCATIONS.JR1;
                                } else if (requestLocation.startsWith("JR2")) {
                                    requestingLocation = LOCATIONS.JR2;
                                } else if (requestLocation.startsWith("WW")) {
                                    requestingLocation = LOCATIONS.WW;
                                } else {
                                    System.out.println("...sent from unknown location. [FAIL]");
                                }
                                headerFound = false;
                            } else if (cell.getStringCellValue().toUpperCase().contains("STOP")) {
                                //System.out.println("End of request processing");
                            }
                        } else {                            
                            System.out.println("...not the correct version. [FAIL]");
                            success = false;
                            headerFound = false;
                            break;
                        }
                    }
                } else {
                    if (headerFound && correctVersion) {
                        //System.setOut(scriptOut);
                        Request r = populateRequestObject(row);  //shreds request row into Request object
                        if ((r != null) && (!r.getRequestAction().equals(REQUEST_ACTION.NONE.toString()))) { //check will only consider rows that contain data. Validation rules defined in Request class

                            //creates a RequestReport object for each row. Also performs a check against the database for existing records
                            RequestReport man = new RequestReport(r.getRequestAction(), r, da);
                            if (man.isGenerateSQL()) { //flag set to true if ok to proceed with SQL script generation. All outcomes of this check are held in manifest
                                
                                //creating the SQL script
                                GenerateSQL gen = new GenerateSQL(r, da, outputScript);
                                String username = "";
                                
                                //if a new password needs to be created a suitable username that does not already exist must be used.
                                //These operations are completed using SYSTEM STORED PROCEDURES, and do not seem to work well within 
                                //transaction blocks. So the SQL Server account will be created first, and then it will be related to the
                                //TIMS account
                                if (r.isNewPasswordNeeded()) {
                                    if (r.isEditExistingAccount()) {
                                        if(r.getTimsInternalID() == null || r.getTimsInternalID().equals("")){
                                            //man.setDetail("\nA TIMS internal ID need to be provided to identify user.\n Please search the TIMS User Spreadsheet to see if the user exists already on TIMS and resubmit the request");
                                            man.setRequestingLocation(LOCATIONS.UNKNOWN);
                                            r.setUsername("");
                                            r.setPassword("");
                                        }else {
                                            //attempt to get already existing TIMS username for existing user
                                            username = da.GetUsernameByTimsInternalID(r.getTimsInternalID());
                                        }
                                    }

                                    //if cannot find an existing username then create a new one
                                    if (username.equals("") || username.equals(null)) {
                                        String tidiedSurname = r.getTidiedSurname();
                                        if (tidiedSurname.length() > 0) {
                                            username = "";
                                            for (int i = 0; i < tidiedSurname.length(); i++) {
                                                if (i + 1 < tidiedSurname.length()) {
                                                    username = r.getForename().replaceAll("\\s+","") + tidiedSurname.substring(0, i + 1);
                                                } else {
                                                    //for the (hopefully) very rare case that the forename and
                                                    //entire surname exists as a concatenated string
                                                    username = r.getForename().replaceAll("\\s+","") + tidiedSurname.substring(0, i) + "1";
                                                }

                                                //check if the created username already exists. Returns a 0 does not exist
                                                if (da.CheckUsernameExist(username) == 0) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    String password = RandomPasswordGenerator.generatePswd(8, 8, 2, 3, 0).toString();

                                    //todo simplify logic badly badly written. After holidays!!
                                    if (r.isAddNewAccount() || r.isNewPasswordNeeded()) {
                                                                               
                                        //16-10-13 HS
                                        //Was trying to over complicate logic and put in a check to not create the user account if the user already appeared to exist on the
                                        //system. However one re-evaluating the position the onus MUST be on the theatre admin filling in the form to check that the user exists
                                        //or not. If they create multiple users this is a Theatres Department issue and NOT an OUH IM&T issue.
                                        gen.CreateSQLServerAccount(username, password);
                                        r.setUsername(username);
                                        r.setPassword(password);
                                        man.setDetail("Adding user details to TIMS and setting up account to access");       
                                        man.setUsername(username);
                                        man.setPassword(password);

                                        /*if (r.getTimsInternalID() != "" || r.getTimsInternalID() != null){
                                            if (r.getTimsInternalID() == "0"){
                                                gen.AddTimsStaffSysuser(username);
                                            } else {
                                                gen.AddTimsStaffSysuserToExistingAccount(username, r.getTimsInternalID());
                                            }
                                        }*/
                                    }


                                    if (r.isEditExistingAccount()) {
                                        if (r.getTimsInternalID().equals("") || r.getTimsInternalID() == null) {
                                            man.setDetail("No internal TIMS ID was provided. Please resubmit request");
                                            man.setUsername("");
                                            man.setPassword("");
                                        }
                                        else {
                                            if (r.isNewPasswordNeeded()) {
                                                gen.CreateSQLServerAccount(username, password);                                                
                                                man.setDetail("Adding new TIMS account access.");
                                                man.setUsername(username);
                                                man.setPassword(password);
                                            } else {
                                                if (!r.isResetPassword()) {
                                                    
                                                    man.setDetail("Editing TIMS account details. Password will not be reset unless explicitly requested.");
                                                    man.setUsername(username);                                    
                                                }
                                            }
                                        }
                                    }
                                }

                                //if the end user's password needs to be reset then we need to create it before the transaction block
                                //starts
                                if (r.isResetPassword()) {
                                    username = da.GetUsernameByTimsInternalID(r.getTimsInternalID());
                                    if (!username.equals("") || !username.equals(null)) {
                                        String password = RandomPasswordGenerator.generatePswd(8, 8, 2, 3, 0).toString();
                                        //String usernameDetails = "Resetting password. Username: " + username + "; Password: " + password;
                                        gen.resetSQLServerAccount(username, password);
                                        man.setUsername(username);
                                        man.setPassword(password);                                      
                                        man.setDetail("Resetting password as requested.");
                                    }
                                }//end of reset password for existing account
                                gen.StartTransaction();

                                if (r.isAddNewAccount()) {
                                    gen.AddTimsStaffDetails();
                                    if (!r.getProfession_code().equals("ADMIN")) {
                                        gen.AddStaffLocationDropdown();
                                    }
                                    gen.AddStaffRoles();
                                    //will only add consultant grade if request is for a doctor. Validation rule
                                    if (r.isDoctor()) {
                                        gen.AddDoctorSpecialty();
                                        if (r.isConsultant()) {
                                            gen.AddConsultantGrade();
                                        }
                                    }

                                    //if a new password needs to be created a suitable username that does not already exist must be used
                                    if (r.isNewPasswordNeeded()) {
                                        if (r.getTimsInternalID() != "" || r.getTimsInternalID() != null){
                                            if (r.getTimsInternalID().equals("0")){
                                                gen.AddTimsStaffSysuser(username);
                                            } else {
                                                gen.AddTimsStaffSysuserToExistingAccount(username, r.getTimsInternalID());
                                            }
                                        }
                                        gen.AddStaffLocationAccess(username);
                                        gen.AddAccountPrivilage(username);
                                    }
                                } //end of addNewAccount

                                if (r.isEditExistingAccount()) {
                                    username = da.GetUsernameByTimsInternalID(r.getTimsInternalID());
                                    if (r.getTimsInternalID().equals("") || r.getTimsInternalID() == null){
                                        //System.out.println("-- No TIMS internal ID was provided. Manifest now contains a request to the theatre administrator to check and resend the user registration request");
                                        man.setUsername("-");
                                        man.setPassword("-");
                                        man.setDetail("Could not process TIMS registration request for " + r.getForename() + " " + r.getSurname().toUpperCase() + ".\nPlease resend with a TIMS internal ID when requesting to edit an existing user");
                                    }
                                    else {
                                        gen.EditTimsStaffDetails();
                                        if (!r.getProfession_code().equals("ADMIN")) {
                                            gen.UpdateStaffRoles();                                            
                                            gen.UpdateStaffLocationDropdown();
                                        } 
                                        
                                        
                                        //will only add/update consultant grade if request is for a doctor. Validation rule
                                        if (r.isDoctor()) {
                                            gen.UpdateDoctorSpecialty();
                                            if (r.isConsultant()) {
                                                gen.UpdateConsultantGrade();
                                            }
                                        }
                                    }
                                }

                                if (r.getRequestAction().equals(REQUEST_ACTION.GRANTEXISTACCESS)){
                                    gen.AddTimsStaffSysuser(r.getUsername());
                                    gen.AddStaffLocationAccess(r.getUsername());
                                }

                                // Removing an account - doesn't seem to work as hoped. Plus theatres very rarely retire users from TIMS.
                                // A better solution would be to 
                                if (r.isRemoveExistingAccount()) {
                                    if (r.getTimsInternalID().equals("") || r.getTimsInternalID() == null){
                                        //System.out.println("-- No TIMS internal ID was provided. Manifest now contains a request to the theatre administrator to check and resend the user registration request");
                                        man.setUsername("-");
                                        man.setPassword("-");
                                        man.setDetail("Could not process TIMS registration request to remove " + r.getForename() + " " + r.getSurname().toUpperCase() + ".\nPlease resend with a TIMS internal ID when requesting to edit an existing user");
                                    }
                                    else {
                                        man.setDetail("Retiring user from TIMS");                                        
                                        gen.RetireTimsStaffDetails();
                                        if (!r.getProfession_code().equals("ADMIN")) {
                                            gen.RetireStaffRoles();
                                            gen.RetireStaffLocationDropdown();
                                        }
                                        //will only add/update consultant grade if request is for a doctor. Validation rule
                                        if (r.isDoctor()) {
                                            if (r.isConsultant()) {
                                                gen.RetireConsultantGrade();
                                            }
                                        }
                                        gen.RetireStaffLocationAccess();
                                        gen.RetireStaffRoles();
                                    } 
                                }
                                gen.EndTransaction();
                                requestListWithRequestingLocation.put(requestingLocation, man);
                            }
                        }
                    }
                }
            }  //end of while loop

            if (correctVersion){
                CreateRequestReportDocument(requestListWithRequestingLocation);
            }
        } 
        catch (Exception ex){
            Logger.getLogger(RequestExtractor.class.getName()).log(Level.SEVERE, "Exception has been caught: " + ex, ex);
            success = false;
        }
        finally {
            try {
                
                //closing file streams
                spreadsheetIS.close();
                scriptOS.close();
                
                
            } catch (IOException ex) {
                Logger.getLogger(RequestExtractor.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            }
        }

        return success;
    }

    private static void checkDirectoryExists(String pathString){
        Path path = Paths.get(pathString);

        if (Files.notExists(path)){
            File newDirecotry = new File(pathString);
            newDirecotry.mkdirs();
        }
    }

    private static Request populateRequestObject(Row row) {

        Request request = new Request();
        boolean retVal = false;
        request.setRequestingLocation(requestingLocation);
        //setup a number formatter to remove any unwanted precisions and grouping (xx,xxx)
        //when dealing with numeric values
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        nf.setGroupingUsed(false);

        //check that the content of the first cell is blank.


        String requestCodeCellValue = (row.getCell(COLUMN_HEADERS.REQUEST_CODE.ordinal()).getStringCellValue());
        if (requestCodeCellValue.equals(REQUEST_ACTION.ADDUSER.toString())) {
            request.setRequestAction(REQUEST_ACTION.ADDUSER);
            request.setAddNewAccount(true);

        } else if (requestCodeCellValue.equals(REQUEST_ACTION.ADDUSERACCESS.toString())) {
            request.setRequestAction(REQUEST_ACTION.ADDUSERACCESS);
            request.setAddNewAccount(true);
            request.setNewPasswordNeeded(true);
        } else if (requestCodeCellValue.equals(REQUEST_ACTION.AMEND.toString())) {
            request.setRequestAction(REQUEST_ACTION.AMEND);
            request.setEditExistingAccount(true);
        } else if (requestCodeCellValue.equals(REQUEST_ACTION.CHANGEACCESS.toString())) {
            request.setRequestAction(REQUEST_ACTION.CHANGEACCESS);
            request.setEditExistingAccount(true);
        } else if (requestCodeCellValue.equals(REQUEST_ACTION.GRANTEXISTACCESS.toString())) {
            request.setRequestAction(REQUEST_ACTION.GRANTEXISTACCESS);
            request.setNewPasswordNeeded(true);
        } else if (requestCodeCellValue.equals(REQUEST_ACTION.REMOVEACCESS.toString())) {
            request.setRequestAction(REQUEST_ACTION.REMOVEACCESS);
        } else if (requestCodeCellValue.equals(REQUEST_ACTION.REMOVEUSER.toString())) {
            request.setRequestAction(REQUEST_ACTION.REMOVEUSER);
            request.setRemoveExistingAccount(true);
        } else if (requestCodeCellValue.equals(REQUEST_ACTION.RESETPWD.toString())) {
            request.setRequestAction(REQUEST_ACTION.RESETPWD);
            request.setResetPassword(true);
        } else if (requestCodeCellValue.equals(REQUEST_ACTION.NONE.toString())) {
            request.setRequestAction(REQUEST_ACTION.NONE);
        } else {
            System.out.println("....unknown request action selected. [FAIL]");
            request.setRequestAction(REQUEST_ACTION.NONE);
        }

        if (request.getRequestAction().equals(REQUEST_ACTION.NONE)) {request = null; }
        else {
            //
            request.setProfession_code(row.getCell(COLUMN_HEADERS.PROFESSION_CODE.ordinal()).getStringCellValue());

            //fixme - non theatre nurses are considered by TIMS as ADMIN and won't appear on TIMS / theatre nurses are considered as NURSE
            //fix should be put in on registration form
            if (request.getProfession_code().equals("NURSE")){
                request.setProfession_code("ADMIN");
            } else if (request.getProfession_code().equals("NURSETH")){
                request.setProfession_code("NURSE");
            }

            request.setSpecialty_code(nf.format(row.getCell(COLUMN_HEADERS.SPECIALTY_CODE.ordinal()).getNumericCellValue()));

            String passwordPrivilageCodeCellValue = (row.getCell(COLUMN_HEADERS.PWD_PRIVILAGE_CODE.ordinal()).getStringCellValue());
            if (passwordPrivilageCodeCellValue.equals(PASSWORD_PRIVILAGE.FULL.toString())) {
                request.setPasswordPrivilage(PASSWORD_PRIVILAGE.FULL);
            } else if (passwordPrivilageCodeCellValue.equals(PASSWORD_PRIVILAGE.READONLY.toString())) {
                request.setPasswordPrivilage(PASSWORD_PRIVILAGE.READONLY);
            } else if (passwordPrivilageCodeCellValue.equals(PASSWORD_PRIVILAGE.NONE.toString())) {
                request.setPasswordPrivilage(PASSWORD_PRIVILAGE.NONE);
            } else {
                System.out.println("...unknown password privilege of '" + passwordPrivilageCodeCellValue + "' requested [FAIL]");
            }

            request.setTimsInternalID(nf.format(row.getCell(COLUMN_HEADERS.TIMS_ID_NUMBER.ordinal()).getNumericCellValue()));

            String titleCellValue = row.getCell(COLUMN_HEADERS.TITLE.ordinal()).getStringCellValue();
            if (titleCellValue.contains("*not selected*")) {
                request.setTitle(null);
            } else {
                request.setTitle(titleCellValue);
            }
            request.setForename(WordUtils.capitalizeFully(row.getCell(COLUMN_HEADERS.FORENAME.ordinal()).getStringCellValue()));
            request.setSurname(WordUtils.capitalizeFully(row.getCell(COLUMN_HEADERS.SURNAME.ordinal()).getStringCellValue()));
            request.setTimsInternalID(nf.format(row.getCell(COLUMN_HEADERS.TIMS_ID_NUMBER.ordinal()).getNumericCellValue()));

            Cell startDateCell = row.getCell(COLUMN_HEADERS.START_DATE.ordinal());
            String startDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
            if (startDateCell.getCellType() != Cell.CELL_TYPE_BLANK){
                startDate = new SimpleDateFormat("dd-MMM-yyyy").format(startDateCell.getDateCellValue());
            }
            request.setStartDate(startDate);

            Cell finishDateCell = row.getCell(COLUMN_HEADERS.FINISH_DATE.ordinal());
            String finishDate = null;//new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
            if (finishDateCell.getCellType() != Cell.CELL_TYPE_BLANK){
                finishDate = new SimpleDateFormat("dd-MMM-yyyy").format(finishDateCell.getDateCellValue());
            }
            request.setEndDate(finishDate);

            if (request.getProfession_code().equals("DOCT")){
                if (row.getCell(COLUMN_HEADERS.CONSULTANT.ordinal()).getStringCellValue() == null || row.getCell(COLUMN_HEADERS.CONSULTANT.ordinal()).getStringCellValue() == "N" )
                    request.setIsConsultant(true);
                else
                    request.setIsConsultant(false);
            }else{
                request.setIsConsultant(false);
            }

            Cell surgeonProfessionCell = row.getCell(COLUMN_HEADERS.SURGEON.ordinal());
            if ((!surgeonProfessionCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.SURGEON);
                request.addRole(ROLES.ASST_SURGEON);
            }

            Cell anaeProfessionCell = row.getCell(COLUMN_HEADERS.ANAESTHETIST.ordinal());
            if ((!anaeProfessionCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.ANAE);
                request.addRole(ROLES.ASST_ANAE);
            }

            Cell radiologistCell = row.getCell(COLUMN_HEADERS.RADIOLOGIST.ordinal());
            if ((!radiologistCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.RADIO);
            }

            Cell anaePracCell = row.getCell(COLUMN_HEADERS.ANAESTHETIC_PRACTITIONER.ordinal());
            if ((!anaePracCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.ANAE_PRAC);
            }

            Cell scrubPracCell = row.getCell(COLUMN_HEADERS.SCRUB_PRACTITIONER.ordinal());
            if ((!scrubPracCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.SCRUB_PRAC);
            }

            Cell circPracCell = row.getCell(COLUMN_HEADERS.CIRCULATING_PRACTITIONER.ordinal());
            if ((!circPracCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.CIRC_PRAC);
            }

            Cell haemoPracCell = row.getCell(COLUMN_HEADERS.HAEMOSTASIS_PRACTITIONER.ordinal());
            if ((!haemoPracCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.HAEM_PRAC);
            }

            Cell recovPracCell = row.getCell(COLUMN_HEADERS.RECOVERY_PRACTITIONER.ordinal());
            if ((!recovPracCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.RECOV_PRAC);
            }

            Cell perfusionistCell = row.getCell(COLUMN_HEADERS.PERFUSIONIST.ordinal());
            if ((!perfusionistCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.PERF);
            }

            Cell trPerfusionistCell = row.getCell(COLUMN_HEADERS.TRAINEE_PERFUSIONIST.ordinal());
            if ((!trPerfusionistCell.getStringCellValue().isEmpty()) && (!request.getProfession_code().equals("ADMIN"))) {
                request.addRole(ROLES.TR_PERF);
            }

            Cell chLocationCell = row.getCell(COLUMN_HEADERS.CHURCHILL_THEATRES.ordinal());
            if (!chLocationCell.getStringCellValue().isEmpty()){
                request.AddLocation(LOCATIONS.CH);
            }

            Cell jr1LocationCell = row.getCell(COLUMN_HEADERS.JR1_THEATRES.ordinal());
            if (!jr1LocationCell.getStringCellValue().isEmpty()){
                request.AddLocation(LOCATIONS.JR1);
            }

            Cell jr2LocationCell = row.getCell(COLUMN_HEADERS.JR2_THEATRES.ordinal());
            if (!jr2LocationCell.getStringCellValue().isEmpty()){
                request.AddLocation(LOCATIONS.JR2);
            }

            Cell hhLocationCell = row.getCell(COLUMN_HEADERS.HORTON_THEATRES.ordinal());
            if (!hhLocationCell.getStringCellValue().isEmpty()){
                request.AddLocation(LOCATIONS.HH);
            }

            Cell wwLocationCell = row.getCell(COLUMN_HEADERS.WEST_WING_THEATRES.ordinal());
            if (!wwLocationCell.getStringCellValue().isEmpty()){
                request.AddLocation(LOCATIONS.WW);
            }
        }

        /*//validation of the read in data
        if (request.getRequestAction() != REQUEST_ACTION.NONE) {
            //valdation of data provided
            if (!request.basicInfoProvided()) {
                System.out.println("...basic staff details not provided [FAIL]");
                request = null;
            } else {
                //more in depth checks
                if (request.getProfession_code().equals("NONE")) {
                    System.out.println("...no profession code provided [FAIL]");
                }

                if (request.getLocationCollection().isEmpty()) {
                    System.out.println("...no locations selected for user [FAIL]");
                }
                request = null;
            }
        }*/
        return request;
    }

    enum REQUEST_ACTION {
        NONE,
        ADDUSER,
        ADDUSERACCESS,
        AMEND,
        GRANTEXISTACCESS,
        RESETPWD,
        CHANGEACCESS,
        REMOVEUSER,
        REMOVEACCESS
    }

    enum PASSWORD_PRIVILAGE {

        NONE,
        FULL,
        READONLY
    }

    enum ROLES {

        SURGEON,
        ASST_SURGEON,
        ANAE,
        ASST_ANAE,
        ANAE_PRAC,
        RECOV_PRAC,
        CIRC_PRAC,
        SCRUB_PRAC,
        HAEM_PRAC,
        PERF,
        TR_PERF,
        RADIO
    }

    enum LOCATIONS {

        CH,
        JR1,
        JR2,
        HH,
        WW,
        UNKNOWN
    }

    //This must match the headers used on the TIMS registration form,
    //used to identify the location of data in related columns of spreadsheet rows
    enum COLUMN_HEADERS {
        HIDDEN,
        REQUEST_ACTION,
        TIMS_ID_NUMBER,
        TITLE,
        FORENAME,
        SURNAME,
        SECURITY_BADGE_ID,
        CONTACT_NUMBER,
        START_DATE,
        FINISH_DATE,
        PROFESSION,
        CONSULTANT,
        SPECIALTY,
        SURGEON,
        ANAESTHETIST,
        RADIOLOGIST,
        ANAESTHETIC_PRACTITIONER,
        RECOVERY_PRACTITIONER,
        SCRUB_PRACTITIONER,
        CIRCULATING_PRACTITIONER,
        HAEMOSTASIS_PRACTITIONER,
        PERFUSIONIST,
        TRAINEE_PERFUSIONIST,
        CHURCHILL_THEATRES,
        JR1_THEATRES,
        JR2_THEATRES,
        WEST_WING_THEATRES,
        HORTON_THEATRES,
        PASSWORD_PRIVILAGES,
        REQUEST_CODE,
        SPECIALTY_CODE,
        PROFESSION_CODE,
        PWD_PRIVILAGE_CODE
    }
}
