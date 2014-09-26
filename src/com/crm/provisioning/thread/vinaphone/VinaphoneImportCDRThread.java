/**
 * 
 */
package com.crm.provisioning.thread.vinaphone;

import java.util.ArrayList;
import java.util.List;

import com.crm.provisioning.thread.CDRImpl;

/**
 * @author hungdt
 * 
 */
public class VinaphoneImportCDRThread extends VinaphoneImportFileThread {

	private List<VinaphoneCDR> cdrs = new ArrayList<VinaphoneCDR>();

	public boolean fileDataProcessing(String[] fileData) {
		String fileBackupPostfix = "";

		boolean hasError = false;
		
		for(int i = 0; i < fileData.length; i++)
		{
			
			try {
				VinaphoneCDR cdr = VinaphoneCDR.createCDRFromFileString(fileData[i]);
				
				cdrs.add(cdr);
				
				debugMonitor("Add records: " + cdr.toString());
			} catch (Exception e) {
				debugMonitor("Can not parse CDR: " + fileData[i]);
				debugMonitor(e);
			}
			
		}
		
		
		debugMonitor("Added " + cdrs.size() + " records successfull");
		
		
		try {
			CDRImpl.insertCDRVinaphone(cdrs.toArray(new VinaphoneCDR[]{}));
			debugMonitor("######## Insert done!");
			cdrs.clear();
		} catch (Exception e) {
			debugMonitor(e);
			fileBackupPostfix = ".txt";
			hasError = true;
		}
		
		setBackupFilePostfix(fileBackupPostfix);
		return !hasError;
	}
	
}
