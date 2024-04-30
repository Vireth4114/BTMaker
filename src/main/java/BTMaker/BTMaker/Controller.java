		package BTMaker.BTMaker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import model.GameObject;
import model.GeometryObject;
import model.Level;
import model.MovingCircle;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

public class Controller implements Initializable {
	@FXML public Pane pane;
	@FXML public VBox vBox;
	@FXML public VBox openVBox;
	@FXML public HBox hBox;
	@FXML public BorderPane borderPane;
	public File gameFile = null;
	public Level level = null;
	public ArrayList<Level> levels = new ArrayList<Level>(Arrays.asList(new Level[15]));
	public double size;
	public double leftOffset = 0;
	public double topOffset = 0;
	public boolean ctrlHeld = false;
	public HashMap<Shape, Short> shapeIDs = new HashMap<Shape, Short>();
	public SimpleIntegerProperty selectedID = new SimpleIntegerProperty();
	public MovingCircle target = null;
	public double mouseX = 0;
	public double mouseY = 0;
	public short objectX = 0;
	public short objectY = 0;
	public double xOffset = 0;
	public double yOffset = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		pane.widthProperty().addListener((obs, prec, nouv) -> {
			openVBox.setPrefHeight(pane.getHeight());
			openVBox.setPrefWidth(pane.getWidth());
		});
		pane.heightProperty().addListener((obs, prec, nouv) -> {
			openVBox.setPrefHeight(pane.getHeight());
			openVBox.setPrefWidth(pane.getWidth());
		});
		vBox.setAlignment(Pos.CENTER);
		vBox.setSpacing(10);
		hBox.setAlignment(Pos.CENTER);
		hBox.setSpacing(10);
		pane.setStyle("-fx-background-color: black;");
		Text text = new Text("Please open the game");
		text.setFill(Color.WHITE);
		text.setFont(new Font(50));
		openVBox.getChildren().add(text);
		Button button = new Button("Open");
		button.setPrefWidth(150);
		button.setPrefHeight(40);
		button.setFont(new Font(20));
		button.setOnAction(e -> {
			try {
				onOpen();
			} catch (IOException e1) {
			} catch (ZipException e2) {
			}
		});
		openVBox.getChildren().add(button);
		openVBox.setAlignment(Pos.CENTER);
		openVBox.setSpacing(20);
	}
	
	public void addDrawListener() {
		pane.widthProperty().addListener(e -> {
			leftOffset = 0;
			topOffset = 0;
			if ((level.xMax - level.xMin) > level.yMax-level.yMin) {
				size = pane.getWidth()/(level.xMax-level.xMin+20);
			} else {
				size = pane.getHeight()/(level.yMax-level.yMin+20);
			}
			draw();
		});
		pane.heightProperty().addListener(e -> {
			leftOffset = 0;
			topOffset = 0;
			if ((level.xMax - level.xMin) > level.yMax-level.yMin) {
				size = pane.getWidth()/(level.xMax-level.xMin+20);
			} else {
				size = pane.getHeight()/(level.yMax-level.yMin+20);
			}
			draw();
		});
		pane.setOnMousePressed(e -> {
			if (e.getTarget() instanceof Shape && !(e.getTarget() instanceof Line)) {
				mouseX = e.getX();
				mouseY = e.getY();
				if (e.getTarget() instanceof MovingCircle) {
					target = (MovingCircle) e.getTarget();
				} else {
					selectedID.set(shapeIDs.get(e.getTarget()));
					GameObject obj = level.objects.get(selectedID.get());
					objectX = obj.xPos;
					objectY = obj.yPos;
					obj.onClick(this);
				}
			} else {
				selectedID.set(-1);
			}
			draw();
		});
		pane.setOnMouseDragged(e -> {
			if (target != null) {
				GeometryObject gObj = (GeometryObject) level.objects.get(selectedID.get());
				gObj.trueX[target.id] = (int) Math.round(e.getX()/size + level.xMin - xOffset);
				gObj.trueY[target.id] = (int) Math.round(level.yMax - e.getY()/size + yOffset);
				gObj.xList[target.id] = gObj.trueX[target.id] - gObj.xAbs;
				gObj.yList[target.id] = gObj.trueY[target.id] - gObj.yAbs;
			} else if (selectedID.get() != -1) {
				GameObject obj = level.objects.get(selectedID.get());
				obj.xPos = (short) (objectX + Math.round((e.getX()-mouseX)/size));
				obj.yPos = (short) (objectY - Math.round((e.getY()-mouseY)/size));
				System.out.println(obj.xPos+" "+obj.yPos);
				for (GameObject obj1: level.objects) {
					obj1.absSet = false;
				}
				for (GameObject obj1: level.objects) {
					obj1.doAbs(level.objects);
				}
			}
			draw();
		});
		pane.setOnMouseReleased(e -> {
			target = null;
		});
	}
	
	public void draw() {
		System.out.println(size);
		shapeIDs.clear();
		xOffset = (pane.getWidth()/size - (level.xMax-level.xMin))/2 + leftOffset;
		yOffset = (pane.getHeight()/size - (level.yMax-level.yMin))/2 + topOffset;
		pane.getChildren().clear();
		addGrid(Math.pow(10, 1-Math.floor(Math.log(size)/Math.log(20))));
		ArrayList<GameObject> zObjects = new ArrayList<GameObject>(level.objects);
		zObjects.sort(new Comparator<GameObject>() {
			@Override
			public int compare(GameObject o1, GameObject o2) {
				return o2.zcoord - o1.zcoord;
			}
		});
		for (GameObject obj: zObjects) {
			for (Shape shape: obj.getShapes(this)) {
				shapeIDs.put(shape, obj.id);
				pane.getChildren().add(shape);
			}
		}
		addTargets();
		Rectangle r = new Rectangle(pane.getWidth(), 0, 200, borderPane.getHeight());
		r.setFill(Color.WHITE);
		pane.getChildren().add(r);
		Rectangle r2 = new Rectangle(0, pane.getHeight(), pane.getWidth(), 100);
		r2.setFill(Color.WHITE);
		pane.getChildren().add(r2);
	}
	
	public void addGrid(double gridSize) {				   
		for (double x = ((xOffset - level.xMin) % gridSize)*size; x < pane.getWidth(); x += gridSize*size) {
			Line l = new Line(x, 0, x, pane.getHeight());
			pane.getChildren().add(l);
		}
		for (double y = ((yOffset + level.yMax) % gridSize)*size; y < pane.getHeight(); y += gridSize*size) {
			Line l = new Line(0, y, pane.getWidth(), y);
			pane.getChildren().add(l);
		}
	}
	
	public void addTargets() {
		for (Node s: new ArrayList<Node>(pane.getChildren())) {
			if (s instanceof MovingCircle) {
				pane.getChildren().remove(s);
			}
		}
		
		if (selectedID.get() != -1) {
			if (level.objects.get((selectedID.get())) instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) level.objects.get(selectedID.get());
				for (int i = 0; i < gObj.angles; i++) {
					double x = (gObj.trueX[i] - level.xMin + xOffset)*size;
					double y = (level.yMax - gObj.trueY[i] + yOffset)*size;
					MovingCircle target = new MovingCircle(x, y, i);
					pane.getChildren().add(target);
				}
			}
		}
	}
	
	public void onKeyPress(KeyEvent evt) {
		if (level != null) {
			switch (evt.getCode()) {
				case ADD:      size = Math.min(100 , size*1.1); break;
				case SUBTRACT: size = Math.max(0.05, size*0.9); break;
				case LEFT:     leftOffset += 100/size; break;
				case RIGHT:    leftOffset -= 100/size; break;
				case UP:       topOffset += 100/size; break;
				case DOWN: 	   topOffset -= 100/size; break;
				case CONTROL:  ctrlHeld = true; break;
				case NUMPAD0:  topOffset = 0; leftOffset = 0; break;
				default: return;
			}
			draw();
		}
	}
	
	public void onKeyReleased(KeyEvent evt) {
		switch(evt.getCode()) {
			case CONTROL: ctrlHeld = false; break;
			default: break;
		}
	}
	
	public void onScroll(ScrollEvent evt) {
		if (ctrlHeld) {
			size = Math.min(100, Math.max(0.05, size*(1 + Math.signum(evt.getDeltaY())/25)));
		} else {
			leftOffset += evt.getDeltaX()*2/size;
			topOffset += evt.getDeltaY()*2/size;
		}
		draw();
	}
	
	public void onOpen() throws IOException, ZipException {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(new ExtensionFilter("Java Game", "*.jar", "*.jad"), new ExtensionFilter("All Files", "*.*"));
//		gameFile = fc.showOpenDialog(null);
		gameFile = new File("C:\\Users\\rapha\\Documents\\KEmulator\\bouncetale_tzasvtte.jar");
		ZipFile zipfile = new ZipFile(gameFile);
		zipfile.extractAll(gameFile.getParent() + "\\BTMaker_dir");
		for (char l = 'f'; l <= 't'; l++) {
			File f = new File(gameFile.getParent() + "\\BTMaker_dir\\b" + l);
			File f2 = new File(gameFile.getParent() + "\\BTMaker_dir\\c" + l);
			Files.copy(f.toPath(), f2.toPath(), StandardCopyOption.REPLACE_EXISTING);
			levels.set(l - 'f', new Level(gameFile.getParent() + "\\BTMaker_dir\\b" + l));
		}
		File f = new File(gameFile.getParent() + "\\BTMaker_dir\\a");
		File f2 = new File(gameFile.getParent() + "\\BTMaker_dir\\a2");
		Files.copy(f.toPath(), f2.toPath(), StandardCopyOption.REPLACE_EXISTING);
		vBox.getChildren().clear();
		for (int i = 0; i < 15; i++) {
			Button button = new Button();
			if (i < 12) {
				button.setText("Chapter " + (i + 1));
			} else {
				button.setText("Bonus Chapter " + (i - 11));
			}
			button.setPrefHeight(30);
			button.setPrefWidth(150);
			button.setOnAction(e -> {
				if (pane.getOnMouseClicked() == null) {
					addDrawListener();
				}
				if (button.getText().length() < 12) {
					int num = Integer.parseInt(button.getText().substring(8));
					if (num <= 4) {
						pane.setStyle("-fx-background-color: lightblue;");
					} else if (num <= 8) {
						pane.setStyle("-fx-background-color: aqua;");
					} else {
						pane.setStyle("-fx-background-color: #000040;");
					}
					level = levels.get(num-1);
				} else {
					pane.setStyle("-fx-background-color: darkorange;");
					level = levels.get(Integer.parseInt(button.getText().substring(14))+11);
				}
				if ((level.xMax - level.xMin) > level.yMax-level.yMin) {
					size = pane.getWidth()/(level.xMax-level.xMin+20);
				} else {
					size = pane.getHeight()/(level.yMax-level.yMin+20);
				}
				draw();
				
			});
			vBox.getChildren().add(button);
			level = levels.get(0);
			pane.setStyle("-fx-background-color: lightblue;");
			if ((level.xMax - level.xMin) > level.yMax-level.yMin) {
				size = pane.getWidth()/(level.xMax-level.xMin+20);
			} else {
				size = pane.getHeight()/(level.yMax-level.yMin+20);
			}
			if (pane.getOnMouseClicked() == null) {
				addDrawListener();
			}
			draw();
		}
	}
	
	public void onSave() throws IOException, ZipException {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(new ExtensionFilter("Java Game", "*.jar", "*.jad"), new ExtensionFilter("All Files", "*.*"));
		FileInputStream fis = new FileInputStream(gameFile.getParent() + "\\BTMaker_dir\\a2");
		DataInputStream dis = new DataInputStream(fis);
		FileOutputStream fos = new FileOutputStream(gameFile.getParent() + "\\BTMaker_dir\\a");
		DataOutputStream dos = new DataOutputStream(fos);
		dos.write(dis.readNBytes(1266));
		for (char l = 'f'; l <= 't'; l++) {
			dos.writeUTF("b" + l);
			dos.writeInt(0);
			dos.writeInt(levels.get(l - 'f').writeObjects(gameFile.getParent() + "\\BTMaker_dir\\b" + l,
											 			  gameFile.getParent() + "\\BTMaker_dir\\c" + l));
			dis.skip(12);
		}
		dos.write(dis.readAllBytes());
		fos.close();
		dos.close();
		fis.close();
		dis.close();
		File f = fc.showSaveDialog(null);
		Files.copy(gameFile.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
		ZipFile zipfile = new ZipFile(f.getAbsolutePath());
		for (char l = 'f'; l <= 't'; l++) {
			zipfile.removeFile("b" + l);
			File f1 = new File(gameFile.getParent() + "\\BTMaker_dir\\b" + l);
			zipfile.addFile(f1, new ZipParameters());
		}
		zipfile.removeFile("a");
		File f1 = new File(gameFile.getParent() + "\\BTMaker_dir\\a");
		zipfile.addFile(f1, new ZipParameters());
		
		for (char l = 'f'; l <= 't'; l++) {
			File fb = new File(gameFile.getParent() + "\\BTMaker_dir\\b" + l);
			File fb2 = new File(gameFile.getParent() + "\\BTMaker_dir\\c" + l);
			Files.copy(fb.toPath(), fb2.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		File fa = new File(gameFile.getParent() + "\\BTMaker_dir\\a");
		File fa2 = new File(gameFile.getParent() + "\\BTMaker_dir\\a2");
		Files.copy(fa.toPath(), fa2.toPath(), StandardCopyOption.REPLACE_EXISTING);	
	}
}
