package main_QANameUpdater;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 
 * @author Nicholas Ostaffe
 * @date 8/1/2017
 * 
 * Personal tool aimed at assisting the manual correction of file names that fell through QANameUpdater.
 *
 */
public class QuickRename {
	static File file = new File("Z:\\ERP Change Management Library\\QA_Locked\\PY3058-DeleteInvalidUnusedComboCode");
	static int lowerRange = 2;
	static int upperRange = 4;
	static int val = 1;
	
	static DecimalFormat df = new DecimalFormat("#00");
	public static void main(String[] args) {
		File[] files = file.listFiles();
		for (File f : files) {
			if(f.isDirectory() && f.getName().subSequence(0, 1).equals("A") && f.getName().contains("-")) {
				int x;
				try {
					x = Integer.parseInt(f.getName().substring(1,3));
				} catch(Exception e) {
					e.printStackTrace();
					continue;
				}
				if (x >= lowerRange && x <= upperRange) {
				 f.renameTo(new File(f.getParent() + File.separator + "A"+ df.format(x+val) + f.getName().substring(3)));
				}
			}
		}
	}
}
