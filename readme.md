

TIMS Request Extractor
======================

Description
-----------
This is a simple command line application that is used to process user request forms for the Theatre Information System (TIMS). Each request form is presented as an Excel spreadsheet that can have many rows each relating to a separate request relating to the management of a user on TIMS. 

Source location
---------------
The code is held in this [Git repository](https://github.com/hizni/tims-request-extractor).

Project files
-------------
When checked out, the Java project will have the following directory structure

    /src
    /lib
    /resourceFiles
	    /testResources

The resourceFiles directory contains the following files and directories:
>**TIMS request form.xslx** - latest version of the TIMS request form
>**TIMS user census.sql** - SQL script that produces output sent as TIMS user census
>**request-extractor.bat** - batch file used to execute compiled Jar
>**timsRequestTemplate.docx** - MS Word template for request report

>**/testResources**
	

The src directory contains the following files:
> **Request.java** - represents an request (ie: a row from the TIMS request form).
>**RandomPasswordGenerator.java** - helper class that holds logic to generate a random password string. Has several settings to allow additional strength, complexity to the password that it produces.
>**RequestReport.java** - represents the report that is generated as a product of the process. This report will provide information to the Theatre Administrators relating to the requests that they have made (ie: any issues, successful processing, etc).
>**DataAccess.java** - provides an abstracted data access layer that allows an instance of the class to perform specific pre-defined queries against the TIMS database.
>**GenerateSQL.java** - generates the SQL that is written to the the SQL script file. This script file will be executed by an appropriate database developer to perform actions against the TIMS database.
>**RequestExtractor.java** - the main program that iterates over the TIMS request forms. On it's first run it will create the following directory structure that will be used by the application.

    /files
        	/requests           deposit all TIMS requests that need to be processed
			/done               successfully completed requests
            /error
        	/output
    		   /sql
    		   /requestReports
    		/resources
    	

Usage
-----
After compiling the source to a JAR file, place the batch file (from resourceFiles) in the same directory. The application will be run by executing this batch file.

**Initial run**

The initial run of the application will create the appropriate directory structure.
Copy `timsRequestTemplate.docx` to the /files/resources directory.

**Subsequent runs**
Any request files in /files/reqeuests will be processed.
They will be deleted from here and moved to /files/done or /files/error accordingly.
The output from a run will be directed to /files/output

Testing
-------
To test the application place the test TIMS request (TIMS request form-testing.xlsx) in the /requests directory and run the application. Check the expected output with the checklist. 


> Written with [StackEdit](https://stackedit.io/).