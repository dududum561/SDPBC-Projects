package main_QANameUpdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 
 * @author Nicholas Ostaffe
 * @date 8/1/2017
 * 
 * Prevents extra fall-through from QANameUpdater (ran this program first) by creating a folder
 * if one doesn't exist and putting all files in the found directory in that newly created folder.
 *
 */
public class PreventFallThrough {
	
	static File[] qaLocked = new File("Z:\\ERP Change Management Library\\QA_Locked").listFiles();
	
	public static void main(String[] args) throws IOException {
		for (File dir : qaLocked) {
			if (dir.isDirectory()) {
				boolean atleastOneSubdir = false;
				File[] subDirs = dir.listFiles();
				Arrays.sort(subDirs);
				for (File subDir : subDirs) {
					if (subDir.isDirectory()) {
						atleastOneSubdir = true;
						break;
					}
				}
				if(!atleastOneSubdir) {
					File v1 = new File(dir.getAbsolutePath() + File.separator + "v1");
					v1.mkdir();
					for (File subDir : subDirs) {
						copyFileUsingStream(subDir, new File(v1.getAbsolutePath() + File.separator + subDir.getName()));
					}
				}
			}
		}
	}
	private static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    boolean toDelete = true;
	    boolean deleted = true;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } catch(Exception e) {
	    	e.printStackTrace();
	    	toDelete = false;
	    } finally {
	        is.close();
	        os.close();
	        if(toDelete) {
	        	deleted = source.delete();
	        	{
	        		System.out.println("Deleted" + deleted + " " + source.getAbsolutePath());
	        	}
	        }
	    }
	}
}
