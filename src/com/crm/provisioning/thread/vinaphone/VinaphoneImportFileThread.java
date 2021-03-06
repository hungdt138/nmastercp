/**
 * 
 */
package com.crm.provisioning.thread.vinaphone;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import com.crm.kernel.io.FileUtil;
import com.crm.kernel.io.WildcardFilter;
import com.crm.thread.DispatcherThread;
import com.crm.thread.util.ThreadUtil;

/**
 * @author hungdt
 * 
 */
public class VinaphoneImportFileThread extends DispatcherThread {
	public String importingDirectory = "";
	public String backupDirectory = "";
	public String wildcard = "";

	private String backupFilePrefix = "";
	private String backupFilePostfix = "";
	private String runDate = "";
	public int startTimeInDay = 0;
	public int endTimeInDay = 0;
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat sdf = new SimpleDateFormat("HH");

	public void setBackupFilePrefix(String backupFilePrefix) {
		this.backupFilePrefix = backupFilePrefix;
	}

	public String getBackupFilePrefix() {
		if (backupFilePrefix != null)
			return backupFilePrefix;
		else
			return "";
	}

	public void setBackupFilePostfix(String backupFilePostfix) {
		this.backupFilePostfix = backupFilePostfix;
	}

	public String getBackupFilePostfix() {
		if (backupFilePostfix != null)
			return backupFilePostfix;
		else
			return "";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Vector getParameterDefinition() {
		Vector vtReturn = new Vector();
		vtReturn.add(ThreadUtil.createTextParameter("importingDirectory", 400,
				"Importing directory path."));
		vtReturn.add(ThreadUtil.createTextParameter("backupDirectory", 400,
				"Backup directory path."));
		vtReturn.add(ThreadUtil.createTextParameter("wildcard", 400,
				"Wildcard for importing files."));
		vtReturn.add(ThreadUtil.createTextParameter("runDate", 400, ""));
		vtReturn.add(ThreadUtil.createTextParameter("startTimeInDay", 100,
				"Thoi gian bat dau chay trong ngay"));
		vtReturn.add(ThreadUtil.createTextParameter("endTimeInDay", 100,
				"Thoi gian ket thuc chay trong ngay"));
		vtReturn.addAll(super.getParameterDefinition());
		return vtReturn;
	}

	@Override
	public void fillDispatcherParameter() throws Exception {
		importingDirectory = ThreadUtil.getString(this, "importingDirectory",
				false, "/");
		backupDirectory = ThreadUtil.getString(this, "backupDirectory", false,
				"/");
		wildcard = ThreadUtil.getString(this, "wildcard", false, "*.cdr");
		runDate = ThreadUtil.getString(this, "runDate", false, "");
		startTimeInDay = ThreadUtil.getInt(this, "startTimeInDay", 8);
		endTimeInDay = ThreadUtil.getInt(this, "endTimeInDay", 9);
		// TODO Auto-generated method stub
		super.fillDispatcherParameter();
	}

	private String validateBackupDirectory() throws Exception {

		Calendar timeToRun = Calendar.getInstance();
		// timeToRun.add(Calendar.DATE, -1);

		if (!runDate.equals("")) {
			timeToRun.setTime(df.parse(runDate));
		}

		File directory = new File(backupDirectory + "/"
				+ df.format(timeToRun.getTime()));
		if (!directory.exists()) {
			debugMonitor("Directory " + backupDirectory + " does not exist.");
			if (!directory.mkdirs()) {
				throw new Exception("Can not create directory: "
						+ backupDirectory);
			} else {
				debugMonitor("Directory " + backupDirectory
						+ " has been created.");
			}
		}

		return backupDirectory + "/" + df.format(timeToRun.getTime());
	}

	private String[] loadFile(File file) throws Exception {
		FileReader fileReader = null;

		String[] fileData = null;

		try {
			StringBuilder strBuilder = new StringBuilder();
			char[] buffer = new char[1024];
			debugMonitor("Reading file " + file.getName());
			fileReader = new FileReader(file);
			int count = -1;
			do {
				count = fileReader.read(buffer, 0, buffer.length);
				if (count > 0)
					strBuilder.append(buffer, 0, count);
			} while (count > 0);

			String fileContent = strBuilder.toString();
			if (fileContent.contains("\r\n"))
				fileData = fileContent.split("\r\n");
			else if (fileContent.contains("\n"))
				fileData = fileContent.split("\n");
			else
				fileData = fileContent.split("\r");

			return fileData;
		} catch (Exception e) {
			throw e;
		} finally {
			if (fileReader != null)
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public void doProcessSession() throws Exception {
		Calendar now = Calendar.getInstance();
		if (Integer.parseInt(sdf.format(now.getTime())) >= startTimeInDay
				&& Integer.parseInt(sdf.format(now.getTime())) <= endTimeInDay) {
			
			Calendar timeToRun = Calendar.getInstance();

			// Vinaphone khong can tru di 1 ngay
			// timeToRun.add(Calendar.DATE, -1);

			if (!runDate.equals("")) {
				timeToRun.setTime(df.parse(runDate));
			}

			File importingDir = new File(importingDirectory + "/"
					+ df.format(timeToRun.getTime()));

			debugMonitor("Checking importing directory " + importingDirectory
					+ " ...");

			if (!importingDir.exists()) {
				throw new Exception("Can not find importing directory: "
						+ importingDirectory);
			}

			debugMonitor("Searching by wildcard " + wildcard + " ...");
			File[] files = importingDir.listFiles(new WildcardFilter(wildcard));

			if (files == null || files.length == 0) {
				debugMonitor("Searched 0 file.");
			}

			for (int k = 0; k < files.length && isAvailable(); k++) {
				try {
					File file = files[k];
					String[] fileData = loadFile(file);

					String backupFileName = file.getName();

					setBackupFilePrefix("");
					setBackupFilePostfix("");

					/**
					 * Processes file data
					 */
					boolean hasError = !fileDataProcessing(fileData);

					/**
					 * Prepares backup file
					 */
					backupFileName = getBackupFilePrefix() + backupFileName;
					backupFileName = backupFileName + getBackupFilePostfix();
					if (hasError)
						backupFileName = backupFileName + ".error";

					/**
					 * Validates file & directory
					 */
					String backupDir = validateBackupDirectory();
					if (backupDir.endsWith("/") || backupDir.endsWith("\\"))
						backupFileName = backupDir + backupFileName;
					else
						backupFileName = backupDir + "/" + backupFileName;

					/**
					 * Backups file
					 */
					File backupFile = new File(backupFileName);
					if (FileUtil.renameFile(file.getAbsolutePath(),
							backupFile.getAbsolutePath(), true)) {
						debugMonitor("Backup file " + file.getName() + " to "
								+ backupFile.getPath());
					} else {
						debugMonitor("Can not backup file " + file.getName()
								+ " to " + backupFile.getPath());
					}

				} catch (Exception e) {
					debugMonitor(e);
				}
			}
		}

	}

	/**
	 * 
	 * @param fileData
	 * @return processing success or not
	 */
	public boolean fileDataProcessing(String[] fileData) {
		return true;
	}
}
