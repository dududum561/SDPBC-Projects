package main_TCMover;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TCMoverController {
	
	    @FXML
	    private Button executeButton;
	    
	    @FXML
	    private Button instructionsButton;

	    @FXML
	    private TextField outputPathTextField;
	    
	    @FXML
	    private TextField folderPathTextField;
	    
	    @FXML
	    private CheckBox replaceFiles;
	    
	    @FXML
	    private TextField psb;
	    
		@FXML
	    private TextField psa;
	    
	    @FXML
	    private TextField ssb;
	    
	    @FXML
	    private TextField ssa;
	    
	    @FXML
	    private TextField srb;
	    
	    @FXML
	    private TextField sra;
	    
	    @FXML
	    private TextField suffixText;
	    		
	    
	    public String getPsb() {
			return psb.getText();
		}

		public String getPsa() {
			return psa.getText();
		}

		public String getSsb() {
			return ssb.getText();
		}

		public String getSsa() {
			return ssa.getText();
		}

		public String getSrb() {
			return srb.getText();
		}

		public String getSra() {
			return sra.getText();
		}
		
		@FXML
	    void execute() throws IOException {
			System.out.println(replaceFiles.isSelected());
			boolean success = TCMover.go(folderPathTextField.getText(), outputPathTextField.getText(), replaceFiles.isSelected());
			if(success) {
				TCMover.loadFXML("TCMover_DoneSuccess.fxml", "Complete!");
			}
	    }
	    
	    @FXML
	    void displayInstructions() {
	        try{
	            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TCMover_Instructions.fxml"));
	            Parent root = (Parent) fxmlLoader.load();
	            Stage stage = new Stage();
	            stage.setTitle("Instructions");
	            stage.setScene(new Scene(root));
	            stage.show();
	          }
	        catch(Exception e) {
	        	e.printStackTrace();
	        }
	    }
	    
	    @FXML
	    void addSuffixToDictionary() {
	    	TCMover.createDictionary();
	    	String suffix = suffixText.getText();
	    	
	    	if(suffix != null && !suffix.isEmpty()) {
	    		suffix += "\r\n";
	    		try {
	    			Files.write(Paths.get(System.getProperty("user.dir")+"//dictionary.txt"), suffix.getBytes(), StandardOpenOption.APPEND);
	    			suffixText.setText("");
	    			TCMover.loadFXML("TCMover_YesAdd.fxml", "Suffix Added!");
	    		} catch (Exception e) {e.printStackTrace();}
	    	}
	    	else {
	    		TCMover.loadFXML("TCMover_NoAdd.fxml", "No Suffix Added");
	    	}
	    }

	    @FXML
	    void clearDictionary() {
	    	TCMover.createDictionary();
	    	PrintWriter writer = null;
	    	try {
				writer = new PrintWriter(new File(System.getProperty("user.dir")+"//dictionary.txt"));
			} catch (FileNotFoundException e) {e.printStackTrace();}
	    	finally {writer.close();}
	    	
	    	TCMover.appendToFile(new File(System.getProperty("user.dir")+"//dictionary.txt"), "SIT\r\nUAT\r\n");
	    	TCMover.loadFXML("TCMover_ClearDictionary.fxml", "Dictionary Cleared!");
	    }	    
}
