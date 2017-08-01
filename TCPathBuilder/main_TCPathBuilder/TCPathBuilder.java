package main_TCPathBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.TableFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**\
 * 
 * @author Nicholas Ostaffe
 * @date   07/06/2017
 *
 *  This piece of code checks a directory 
 *  for a list of word documents, opens each 
 *  of them up, and checks each for a path 
 *  to a test case (TC) based on keywords 
 *  within the word document.
 *  
 *  This code depends on Docx4j to open, 
 *  read, and modify word documents.
 *  After opening each document, names of
 *  menus and links are extracted based on
 *  a regex pattern. These names are then
 *  constructed into a path.
 *  	e.g.
 *  	menu > menu > menu
 *  	menu > link > link
 *  	menu > link > menu
 *  	link > link > link
 *  	...
 *  	etc.
 *  These paths are exported into a .txt
 *  file, associated with the original TC
 *  name, separated by a tab. Format for 
 *  the .txt appears as follows :
 *  	e.g.
 *  	TC name 1	this > is > my > path
 *  	TC_THIS_IS_TC_2		path > number > two
 *  	...
 *  	etc.
 *  No editing of the .txt file is required.
 *  Simply select all of the text within the
 *  .txt file, copy it, and paste it into
 *  your desired excel sheet.
 *  
 *  
 *  Most up-to-date version v1.2
 *  
 *
 */
public class TCPathBuilder extends Application{
	
	@SuppressWarnings("unused")
	private static Stage mainStage;
	
	@SuppressWarnings("unused")
	private static TCPathBuilderController pathBuilderController;
	
	private static Stage instructionsStage;
	
	private Scene main;	

