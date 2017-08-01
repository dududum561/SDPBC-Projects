package main_QANameUpdater;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

import main_QANameUpdater.WindowsSorter.WindowsExplorerComparator;
/**
 * 
 * @author Nicholas Ostaffe
 * @date 8/1/2017
 * @version 1.1
 * 
 * QANameUpdater is meant as a one-time use to rename the entirety of the SDPBC'S QA
 * Libray using a new naming convention : "A##-[bug_number]-[job_type]"
 *
 */
public class QANameUpdater{
	static File[] qaLocked = new File("Z:\\ERP Change Management Library\\QA_Locked\\PD3283-ProcessStmtsPcard").listFiles();
	
	static DecimalFormat df = new DecimalFormat("#00");

	public static void main(String[] args) throws IOException, Xlsx4jException, Docx4JException {
		long time = System.currentTimeMillis();
		go();
		System.out.println("Done. " + (System.currentTimeMillis()-time)/1000 + "s");
	}

	public static void go() throws Xlsx4jException, Docx4JException {
		
		File outputFail = new File(System.getProperty("user.dir")+"\\outFail.txt");
		File outputSuccess = new File(System.getProperty("user.dir")+"\\outSuccsess.txt");
		int successCounter = 0;
		int failureCounter = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		for(File dir : qaLocked) {
			if(dir.isDirectory()) {
				File[] subDirs = dir.listFiles();
				
				// Sorts the files based on Windows Explorer Name sorting convention
				Arrays.sort(subDirs, new Comparator<File>(){
					private final Comparator<String> NATURAL_SORT = new WindowsExplorerComparator();
					@Override
					public int compare(File o1, File o2) {
						;
						return NATURAL_SORT.compare(o1.getName(), o2.getName());
					}
				});
				
				// Keeps track of A01, A02, A03,... etc.
				int versionCounter = 1;
				
				for (File subDir : subDirs) {
					if(subDir.isDirectory()) {
						
						// Gets calendar instance to record time in output logs
						Calendar cal = Calendar.getInstance();
						
						File[] files = subDir.listFiles();
						Arrays.sort(files);
						boolean renamedSubDir = false;
						
						for(File file : files) {
							if(file.getName().contains("-") && file.getName().split("-")[0].equals("000")) {
								
								//Attempt to open excel file
								SpreadsheetMLPackage xlsxPkg = null;
								WorkbookPart workbookPart = null;
								try {
									xlsxPkg = SpreadsheetMLPackage.load(new File(file.getAbsolutePath()));		
									workbookPart = xlsxPkg.getWorkbookPart();
								} catch (Exception e) {
									continue;
								}
								for(int i=0; i<10; i++) {
									WorksheetPart sheet = null;
									try {
										sheet = workbookPart.getWorksheet(i);
									} catch (Xlsx4jException e) {
										continue;
									}
									
									DataFormatter formatter = new DataFormatter();
							
									// Calls displayContent() to find bug number and job type 
									String[] nameContents = displayContent(sheet, formatter);
									if(nameContents!=null && nameContents[0]!=null && nameContents[1]!=null) {
										
										String beforePath = subDir.getAbsolutePath();
										String afterPath = subDir.getParent() + "\\" + "A"+ df.format(versionCounter) + "-"+ nameContents[0] + "-" + nameContents[1];
										
										// Rename directory based on returned data from displayContent() in nameContents
										subDir.renameTo(new File(afterPath));
										
										// Log success
										appendToFile(outputSuccess, "Renamed:"  + sdf.format(cal.getTime()) + ": " + beforePath + " to "+ afterPath + "\r\n");
										
										renamedSubDir = true;
										successCounter++;
										versionCounter++;
										break;
									} else {
										renamedSubDir = false;
									}
								}
								if (renamedSubDir) {
									break;
								}
							}
						}
						if (!renamedSubDir){
							failureCounter++;
							
							// Log failure
							appendToFile(outputFail, "Not renamed:" + sdf.format(cal.getTime()) + ": " + subDir.getAbsolutePath() + "\r\n");
						}
					}
				}
			}
		}
		System.out.println("Succsessful renames: " + successCounter);
		System.out.println("Unsuccsessful renames: " + failureCounter);
	}
	
	/**
	 * Finds the bug number and a job type in a 000 document
	 * 
	 * @param sheet
	 * @param formatter
	 * @return an array containing the bug number and job type
	 * @throws Docx4JException
	 */
	private static String[] displayContent(WorksheetPart sheet, DataFormatter formatter) throws Docx4JException{
		try {
			Worksheet ws = sheet.getContents();
			SheetData data = ws.getSheetData();
			
			String bugNumber = null;
			String jobType = null;
			
			boolean flag1 = false;
			boolean flag2 = false;
			
			boolean nextBugCell = false;
			boolean nextTypeCell = false;
			
			for (Row r : data.getRow() ) {				
				for (Cell c : r.getC() ) {					
		            String text = formatter.formatCellValue(c);
		           
		            if(nextBugCell && !flag1 && text != null && !text.isEmpty()) {
		            	bugNumber = text.replaceAll("[^0-9]", "").substring(0,4);
		            	flag1 = true;
		            }
		            
		            if(nextTypeCell && !flag2 && text != null && !text.isEmpty() ) {
		            	jobType = text.substring(0,1);
		            	flag2 = true;
		            }
		            
		            if(text.toLowerCase().contains("tracking log number")) {
		            	nextBugCell = true;
		            }
		            
		            if(text.toLowerCase().contains("project type (")) {
		            	nextTypeCell = true;
		            }
		            	            
		            if (flag1 && flag2) {
		            	String[] str = {bugNumber, jobType};
		            	return str;
		            }
				}
		            
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	/**
	 * Appends a string to a file
	 * 
	 * @param file is the file to append to
	 * @param string is the string to append
	 */
	public static void appendToFile(File file, String string) {
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {e.printStackTrace();}	
		}
		try {
			Files.write(Paths.get(file.getPath()), string.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {e.printStackTrace();}
	}
}
