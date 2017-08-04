package main_QAMover;

import java.io.IOException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.xlsx4j.exceptions.Xlsx4jException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class QAMoverController {
	
    @FXML
    private Button executeButton;
    
    @FXML
    private Button instructionsButton;

    @FXML
    private TextField outputPathTextField;
    
    @FXML
    private TextField folderPathTextField;
    
	@FXML
    void execute() throws IOException, Xlsx4jException, Docx4JException {
		boolean success = QAMover.go(folderPathTextField.getText(), outputPathTextField.getText());
		if(success) {
			QAMover.loadFXML("QAMover_DoneSuccess.fxml", "Complete!");
		}
    }
	
    @FXML
    void displayInstructions() {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QAMover_Instructions.fxml"));
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
}
