package main_QAMover;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class QAMover extends Application {
	@SuppressWarnings("unused")
	private static Stage mainStage;

	@SuppressWarnings("unused")
	private static QAMoverController qaNameUpdaterController;

	private Scene main;

	static File[] qaLocked = new File("C:\\Users\\daltons.ADMIN\\Desktop\\QALockedMock").listFiles();
	static DecimalFormat df = new DecimalFormat("#00");

	@Override
	/**
	 * 
	 * Configures the main UI view from QANameUpdater_Main.fxml
	 */
	public void start(final Stage stage) throws IOException {
		QAMover.mainStage = stage;
		stage.setTitle("QAMover v2.1  -- Nicholas Ostaffe -- Source at https://github.com/supergoa");

		FXMLLoader loader = new FXMLLoader(QAMover.class.getResource("QAMover_Main.fxml"));

		try {
			main = new Scene(loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}

		QAMover.qaNameUpdaterController = loader.getController();

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

	public static void main(String[] args) throws IOException, Xlsx4jException, Docx4JException {
		Application.launch(args);
		// long time = System.currentTimeMillis();
		// go("C:\\Users\\daltons.ADMIN\\Desktop\\QAMoverTest\\HR3494-EvaluationsLSILoad2017WIP","C:\\Users\\daltons.ADMIN\\Desktop\\QAMoverTest\\HR3494-Teacher-Principal-AP
		// Evaluation TrackingLOCKED");
		// System.out.println("Done. " + (System.currentTimeMillis()-time)/1000 + "s");
	}

	public static boolean go(String inputPath, String outputPath) throws Xlsx4jException, Docx4JException, IOException {

		if (inputPath != null && !inputPath.isEmpty() && outputPath != null && !outputPath.isEmpty()) {

			File input = new File(inputPath);
			File output = new File(outputPath);

			if (input.isDirectory() && output.isDirectory()) {
				int versionCounter = 1;
				for (File file : output.listFiles()) {
					if (file.isDirectory()) {
						if (file.getName().substring(0, 1).equals("A") && isInteger(file.getName().substring(1, 3))) {
							int num = Integer.parseInt(file.getName().substring(1, 3));
							if (num > versionCounter) {
								versionCounter = num;
							}
						}
					}
				}

				File newVersion = new File(
						output.getAbsolutePath() + File.separator + Math.round(Math.random() * 1000) + "-tempName");
				newVersion.mkdir();
				String newVersionName = "";

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for (File file : input.listFiles()) {
					copyFileUsingStream(file, new File(newVersion.getAbsoluteFile() + File.separator + file.getName()));

					if (file.getName().contains("-") && file.getName().split("-")[0].equals("000")) {

						SpreadsheetMLPackage xlsxPkg = SpreadsheetMLPackage.load(new File(file.getAbsolutePath()));
						WorkbookPart workbookPart = xlsxPkg.getWorkbookPart();

						for (int i = 0; i < 10; i++) {
							WorksheetPart sheet = null;
							try {
								sheet = workbookPart.getWorksheet(i);
							} catch (Xlsx4jException e) {
								continue;
							}

							DataFormatter formatter = new DataFormatter();

							// Now lets print the cell content
							String[] nameContents = displayContent(sheet, formatter);
							if (nameContents != null && nameContents[0] != null && nameContents[1] != null) {
								newVersionName = newVersion.getParent() + File.separator + "A" + df.format(versionCounter+1) + "-" + nameContents[0] + "-" + nameContents[1];
								break;
							}
							if (i == 10) {
								System.out.println("Process renameDir Unsuccessful.");
							}
						}
					}
					// Maintains only one copy of 010's and 050's in the root
					if (	((file.getName().contains("-") && file.getName().split("-")[0].equals("010")) ||
							(file.getName().contains("-") && file.getName().split("-")[0].equals("050"))) &&
							!file.getName().toLowerCase().contains(".pdf")) {
						String str = file.getName().split("-")[0];
						for (File f : output.listFiles()) {
							if((f.getName().contains("-") && f.getName().split("-")[0].equals(str) )) {
								f.delete();
							}
						}
						copyFileUsingStream(file, new File(output.getAbsoluteFile() + File.separator + file.getName()));
					}
					// Adds and overwrites all 110's
					if((file.getName().contains("-") && file.getName().split("-")[0].equals("110"))) {
						copyFileUsingStream(file, new File(output.getAbsoluteFile() + File.separator + file.getName()));
					}

				}
				newVersion.renameTo(new File(newVersionName));
				return true;
			}
			else {
				loadFXML("QAMover_InvalidPath.fxml","Warning!");
			}
		}
		else {
			loadFXML("QAMover_Warning.fxml","Warning!");
		}
		return false;
	}

	/**
	 * Copies files from one location to another using streams
	 * 
	 * @param source
	 *            is the source location for the file
	 * @param dest
	 *            is the destination location of the file to be copied
	 * @param replaceEstisting
	 *            denotes whether to replace files that have the same name
	 * @throws IOException
	 */
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
	        	//deleted = source.delete();
	        	//{
	        	//	System.out.println("Deleted" + deleted + " " + source.getAbsolutePath());
	        	//}
	        }
	    }
	}

	public static boolean isInteger(String s) {
		return isInteger(s, 10);
	}

	public static boolean isInteger(String s, int radix) {
		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}

	private static String[] displayContent(WorksheetPart sheet, DataFormatter formatter) throws Docx4JException {

		Worksheet ws = sheet.getContents();
		SheetData data = ws.getSheetData();

		String bugNumber = null;
		String jobType = null;

		boolean flag1 = false;
		boolean flag2 = false;

		boolean nextBugCell = false;
		boolean nextTypeCell = false;

		for (Row r : data.getRow()) {
			// System.out.println("row " + r.getR() );

			for (Cell c : r.getC()) {

				// get the text that appears in the cell by getting the cell value and applying
				// any data formats (Date, 0.00, 1.23e9, $1.23, etc)
				String text = formatter.formatCellValue(c);
				// System.out.println(c.getR() + " contains " + text);
				if (nextBugCell && !flag1 && text != null && !text.isEmpty()) {
					bugNumber = text.replaceAll("[^0-9]", "").substring(0, 4);
					// System.out.println("bugnum:" + bugNumber);
					flag1 = true;
				}

				if (nextTypeCell && !flag2 && text != null && !text.isEmpty()) {
					jobType = text.substring(0, 1);
					// System.out.println("type:" + jobType);
					flag2 = true;
				}

				if (text.toLowerCase().contains("tracking log number")) {
					nextBugCell = true;
				}

				if (text.toLowerCase().contains("project type (")) {
					nextTypeCell = true;
				}

				if (flag1 && flag2) {
					String[] str = { bugNumber, jobType };
					System.out.println(bugNumber);
					System.out.println(jobType);
					return str;
				}
			}

		}
		return null;

	}

	public static void loadFXML(String fxml, String title) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(QAMover.class.getResource(fxml));
			Parent root = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.setTitle(title);
			stage.setScene(new Scene(root));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
