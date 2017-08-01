package main_TCMover;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 * @author Nicholas Ostaffe
 * @date 07/25/2017
 * @version 2.1
 *
 */
public class TCMover extends Application {
	@SuppressWarnings("unused")
	private static Stage mainStage;

	@SuppressWarnings("unused")
	private static TCMoverController tcMoverController;

	private Scene main;

	static File[] sharepointFiles = null;

	@Override
	/**
	 * 
	 * Configures the main UI view from TCMover_Main.fxml
	 */
	public void start(final Stage stage) throws IOException {
		TCMover.mainStage = stage;
		stage.setTitle("TCMover v2.1  -- Nicholas Ostaffe -- Source at https://github.com/supergoa");

		FXMLLoader loader = new FXMLLoader(TCMover.class.getResource("TCMover_Main.fxml"));

		try {
			main = new Scene(loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}

		TCMover.tcMoverController = loader.getController();

		// When the user closes the main application, this ensures the instructions
		// window will close as well.
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				Platform.exit();
			}
		});

		stage.setScene(main);
		stage.setResizable(false);
		stage.show();
	}

	public static void main(String[] args) throws IOException {
		Application.launch(args);
	}

	/**
	 * Finds all files at lookAtPath and iterates over each one for parsing and copying
	 * 
	 * @param lookAtPath is the path to the folder containing test cases to be copied & parsed
	 * @param outputPath is the path to the output location specified by the user
	 * @return 
	 * @throws IOException
	 */
	public static boolean go(String lookAtPath, String outputPath, boolean replaceFiles) throws IOException {
		
		if(lookAtPath != null && !lookAtPath.isEmpty() && outputPath != null && !outputPath.isEmpty()) {

			File input = new File(lookAtPath);
			File output = new File(outputPath);
			
			if(input.isDirectory() && output.isDirectory()) {

				long time = System.currentTimeMillis();
				sharepointFiles = input.listFiles();
		
				 for(File file : sharepointFiles) {
					 if(file.isFile()) {
						 copyFileUsingJava7Files(file, new File(output.getAbsolutePath() + File.separator + parseFileName(file.getName())), replaceFiles);
					 }
				 }
				 System.out.println("Done. " + (System.currentTimeMillis()-time)/1000 + "s");
				 return true;
			}
			else {
				loadFXML("TCMover_InvalidPath.fxml", "Warning!");
				return false;
			}
		}
		else {
			loadFXML("TCMover_Warning.fxml", "Warning!");
			return false;
		}
	}
	
	 public static void loadFXML(String fxml, String title) {
	        try{
	            FXMLLoader fxmlLoader = new FXMLLoader(TCMover.class.getResource(fxml));
	            Parent root = (Parent) fxmlLoader.load();
	            Stage stage = new Stage();
	            stage.setTitle(title);
	            stage.setScene(new Scene(root));
	            stage.show();
	          }
	        catch(Exception e) {
	        	e.printStackTrace();
	        }	
	}
	

	/**
	 * Copies files from one location to another using java.nio's implementation
	 * 
	 * @param source is the source location for the file
	 * @param dest is the destination location of the file to be copied
	 * @param replaceEstisting denotes whether to replace files that have the same name
	 * @throws IOException
	 */
	private static void copyFileUsingJava7Files(File source, File dest, boolean replaceExisting) throws IOException {
	    if(replaceExisting) {
	    	copyFileUsingStream(source, dest);
	    }
	    else {
	    	try {
	    		Files.copy(source.toPath(), dest.toPath());
	    	}
	    	catch (java.nio.file.FileAlreadyExistsException e) {}
	    	catch (Exception e) {e.printStackTrace();}
	    }
	}
	
	/**
	 * Copies files from one location to another using streams (for overwriting)
	 * 
	 * @param source is the source location for the file
	 * @param dest is the destination location of the file to be copied
	 * @throws IOException
	 */
	private static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	
	/**
	 * Parses the name of the file based on the values of the parameters set by the user
	 * 
	 * @param name is the name of the TC which should follow the format ###-XXTC-namehere.fileextension
	 * @return is the modified name to fit the new format ###-XXTR-namehere.fileextension
	 */
	private static String parseFileName(String name) {
		
		// ps denotes Prefix String
		// ss denotes Suffix String
		// sr denotes String Replace
		// a denotes after
		// b denotes before
		
		String psb = null;
	    String psa = null;
	    String ssb = null;
	    String ssa = null;
	    String srb = null;
	    String sra = null;
	    
		try {
		    psb = tcMoverController.getPsb();	    
		    psa = tcMoverController.getPsa();  
		    ssb = tcMoverController.getSsb();      
		    ssa = tcMoverController.getSsa();  
		    srb = tcMoverController.getSrb();  
		    sra = tcMoverController.getSra();
		} catch (NullPointerException e) {e.printStackTrace();}
		
	    //
	    // Prefix String
	    //
	    
	    if ((psb != null && !psb.isEmpty()) && (psa != null && !psa.isEmpty())) {
	    	name = name.replace(psb, psa);
	    } else if ((psb == null || psb.isEmpty()) && (psa != null && !psa.isEmpty())) {
	    	name = name.replace(name.split("-")[0], psa);
	    }
	    
	    //
	    // TC --> TR
	    //
	    
	    name = name.replace(name.split("-")[1], name.split("-")[1].replace("TC", "TR") );
		
	    //
	    // Suffix String (using .txt dictionary)
	    //
	    
	    // Create relative file if it doesn't exist and add default values 
	    createDictionary();
	    
	    // Read in Suffix's from dictionary (.txt file)
	    ArrayList<String> suffixDictionary = new ArrayList<>();
	    try {
	    	BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")+ "\\dictionary.txt"));
			
			String line;
			while((line = br.readLine()) != null) {
				suffixDictionary.add(line);
		    }
			br.close();
		} catch (IOException e) {e.printStackTrace();} 
	    
	    
	    // Ensure that entered Suffix exists in dictionary
	    // and check if the file already has a suffix
	    boolean validSsbSuffix = false;
	    boolean validSsaSuffix = false;
	    
	    boolean existingSuffix = false;
	    String potentialSuffix = name.split("-")[2];
	    
	    for (String s : suffixDictionary) {
	    	if(s.equals(ssb)) {
	    		validSsbSuffix = true;
	    	}
	    	if(s.equals(ssa)) {
	    		validSsaSuffix = true;
	    	}
	    	if(s.equals(potentialSuffix)) {
	    		existingSuffix = true;
	    	}
	    }
	    
	    if(validSsbSuffix || validSsaSuffix) {
	    	if(validSsaSuffix && (ssb == null || ssb.isEmpty())) {
				if(existingSuffix) {
					name = name.replace("-"+potentialSuffix+"-", "-"+ssa+"-");
				}
				else {
		    		int pos = ordinalIndexOf(name, "-", 2);
					name = name.substring(0,pos) + "-" + ssa + name.substring(pos);	  	
	    		}
	    	}
	    	if(validSsbSuffix && validSsaSuffix) {
	    		name = name.replace("-"+ssb+"-", "-"+ssa+"-");
	    	}
	    }
	    
	    //
	    // String Replace
	    //
	    
	    if ((srb != null && !srb.isEmpty())) {
	    	name = name.replace(srb, sra);
	    }
	    
		return name;
	}
	
	/**
	 * Returns the index of the nth occurrence of substr found in str
	 * 
	 * @param str is the String to search on
	 * @param substr is the String to find in str
	 * @param n is the nth occurrence of substr in str
	 * @return
	 */
	public static int ordinalIndexOf(String str, String substr, int n) {
	    int pos = str.indexOf(substr);
	    while (--n > 0 && pos != -1)
	        pos = str.indexOf(substr, pos + 1);
	    return pos;
	}
	
	/**
	 * Appends a string of characters to a file
	 * 
	 * @param file is the file to append to	
	 * @param string is the string to append to the file
	 */
	public static void appendToFile(File file, String string) {
		try {
		    Files.write(Paths.get(file.getPath()), string.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {e.printStackTrace();}
		
	}

	/**
	 * Creates a dictionary relative to where the program is being executed if does not already exist
	 */
	public static void createDictionary() {
		File dictionary = new File(System.getProperty("user.dir")+"//dictionary.txt");
	    if(!dictionary.exists()) {
	    	try{
	    		dictionary.createNewFile();
	    		appendToFile(dictionary, "SIT\r\nUAT\r\n");
	    	} catch (IOException e) {e.printStackTrace();}
	    }
	}
}
