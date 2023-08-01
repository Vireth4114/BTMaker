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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import model.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

public class Controller implements Initializable {
	@FXML Pane pane;
	@FXML VBox vBox;
	@FXML VBox openVBox;
	private File gameFile = null;
	private Level level = null;
	private ArrayList<Level> levels = new ArrayList<Level>(Arrays.asList(new Level[15]));
	private double size;
	private double leftOffset = 0;
	private double topOffset = 0;
	private boolean ctrlHeld = false;
	private HashMap<Shape, Short> shapeIDs = new HashMap<Shape, Short>();
	private SimpleIntegerProperty selectedID = new SimpleIntegerProperty();
	private MovingCircle target = null;
	private double mouseX = 0;
	private double mouseY = 0;
	private short objectX = 0;
	private short objectY = 0;

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
		pane.setStyle("-fx-background-color: black;");
		Text text = new Text("Open the game or it won't work");
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
			if (e.getTarget() instanceof Shape) {
				mouseX = e.getX();
				mouseY = e.getY();
				if (e.getTarget() instanceof MovingCircle) {
					target = (MovingCircle) e.getTarget();
				} else {
					selectedID.set(shapeIDs.get(e.getTarget()));
					objectX = level.objects.get(selectedID.get()).xPos;
					objectY = level.objects.get(selectedID.get()).yPos; 
				}
			} else {
				selectedID.set(-1);
			}
		});
		pane.setOnMouseDragged(e -> {
			if (target != null) {
				GeometryObject gObj = (GeometryObject) level.objects.get(selectedID.get());
				double xOffset = (pane.getWidth()/size - (level.xMax-level.xMin))/2 + leftOffset;
				double yOffset = (pane.getHeight()/size - (level.yMax-level.yMin))/2 + topOffset;
				gObj.trueX[target.id] = (int) Math.floor(e.getX()/size + level.xMin - xOffset);
				gObj.trueY[target.id] = (int) Math.floor(level.yMax - e.getY()/size + yOffset);
				gObj.xList[target.id] = gObj.trueX[target.id] - gObj.xAbs;
				gObj.yList[target.id] = gObj.trueY[target.id] - gObj.yAbs;
			} else if (selectedID.get() != -1) {
				GameObject obj = level.objects.get(selectedID.get());
				obj.xPos = (short) (objectX + Math.floor((e.getX()-mouseX)/size));
				obj.yPos = (short) (objectY - Math.floor((e.getY()-mouseY)/size));
				for (GameObject obj1: level.objects) {
					obj1.absSet = false;
				}
				for (GameObject obj1: level.objects) {
					obj1.doAbs(level.objects);
					if (obj1 instanceof GeometryObject) {
						GeometryObject gObj = (GeometryObject) obj1	;
						for (int i = 0; i < gObj.angles; i++) {
							gObj.trueX[i] = gObj.xList[i] + gObj.xAbs;
							gObj.trueY[i] = gObj.yList[i] + gObj.yAbs;
						}
					}
				}
			}
			draw();
		});
		pane.setOnMouseReleased(e -> {
			target = null;
		});
		selectedID.addListener(e -> addTargets());
	}
	
	public void draw() {
		shapeIDs.clear();
		double xOffset = (pane.getWidth()/size - (level.xMax-level.xMin))/2 + leftOffset;
		double yOffset = (pane.getHeight()/size - (level.yMax-level.yMin))/2 + topOffset;
		pane.getChildren().clear();
		ArrayList<GameObject> zObjects = new ArrayList<GameObject>(level.objects);
		zObjects.sort(new Comparator<GameObject>() {
			@Override
			public int compare(GameObject o1, GameObject o2) {
				return o2.zcoord - o1.zcoord;
			}
		});
		for (GameObject obj: zObjects) {
			if (obj.type == 8) {
				double x = (obj.xAbs - level.xMin + xOffset)*size;
				double y = (level.yMax - obj.yAbs + yOffset)*size;
				Circle bounce = new Circle(x, y, 20*size);
				bounce.setFill(Color.RED);
				shapeIDs.put(bounce, obj.id);
				pane.getChildren().add(bounce);
			}
			if (obj instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) obj;
				for (int i = 0; i < gObj.polygons; i += 3) {
					double listCorners[] = new double[6];
					for (int j = 0; j < 3; j++) {
						int x = gObj.trueX[gObj.indexBuffer[i + j]];
						int y = gObj.trueY[gObj.indexBuffer[i + j]];
						listCorners[j*2] = (x - level.xMin + xOffset)*size;
						listCorners[j*2 + 1] = (level.yMax - y + yOffset)*size;
					}
					Polygon p = new Polygon(listCorners);
					p.setFill(gObj.color);
					p.setStroke(gObj.color);
					pane.getChildren().add(p);
					shapeIDs.put(p, obj.id);
				}
			}
		}
		addTargets();
		Rectangle r = new Rectangle(pane.getWidth(), 0, 200, pane.getHeight());
		r.setFill(Color.WHITE);
		pane.getChildren().add(r);
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
					double xOffset = (pane.getWidth()/size - (level.xMax-level.xMin))/2 + leftOffset;
					double yOffset = (pane.getHeight()/size - (level.yMax-level.yMin))/2 + topOffset;
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
				case ADD:
					size *= 1.1;
					draw();
					break;
				case SUBTRACT:
					size *= 0.9;
					draw();
					break;
				case LEFT:
					leftOffset += 100/size;
					draw();
					break;
				case RIGHT:
					leftOffset -= 100/size;
					draw();
					break;
				case UP:
					topOffset += 100/size;
					draw();
					break;
				case DOWN:
					topOffset -= 100/size;
					draw();
					break;
				case CONTROL:
					ctrlHeld = true;
					break;
				case NUMPAD0:
					topOffset = 0;
					leftOffset = 0;
					draw();
					break;
				default:
					break;
			}
		}
	}
	
	public void onKeyReleased(KeyEvent evt) {
		switch(evt.getCode()) {
			case CONTROL:
				ctrlHeld = false;
				break;
			default:
				break;
		}
	}
	
	public void onScroll(ScrollEvent evt) {
		if (ctrlHeld) {
			size *= 1 + Math.signum(evt.getDeltaY())/25;
		} else {
			leftOffset += evt.getDeltaX()*2/size;
			topOffset += evt.getDeltaY()*2/size;
		}
		draw();
	}
	
	public void onOpen() throws IOException, ZipException {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(new ExtensionFilter("Java Game", "*.jar", "*.jad"), new ExtensionFilter("All Files", "*.*"));
		gameFile = fc.showOpenDialog(null);
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
