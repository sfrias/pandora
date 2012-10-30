package com.tbitsglobal.ddc.rest;

import org.apache.log4j.Logger;

/*
 * Notes
 Detect PageFormat : PageFormat PDDocument.getPageFormat(int pageIndex);
 */
public class DDCObject {
	
	private static final Logger logger = Logger.getLogger(DDCObject.class);
	
	/**
	 * 1. Routine – 1: Identification of Agency and Expected Value Identification
	 * 2. Routine for Identification of the Document Transmittal Note
	 * 3. Routine – 2: Identification of all attributes of the documents received using the searching of information from tBits
	 * 4. Routine-3: Extraction of information-OCR
	 * 5. Routine-4: Reconciliation between the information received from OCR & Information received from DTN.
	 * 6. Routine – 5: Updating the Records – upon successful reconciliation of the information received from DTN and OCR of files.
	 * 7. Routine – 6: Proceed with the inward of the information
	 */
	public   void doDDC()
	{
		/*
		New Email received in Inboxes related to the
		digital document controller assistant BA.
		There can be several email ID’s from which
		the emails will be collected into the system
		*/
		
		/*
		 * Fetch Emails from the inbox and create the
record in the inward. Send
Acknowledgement.
		 */
		/*
		Determine the sender of the email, compare
the same with the allowance processes for
From_Agency_Email_Address:
*/
		boolean hasAttachments = doesEmailHasAttachments();
		if( hasAttachments )
		{
			boolean isDDCExpected = isDDCExpectedFromSender();
			if(isDDCExpected)
			{
				boolean foundPatternMatchingFile = findPatternMatchingFile();
				if(foundPatternMatchingFile)
				{
					boolean textExtracted = canTextBeExtracted();
					if( textExtracted )
					{
						
					}
					else
					{
						// for each file
						{
							boolean foundText = processFirstPageWithOCR();
							if( foundText )
							{
								// 
								searchAgencyBAForSearch();
							}
							else
							{
								continue;
							}
						}
							
					}
				}
				else
				{
					boolean foundA4PageSizeFile = findA4PageSizeFile();
					if( foundA4PageSizeFile )
					{
						processEachA4PageSizeFile();
					}
					else
					{
						
					}
				}
			}
			else
			{
				selectAllProcessAndProceed();
			}
		}
		else
		{
			emailDDC();
		}
	}
	/**
	 * @return 
	 * 
	 */
	private boolean processFirstPageWithOCR() {
		return false;
		/*
Pass the first page 
through the DTN 
Letter Paraemeters 
as specified in 
Extraction of 
information-OCR 
table and compare 
the values with 
those specified in 
the Regex.
		 */
	}
	/**
	 * @param 
	 */
	private   void processEachA4PageSizeFile() {
		/*
Perform the below test
for all files.
		 */
		
		boolean foundValues = findValuesThroughTextExtraction();
		if( foundValues )
		{
			
		}
		else
		{
			/*
			Are you able to
			get the values
			expected of the
			DTN?
			*/
			boolean foundValuesThroughOCR = findValuesThroughOCR();
			if( foundValuesThroughOCR )
			{
				processDTNValues();
			}
			else
			{
				

			}
		}
	}
	/**
	 * @param 
	 */
	private   void processDTNValues() {
		searchAgencyBAForSearch();
		
		// for each file in attachments except DTN file
		processAttachments();
		ocrForDrawings();
		boolean foundPattern = findPattern();
		if( foundPattern)
		{
			updateInformation();
		}
		else
		{
			
		}
		
		boolean foundInformationForAllAttachments = findInfomationForAllAttachments();
		if( foundInformationForAllAttachments )
		{
			boolean reconcileSuccessful = reconcileFiles();
			if( reconcileSuccessful )
			{
				performQualityChecks();
				updateAgencyBAForSearch();
				
				createDTN();
				
				updateTBitsRecords();
				/*
				 Process Ends Successfully.
				 */
				logger.info("Process Ends Successfully.");
			}
			else
			{
				updateRecordWithReconciliation();
			}
		}
		else
		{
			// show error
			updateRecordWithFileFound();
		}
		
	}
	/**
	 * 
	 */
	private void updateRecordWithReconciliation() {
		/*
Update the record with the 
reconciliation results and STOP. 
Human intervention Required.
Ends Unsuccessfully.
		 */
	}
	/**
	 * 
	 */
	private boolean reconcileFiles() {
		/*
		Are you able to reconcile 
		all the files received with 
		the information 
		contained in the DTN/
		tBits Records?
		*/
		return false;
	}
	/**
	 * 
	 */
	private void updateRecordWithFileFound() {
		/*
		Update the record with 
		files found and the files 
		that could not be 
		processed and STOP. 
		Human intervention 
		Required. Ends 
		Unsuccessfully.
		*/
	}
	/**
	 * @return
	 */
	private boolean findInfomationForAllAttachments() {
		/*
		Are you able to get the 
		infromation requested for 
		all the enclosed 
		attachment files?
		*/
		return false;
	}
	/**
	 * @param 
	 */
	private   void updateInformation() {
		/*
Update the information received in
the HAshMAP that contains the
information received from DTN and
tBits records.
		 */
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findPattern() {
		/*
Check if you are able to
extract information
complying to the Pattern
as specified against the
same.
		 */
		return false;
	}
	/**
	 * @param 
	 */
	private   void ocrForDrawings() {
		/*
Pass the files through the expected OCR
Values for drawings/documents and
other attributes specified except for the
DTNLetter Value.
		 */
	}
	/**
	 * @param 
	 */
	private   void processAttachments() {
		/*
Extract the page information for all the
		attachments – first page – for all files
		except for the DTN Note file.
		 */
	}
	/**
	 * @param 
	 */
	private   void searchAgencyBAForSearch() {
		/*
		Search for the BA specified under -
		Agency_BA_For_Search: using the Key =
		Primary_Key_for_Search: and extract the
		request ID’s and all associated fields
		information as has been specified under -
		Relationship_Keys_BAFields for the
		Process.
				 */
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findValuesThroughOCR() {
		/*
		Pass the first page
		through the DTN
		Letter Paraemeters
		as specified in
		Extraction of
		information-OCR
		table and compare
		the values with
		those specified in
		the Regex.
					 */
		return false;
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findValuesThroughTextExtraction() {
		/*
		Are you able to
		extract the
		values using the
		PDF text
		extraction?
				 */
		return false;
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findA4PageSizeFile() {
		/*
Are there any files whose
first page size is A4 /
Letter Portraist settings –
		 */
		return false;
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findPatternMatchingFile() {
		/*
		Is there a file whose name is
		following a pattern similar
		to
		Expected_DTN_File_Naming
		_Convention?
		*/
		return false;
	}
	/**
	 * @param 
	 */
	private   void emailDDC() {
		/*
		Stop – email to document
		controller stating no attachments
		included. May need human
		intervention.
		*/
	}
	
	/**
				 Select all Processes for which
the user is allowed to interact
with the other agency from the
table - Process_DTN_Analysis
AND where the category ID as
what has been specified in the
tBits Record
	 */
	private   void selectAllProcessAndProceed() {
		
		checkProjectCode();
		determinePageSize();
		selectApplicableProcesses();
		doOCR();
		boolean foundOCRValue = findOCRValue();
		if( foundOCRValue )
		{

			/*
			For the selected
			ProcessID – select
			the values of the
			parameter Project
			Code, Revision,
			Contractor
			Document Number
			etc etc as has been
			specified in the table
			of Extraction of
			information-OCR
			*/
			
			/*
Search in the
specified BA as
mentioned under
Agency_BA_For_Sea
rch: for the said BA
to determine the
RequestID to be
updated.

Perform the above
operation for all the
files enclosed.
			 */
			
			performQualityChecks();
			
			updateAgencyBAForSearch();
			
			createDTN();
			
			updateTBitsRecords();
			/*
			 Process Ends Successfully.
			 */
			logger.info("Process Ends Successfully.");
		}
		else
		{
			// NO ALGO MENTIONED in flowchart
		}
	}
	/**
	 * @param 
	 */
	private   void updateTBitsRecords() {
		/*
		 Get the DTN
Number created and
update the tBits
records with the
success status.
		 */
	}
	/**
	 * @param 
	 */
	private   void createDTN() {

		/*
Create the DTN
using the
information as is
available under -
DTNProcessInputs
		 */
	}
	/**
	 * @param 
	 */
	private   void updateAgencyBAForSearch() {
		/*
Update the
Agency_BA_For_Sea
rch: and the
associated records
with the information
as available in
BAUpdationsWtRece
ivedValues
		 */
	}
	/**
	 * @param 
	 */
	private   void performQualityChecks() {
		/*
		Perform the quality
		checks on the
		documents as per
		the required
		parameters.
		*/
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findOCRValue() {
		/*
Are you able to find the
OCR Value matching the
pattern as defined in the
OCR Table for a particular
process ID?
		 */
		return false;
	}
	/**
	 * @param 
	 */
	private   void doOCR() {
		/*
Run the file through the project
code ONLY search via the OCR.
		 */
	}
	/**
	 * @param 
	 */
	private   void selectApplicableProcesses() {
		/*
Based on the page size
specifications – select all the
applicable Processes related to
the search of the ProjectCode:
Parameter from the Extraction
of information-OCR table.
		 */
	}
	/**
	 * @param 
	 */
	private   void determinePageSize() {
		/*
Determine the page size of the
incoming files.
		 */
	}
	/**
	 * @param 
	 */
	private   void checkProjectCode() {
		/*
Check for the Parameter of
Project Code for the said
processes.
		 */
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean isDDCExpectedFromSender() {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean doesEmailHasAttachments() {
		// TODO Auto-generated method stub
		return false;
	}
}