	@Override
	/**
	 * 
	 * Configures the main UI view from TCPathBuilder_Main.fxml
	 */
	public void start(final Stage stage) throws IOException {
		TCPathBuilder.mainStage = stage;
		stage.setTitle("TCPathBuilder v1.2  -- Nicholas Ostaffe -- Source at https://github.com/supergoa");

		FXMLLoader loader = new FXMLLoader(TCPathBuilder.class.getResource("TCPathBuilder_Main.fxml"));
		
		try {
			main = new Scene(loader.load());
		} catch (IOException e) {e.printStackTrace();}
		
		TCPathBuilder.pathBuilderController = loader.getController();
		
		// When the user closes the main application, this ensure the instructions window will close as well.
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	          public void handle(WindowEvent we) {
	              if(instructionsStage != null && instructionsStage.isShowing()) {
	            	  instructionsStage.close();
	              }
	          }
	      });  
		
		stage.setScene(main);
		stage.setResizable(false);
		stage.show();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	/**
	 * 
	 * @param tcPath is the path to the folder containing test cases delivered from Oracle as specified by the user
	 * @param outputPath is the path to the output location specified by the user
	 */
	public static void go(String tcPath, String outputPath) {

		File deliveredTCPath = null;
		File[] deliveredTCs = null;
		
		BufferedWriter output = null;
		
		try {
			deliveredTCPath = new File(tcPath);
			
			// Populate with files found at tcPath location
			deliveredTCs = deliveredTCPath.listFiles();
			
			output = new BufferedWriter(new FileWriter(outputPath + "\\TCPathBuilder_Output.txt"));
			
			// Will store all the paths found in every file at the tcPath location
			ArrayList<String> allPaths = new ArrayList<String>();
			
			/*
			 * if "Personalize Homepage > FSCM Navigation Collections > Accounts Payable"
			 * is a path to find. This regex will be responsible for capturing "Personalize
			 * Homepage", "FSCM Navigation Collections", "Accounts Payable"
			 */
			final String regex = "(?<=Click the ).*?(?= link| menu)";
			
			if(deliveredTCs!=null) {
				
				// Loop every file found at the tcPathLocation
				for(File TC : deliveredTCs) {
					
					/**
					 * 
					 * 
					 * Extract data from each file
					 * 
					 * 
					 * 
					 */
					
					
					// Load each file's contents
					WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(TC);
					MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

					// Find all tables within each file (Word document tables)
					TableFinder finder = new TableFinder();
					new TraversalUtil(documentPart.getContent(), finder);

					// Stores all text found in the tables except for certain keywords/phrases.
					ArrayList<String> tableText = new ArrayList<>();
					
					// Begin iteration to reach text in table object
					// Hierarchy: Tbl -> Tr -> Tc -> P -> R -> Text
					for (Object table : finder.tblList) {

						Tbl tbl = (Tbl) XmlUtils.unwrap(table);

						for (Object tr : tbl.getContent()) {

							Tr tableRow = (Tr) XmlUtils.unwrap(tr);

							for (Object c : tableRow.getContent()) {
								
								Tc tableCell = (Tc) XmlUtils.unwrap(c);
								
								// Builds text per table cell
								// Each line will later be matched against the regex
								String line = "";
								
								for (Object p : tableCell.getContent()) {
									
									P paragraph = (P) XmlUtils.unwrap(p);
									
									for (Object r : paragraph.getContent()) {
										
										// Not all P objects are R objects and only R objects contain desired Text objects
										// Therefore, filter out all objects that are not of type R.
										R run = null;
										if(r instanceof R) {
											run = (R) XmlUtils.unwrap(r);
										}
										else {
											continue; // Ignores the object that is not of type R
										}
										
										// Reached Text objects
										for (Object t : run.getContent()) {
											Text text = null;
											try {
												text = (Text) XmlUtils.unwrap(t);
												
												// Filter out keywords known to be unnecessary
												if(
													!text.getValue().equals("Step") &&
													!text.getValue().equals("Action") &&
													!text.getValue().equals("Expected Results") &&
													!text.getValue().equals("Test Step Notes") &&
													!text.getValue().equals("Results") &&
													!text.getValue().equals("Date") &&
													!text.getValue().equals("Tester") &&
													!text.getValue().equals("Test Notes") &&
													!text.getValue().equals("Estimated Time") &&
													!text.getValue().equals("Actual Time")) {
													
													line += text.getValue();
												}
												
											} catch (java.lang.ClassCastException e) {}

										}
										
									}
									
								}
								if(
									line.trim().length() > 0 && 
									
									// Filter out phrases known to be unnecessary
									!line.contains("Add a New Value") && 
									!line.contains("Find an Existing Value")) {
									
									tableText.add(line);
								}
							}

						}
					}

					/**
					 * 
					 * 
					 * Build paths for each file from extracted data
					 * 
					 * 
					 * 
					 */
					
					String[] sentences = {};
					sentences = (String[]) tableText.toArray(sentences);
					
					final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
					Matcher matcher = null;

					// Flag denoting whether to include the fileName -- TC.getName()
					boolean includeName = true;
					// Flag denoting whether to append a ">" character
					boolean includeSymbol = false;
					
					// Temporarily builds a path before appending itself to allPaths
					String path = "";

					for(int i=0; i<sentences.length; i++) {
						matcher = pattern.matcher(sentences[i]);
						
						// Paths never continue unless "Click the" appears between two adjacent table cells
						// Therefore, reset path under this condition and temporarily turn off includeSymbol
						// to allow for format "TC_name	[tab] file > path > here" to be built
						if(!sentences[i].contains("Click the")) {
							if(path.trim().length() > 0){
								allPaths.add(path);
								path = "";
							}
							includeSymbol = false;
						}
						
						// Loop through all keyword matches found in a sentence
						while (matcher.find()) {
							
							// Avoids catching phrases like "Click the link" and "Click the menu"
							// because it would only capture a " " character.
							if(matcher.group(0).trim().length() > 0) {
								if(includeName) {
									path += TC.getName().split("_TEST.docx|_TEST.doc|_TESTRESULTS.docx|_TESTRESULTS.doc")[0] + "\t";
									includeName = false;
								}
								if(includeSymbol) {
									path += " > ";
								}
							    path += matcher.group(0);
							    includeSymbol = true;
							}
						}
					}
					if(path.trim().length() > 0){
						allPaths.add(path);
					}
				}
				
				
				/**
				 * 
				 * 
				 * Build output file
				 * 
				 * 
				 * 
				 */
				
				String finalString = "";
				String fileName = "";
				
				// Format all paths in format "TC_name	[tab] file > path > here"
				// corrects some paths that were initially built w/o their TC name
				for(String s : allPaths) {
					if(s.contains("\t")) {
						finalString += s + "\r\n";
						fileName = s.split("\t")[0];
					}
					else {
						finalString += fileName + "\t" + s + "\r\n";
					}
				}
				output.write(finalString);
				output.close();
				
				System.out.println("Done.");
			}
			else {
				System.err.println("Invalid directory or no children found.");
				System.exit(0);
			}
		}
		catch(Exception e) {
			System.err.println("Before");
			e.printStackTrace();
			System.err.println("Exception caught.");
			System.exit(0);
		}
		
	}

	/**
	 * Method called when the "here." button is clicked so this class can maintain an 
	 * updated reference to the stage.
	 * 
	 * @param s references the instruction stage that appears when clicking the "here."
	 * button from the UI
	 */
	public static void setInstructionsStage(Stage s) {
		instructionsStage = s;
	}
}
