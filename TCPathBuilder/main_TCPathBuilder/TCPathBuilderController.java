package main_TCPathBuilder;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class TCPathBuilderController {
	
	    @FXML
	    private Button executeButton;
	    
	    @FXML
	    private Button instructionsButton;

	    @FXML
	    private TextField outputPathTextField;
	    
	    @FXML
	    private TextField folderPathTextField;
	    
	    /**
	     * 
	     * Components not in use
	     */
	    
	    @FXML
	    private AnchorPane anchorPane;
	    
	    @FXML
	    private Label welcomeLabel;
	    
	    @FXML
	    private Label tcLabel;
	    
	    @FXML
	    private Label outputLabel;

	   
	    
	    @FXML
	    void execute() {
	    	
	    	TCPathBuilder.go(folderPathTextField.getText(), outputPathTextField.getText());
	    }
	    
	    @FXML
	    void displayInstructions() {
	        try{
	            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TCPathBuilder_Instructions.fxml"));
	            Parent root = (Parent) fxmlLoader.load();
	            Stage stage = new Stage();
	            stage.setTitle("Instructions");
	            stage.setScene(new Scene(root)); 
	            TCPathBuilder.setInstructionsStage(stage);
	            stage.show();
	          }
	        catch(Exception e) {
	        	e.printStackTrace();
	        }
	    }
}
