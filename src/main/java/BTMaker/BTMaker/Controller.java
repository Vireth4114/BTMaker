package BTMaker.BTMaker;

import java.io.ByteArrayInputStream;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import model.CannonObject;
import model.EnemyObject;
import model.EventObject;
import model.FieldEvent;
import model.FieldObject;
import model.GameObject;
import model.GeometryObject;
import model.ImageMap;
import model.Level;
import model.MovingCircle;
import model.RectangleObject;
import model.Resource;
import model.SpriteObject;
import model.SubSprite;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

public class Controller implements Initializable {
	@FXML public Pane pane;
	@FXML public VBox vBox;
	@FXML public VBox openVBox;
	@FXML public VBox dragBox;
	@FXML public HBox hBox;
	@FXML public BorderPane borderPane;
	@FXML public CheckMenuItem gridCheck;
	@FXML public CheckMenuItem invisibleCheck;
	@FXML public MenuItem save;
	@FXML public SplitPane innerSplit;
	@FXML public SplitPane outerSplit;
	public File gameFile = null;
	public Level level = null;
	public ArrayList<Level> levels = new ArrayList<Level>(Arrays.asList(new Level[15]));
	public double size;
	public double leftOffset = 0;
	public double topOffset = 0;
	public boolean ctrlHeld = false;
	public HashMap<Node, Short> shapeIDs = new HashMap<Node, Short>();
	public SimpleIntegerProperty selectedID = new SimpleIntegerProperty();
	public SimpleIntegerProperty subSelected = new SimpleIntegerProperty();
	public MovingCircle target = null;
	public double mouseX = 0;
	public double mouseY = 0;
	public short objectX = 0;
	public short objectY = 0;
	public double xOffset = 0;
	public double yOffset = 0;
	public boolean grid = true;
	public boolean drawInvisible = false;
	public int rectangleScaling = 0;
	public ObjectProperty<Cursor> cursor = new SimpleObjectProperty<>(Cursor.DEFAULT);
	public List<Double> xZones = new ArrayList<Double>();
	public int test = 0;
	public boolean drawing = true;
	public static Controller instance; 

	public static HashMap<Short, Resource> rscMap = new HashMap<Short, Resource>();
	public static HashMap<Resource, List<Resource>> rscBatch = new HashMap<Resource, List<Resource>>();
	public HashMap<Short, ImageMap> imageMap = new HashMap<Short, ImageMap>();
	public HashMap<Short, List<SubSprite>> compounds = new HashMap<Short, List<SubSprite>>();
	public HashMap<Short, List<Short>> animated = new HashMap<Short, List<Short>>();
	public HashMap<Short, Double> heartBeat = new HashMap<Short, Double>();
	public SpriteAnimation spriteAnimation;
	public ArrayList<GeometryObject> cannonShapes = new ArrayList<GeometryObject>();
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		instance = this;
		spriteAnimation = new SpriteAnimation(this);
		spriteAnimation.start();
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
		vBox.setPadding(new Insets(10));
		dragBox.setAlignment(Pos.CENTER);
		dragBox.setSpacing(10);
		dragBox.setPadding(new Insets(10));
		hBox.setAlignment(Pos.CENTER);
		hBox.setSpacing(30);
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
		gridCheck.setSelected(true);
		gridCheck.selectedProperty().addListener((obs, prevV, newV) -> {
			grid = newV;
			draw();
		});
		invisibleCheck.setSelected(false);
		invisibleCheck.selectedProperty().addListener((obs, prevV, newV) -> {
			drawInvisible = newV;
			draw();
		});
		selectedID.set(-1);
		subSelected.set(-1);
	}
	
	public void resetPane() {
		if (gameFile == null) return;
		leftOffset = 0;
		topOffset = 0;
		if ((level.xMax - level.xMin) > level.yMax-level.yMin) {
			size = pane.getWidth()/(level.xMax-level.xMin+20);
		} else {
			size = pane.getHeight()/(level.yMax-level.yMin+20);
		}
		draw();
	}
	
	public void addDrawListener() {

		pane.setOnScroll(evt -> onScroll(evt));
		pane.setOnMouseMoved(e -> {
			cursor.set(Cursor.DEFAULT);
			if (e.getTarget() instanceof Rectangle) {
				boolean north = false, south = false, east = false, west = false;
				Rectangle r = (Rectangle) e.getTarget();
				if (Math.abs(e.getY() - r.getY()) < 5)                    north = true;
				if (Math.abs(e.getX() - (r.getX() + r.getWidth())) < 5)   east  = true;
				if (Math.abs(e.getY() - (r.getY() + r.getHeight())) < 5)  south = true;
				if (Math.abs(e.getX() - r.getX()) < 5) 					  west  = true;
				if (north) {
					if (east) cursor.set(Cursor.NE_RESIZE);
					else if (west) cursor.set(Cursor.NW_RESIZE);
					else cursor.set(Cursor.N_RESIZE);
				} else if (south) {
					if (east) cursor.set(Cursor.SE_RESIZE);
					else if (west) cursor.set(Cursor.SW_RESIZE);
					else cursor.set(Cursor.S_RESIZE);
				} else if (west || east) cursor.set(Cursor.W_RESIZE);
			}
		});
		pane.setOnMousePressed(e -> {
			subSelected.set(-1);
			mouseX = e.getX();
			mouseY = e.getY();
			if ((e.getTarget() instanceof Shape || e.getTarget() instanceof ImageView) && !(e.getTarget() instanceof Line)) {
				if (e.getTarget() instanceof MovingCircle) {
					target = (MovingCircle) e.getTarget();
				} else {
					drawing = false;
					selectedID.set(shapeIDs.get(e.getTarget()));
					drawing = true;
					GameObject obj = level.objects.get(selectedID.get());
					if (e.getButton().equals(MouseButton.SECONDARY) && obj instanceof SpriteObject) {
						SpriteObject sObj = (SpriteObject)obj;
						int id = 0;
						for (Group sprite: sObj.sprites) {
							if (sprite.getChildren().contains(e.getTarget())) break;
							id++;
						}
						objectX = (short) sObj.trueX[id];
						objectY = (short) sObj.trueY[id];
						subSelected.set(id);
					} else {
						objectX = obj.xPos;
						objectY = obj.yPos;
						if (obj instanceof RectangleObject) {
							RectangleObject wObj = (RectangleObject) obj;
							Rectangle r = (Rectangle) e.getTarget();
							if (Math.abs(mouseY - r.getY()) < 5) {
								rectangleScaling |= 0x1;
								objectY = wObj.minY;
							}
							if (Math.abs(mouseX - (r.getX() + r.getWidth())) < 5) {
								rectangleScaling |= 0x2;
								objectX = wObj.maxX;
							}
							if (Math.abs(mouseY - (r.getY() + r.getHeight())) < 5) {
								rectangleScaling |= 0x4;
								objectY = wObj.maxY;
							}
							if (Math.abs(mouseX - r.getX()) < 5) {
								rectangleScaling |= 0x8;
								objectX = wObj.minX;
							}	
						}
					}
				}
			} else {
				selectedID.set(-1);
			}
		});
		selectedID.addListener((obs, prevV, newV) -> {
			hBox.getChildren().clear();
			if ((int)newV < 0) return;
			hBox.getChildren().add(new Label(String.valueOf((int)newV)));
			level.objects.get((int)newV).onClick(this);
			if (drawing) {
				draw();
			}
		});
		subSelected.addListener((obs, prevV, newV) -> {
			if ((int)newV < 0 || selectedID.get() < 0) return;
			hBox.getChildren().clear();
			level.objects.get(selectedID.get()).onClick(this);
		});
		pane.setOnMouseDragged(e -> {
			if (target != null) {
				if (target.object instanceof GeometryObject) {
					GeometryObject gObj = (GeometryObject) target.object;
					gObj.trueX[target.id] = (int) Math.round(e.getX()/size + level.xMin - xOffset);
					gObj.trueY[target.id] = (int) Math.round(level.yMax - e.getY()/size + yOffset);
					gObj.xList[target.id] = gObj.trueX[target.id] - gObj.xAbs;
					gObj.yList[target.id] = gObj.trueY[target.id] - gObj.yAbs;
				} else if (target.object instanceof EnemyObject) {
					EnemyObject eObj = (EnemyObject) target.object;
					int xPos = (int) Math.round(e.getX()/size + level.xMin - xOffset) - eObj.xAbs;
					int yPos = (int) Math.round(level.yMax - e.getY()/size + yOffset) - eObj.yAbs;
					switch (target.id) {
						case 0: eObj.path.setStartX(xPos); eObj.path.setStartY(yPos); break;
						case 1: eObj.path.setEndX(xPos); eObj.path.setEndY(yPos); break;
					}
				}
			} else if (selectedID.get() != -1) {
				GameObject obj = level.objects.get(selectedID.get());
				if (rectangleScaling != 0) {
					RectangleObject wObj = (RectangleObject) obj;
					if ((rectangleScaling & 0x1) != 0) {						
						wObj.minY = (short) (objectY - Math.round((e.getY()-mouseY)/size));
					}
					if ((rectangleScaling & 0x2) != 0) {						
						wObj.maxX = (short) (objectX + Math.round((e.getX()-mouseX)/size));
					}
					if ((rectangleScaling & 0x4) != 0) {						
						wObj.maxY = (short) (objectY - Math.round((e.getY()-mouseY)/size));
					}
					if ((rectangleScaling & 0x8) != 0) {						
						wObj.minX = (short) (objectX + Math.round((e.getX()-mouseX)/size));
					}
				} else {
					if (subSelected.get() == -1) {
						obj.xPos = (short) (objectX + Math.round((e.getX()-mouseX)/size));
						obj.yPos = (short) (objectY - Math.round((e.getY()-mouseY)/size));
						for (GameObject obj1: level.objects) {
							obj1.absSet = false;
						}
						for (GameObject obj1: level.objects) {
							obj1.doAbs(level.objects);
						}
					} else {
						SpriteObject sObj = (SpriteObject) obj;
						sObj.trueX[subSelected.get()] = (short) (objectX + Math.round((e.getX()-mouseX)/size));
						sObj.trueY[subSelected.get()] = (short) (objectY - Math.round((e.getY()-mouseY)/size));
						sObj.xList[subSelected.get()] = sObj.trueX[subSelected.get()] - sObj.xAbs;
						sObj.yList[subSelected.get()] = sObj.trueY[subSelected.get()] - sObj.yAbs;
					}
				}
			} else {
				
				leftOffset += (e.getX()-mouseX)/size;
				topOffset += (e.getY()-mouseY)/size;
				mouseX = e.getX();
				mouseY = e.getY();
			}
			draw();
		});
		pane.setOnMouseReleased(e -> {
			target = null;
			rectangleScaling = 0;
		});
	}
	
	public void draw() {
		shapeIDs.clear();
		xOffset = (pane.getWidth()/size - (level.xMax-level.xMin))/2 + leftOffset;
		yOffset = (pane.getHeight()/size - (level.yMax-level.yMin))/2 + topOffset;
		pane.getChildren().clear();
		List<GameObject> zObjects = new ArrayList<GameObject>(level.objects);
		if (!drawInvisible)
			zObjects = zObjects.stream().filter(obj -> obj.noDraw == 0).collect(Collectors.toList());
		zObjects.sort(new Comparator<GameObject>() {
			@Override
			public int compare(GameObject o1, GameObject o2) {
				return o2.zcoord - o1.zcoord;
			}
		});
		if (grid)
			addGrid(Math.pow(10, 1-Math.floor(Math.log(size)/Math.log(20))), 1);
		for (GameObject obj: zObjects) {
			for (Node shape: obj.getShapes(this)) {
				shape.setScaleX(obj.xScale);
				shape.setScaleY(obj.yScale);
				registerShape(shape, obj.id);
				pane.getChildren().add(shape);
			}
		}
		addTargets();
	}
	
	public short unregisterShape(Node shape) {
		if (shape instanceof Parent) {
			for (Node child: ((Parent)shape).getChildrenUnmodifiable())
				unregisterShape(child);
		}
		if (!shapeIDs.containsKey(shape)) return -1;
		return shapeIDs.remove(shape);
	}
	
	public void registerShape(Node shape, short id) {
		if (shape instanceof Parent) {
			for (Node child: ((Parent)shape).getChildrenUnmodifiable())
				registerShape(child, id);
		}
		shapeIDs.put(shape, id);
	}
	
	public void addGrid(double gridSize, double strokeWidth) {
		for (double x = transX(0) % (gridSize*size); x < borderPane.getWidth(); x += gridSize*size) {
			Line l = new Line(x, 0, x, borderPane.getHeight());
			l.setStrokeWidth(strokeWidth);
			pane.getChildren().add(l);
		}
		for (double y = transY(0) % (gridSize*size); y < borderPane.getHeight(); y += gridSize*size) {
			Line l = new Line(0, y, borderPane.getWidth(), y);
			l.setStrokeWidth(strokeWidth);
			pane.getChildren().add(l);
		}
		if (gridSize < 1000)
			addGrid(gridSize*10, strokeWidth+1);
		else {
			double x = transX(0);
			Line l = new Line(x, 0, x, borderPane.getHeight());
			l.setStrokeWidth(strokeWidth+1);
			pane.getChildren().add(l);
			double y = transY(0);
			Line l2 = new Line(0, y, borderPane.getWidth(), y);
			l2.setStrokeWidth(strokeWidth+1);
			pane.getChildren().add(l2);
		}
	}
	
	public boolean addTargets() {
		for (Node n: new ArrayList<Node>(pane.getChildren())) {
			if (n instanceof MovingCircle && ((MovingCircle)n).object instanceof GeometryObject) {
				pane.getChildren().remove(n);
			} else if (n instanceof Shape) {
				Shape s = (Shape)n;
				if (s.getStrokeWidth() == 5 && (s.getStroke() == Color.WHITE || s.getStroke() == Color.BLUE)) {
					pane.getChildren().remove(n);
				}
			}
		}
		
		boolean value = false;
		if (selectedID.get() != -1) {
			if (level.objects.get((selectedID.get())) instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) level.objects.get(selectedID.get());
				Polygon pongon = ((Polygon)((Group)gObj.getShapes(this).get(0)).getChildren().get(0));
				for (int i = 0; i < gObj.angles; i++) {
					double startX = transX(gObj.trueX[i]);
					double startY = transY(gObj.trueY[i]);
					double endX = transX(gObj.trueX[(i+1) % gObj.angles]);
					double endY = transY(gObj.trueY[(i+1) % gObj.angles]);
					Line l = new Line(startX, startY, endX, endY);
					l.setStrokeWidth(5);
					l.setStroke(Color.WHITE);
					pane.getChildren().add(l);
					double midX = (startX + endX)/2.0;
					double midY = (startY + endY)/2.0;
					double deltaX = startX - endX;
					double deltaY = startY - endY;
					double factor = 10 / Math.sqrt(deltaX*deltaX + deltaY*deltaY);
					Line l2 = new Line(midX, midY, midX - deltaY*factor, midY + deltaX*factor);
					l2.setStrokeWidth(5);
					l2.setStroke(Color.BLUE);
					pane.getChildren().add(l2);
					value |= pongon.contains(l2.getEndX(), l2.getEndY());
				}
				for (int i = 0; i < gObj.angles; i++) {
					double x = transX(gObj.trueX[i]);
					double y = transY(gObj.trueY[i]);
					MovingCircle target = new MovingCircle(x, y, i, gObj);
					pane.getChildren().add(target);
				}
			}
		}
		return value;
	}

	public double transX(double x) {return (x - level.xMin + xOffset)*size;}
	public double transY(double y) {return (level.yMax - y + yOffset)*size;}
	
	public void onKeyPress(KeyEvent evt) {
		boolean nofocus = false;
		for (Node child: hBox.getChildren()) {
			nofocus |= child.isFocused();
		}
		if (level != null && !nofocus) {
			switch (evt.getCode()) {
				case ADD:      size = Math.min(100 , size*1.1); break;
				case SUBTRACT: size = Math.max(0.05, size*0.9); break;
				case LEFT:     leftOffset += 100/size; break;
				case RIGHT:    leftOffset -= 100/size; break;
				case UP:       topOffset += 100/size; break;
				case DOWN: 	   topOffset -= 100/size; break;
				case CONTROL:  ctrlHeld = true; break;
				case NUMPAD0:  topOffset = 0; leftOffset = 0; break;
				case DELETE:   deleteObject((short) selectedID.get()); selectedID.set(-1); break;
				case R:   deleteAll(); break;
				default: return;
			}
			draw();
		}
	}
	
	public Group getImageById(short id) {
		return getImageById(id, (short)-1);
	}
	
	public Group getImageById(short id, short objID) {
		if (imageMap.keySet().size() == 0) {
			try {
				loadSprites();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ImageMap myImage = imageMap.get(id);
		Group g = new Group();
		if (myImage != null) {
			File f = new File(gameFile.getParent() + "\\BTMaker_dir\\"+imageMap.get(id).image);
			ImageView i =  new ImageView(new Image(f.toURI().toString()));
			i.setViewport(new Rectangle2D(myImage.atlasX, myImage.atlasY, myImage.width, myImage.height));
			i.setFitWidth(myImage.width);
			i.setFitHeight(myImage.height);
			double imageSize = 65536.0/43266.0*size;
			i.setScaleX(imageSize);
			i.setScaleY(imageSize);
			g.getChildren().add(i);
			g.setTranslateX(-myImage.originX * i.getScaleX() - g.getBoundsInLocal().getMinX());
			g.setTranslateY(-myImage.originY * i.getScaleY() - g.getBoundsInLocal().getMinY());
			return g;
		}
		List <SubSprite> subSprites = compounds.get(id);
		if (subSprites != null) {
			for (SubSprite spr: compounds.get(id)) {
				Group sub = getImageById((short) spr.image);
				Node smolChild = Collections.min(sub.getChildren(), new Comparator<Node>() {
					public int compare(Node o1, Node o2) {
						return (int) Math.signum(o1.getScaleX() - o2.getScaleX());
					}});
				sub.setLayoutX(spr.drawX * smolChild.getScaleX());
				sub.setLayoutY(spr.drawY * smolChild.getScaleY());
				g.getChildren().add(sub);
			}
			return g;
		}
		spriteAnimation.sprites.put(id, g);
		spriteAnimation.objSprites.put(g, objID);
		
		return g;
	}
	
	public void deleteAll() {
		GameObject bounceObj = level.objects.get(level.bounceObject);
		GameObject baseObj = level.objects.get(0);
		bounceObj.id = 1;
		bounceObj.parentID = 0;
		bounceObj.previousID = -1;
		level.countEvent = 0;
		level.bounceObject = 1;
		ArrayList<GameObject> objects = new ArrayList<GameObject>(level.objects);
		for (GameObject obj: objects) {
			if (obj != bounceObj && obj != baseObj) {
				level.objects.remove(obj);
				level.deletedObjects.put(obj.id, obj.length);
			}
		}
	}
	
	public void deleteObject(short id) {
		GameObject obj = level.objects.get(id);
		if (obj.type == 8 || id == 0) return;
		level.deletedObjects.put(obj.id, obj.length);
		for (int i = obj.id+1; i < level.objects.size(); i++) {
			GameObject obj2 = level.objects.get(i);
			obj2.id--;
			if (obj2.previousID >= obj.id) obj2.previousID--;
			if (obj2.parentID >= obj.id) obj2.parentID--;
			if (obj.type == 6 && obj2.type == 6) {
				EventObject eObj = (EventObject) obj2;
				eObj.eventId--;
			}
		}
		
		boolean changeTrigger = false;
		if (id < level.bounceObject) {
			level.bounceObject--;
			changeTrigger = true;
		}

		List<Node> nodes = hBox.getChildren();
		ArrayList<GameObject> currentObjects = new ArrayList<GameObject>(level.objects);
		for (GameObject obj2: currentObjects) {
			if (obj2 instanceof CannonObject) {
				CannonObject cObj = (CannonObject) obj2;
				cObj.playerId = level.bounceObject;
			}
			if (obj2 instanceof EventObject) {
				EventObject eObj = (EventObject) obj2;
				nodes.clear();
				eObj.onClick(this);
				List<Node> rows = ((VBox)((ScrollPane)nodes.get(nodes.size()-1)).getContent()).getChildren();
				for (int i = 0; i < rows.size(); i++) {
					Node eventLine = rows.get(i);
					if (eventLine instanceof HBox) {
						HBox eventText = (HBox) ((HBox)eventLine).getChildren().get(1);
						for (Node child: eventText.getChildren()) {
							if (child instanceof FieldObject) {
								FieldObject field = (FieldObject) child;
								short cmpId = Short.parseShort(field.getText());
								if (obj.id < cmpId) {
									field.setText(String.valueOf(cmpId-1));
									
								}
								if (obj.id == cmpId) {
//									eObj.eventList.remove(i);
//									eObj.eventData.remove(i);
								}
							}

							if (child instanceof FieldEvent && obj.type == 6) {
								EventObject eventObj = (EventObject) obj;
								FieldEvent field = (FieldEvent) child;
								short cmpId = Short.parseShort(field.getText());
								if (eventObj.eventId < cmpId) {	
									field.setText(String.valueOf(cmpId-1));
								}
								if (eventObj.eventId == cmpId) {
//									eObj.eventList.remove(i);
//									eObj.eventData.remove(i);
								}
							}
						}
					}
				}
				if (changeTrigger) {
					eObj.triggerId = level.bounceObject;
				}
			}
		}
		level.objects.remove(obj.id);
		nodes.clear();
		draw();
	}
	
	public GameObject createObject(byte type, int x, int y) {
		GameObject obj = new GameObject((short) level.objects.size(), type);
		switch (type) {
			case 4: obj = new GeometryObject((short) level.objects.size()); break;
			case 6: obj = new EventObject((short) level.objects.size()); break;
			case 11: obj = new CannonObject((short) level.objects.size()); break;
		}
		obj.length = 0;
		obj.parentID = 0;
		obj.previousID = -1;
		for (int i = level.objects.size()-1; i >= 0; i--)
			if (level.objects.get(i).parentID == 0) {obj.previousID = (short)i; break;}
		obj.transformFlags = 1;
		obj.rotation = 0;
		obj.xScale = 1;
		obj.yScale = 1;
		obj.xPos = (short) x;
		obj.yPos = (short) y;
		obj.flags = 24;
		obj.zcoord = 24;
		obj.noDraw = 0;
		if (type == 4) {
			GeometryObject gObj = (GeometryObject) obj;
			gObj.nbRead = 0;
			gObj.angles = 4;
			gObj.polygons = 6;
			gObj.color = Color.BLACK;
			gObj.xList = new int[] {-50, 50, 50, -50};
			gObj.yList = new int[] {50, 50, -50, -50};
			gObj.indexBuffer = new int[] {1, 0, 2, 3, 2, 0};
		}
		if (type == 6) {
			EventObject eObj = (EventObject) obj;
			eObj.minX = -50;
			eObj.minY = 50;
			eObj.maxX = 50;
			eObj.maxY = -50;
			eObj.state = 0;
			eObj.triggerLeave = 0;
			eObj.repeatable = 0;
			eObj.triggerId = level.bounceObject;
			eObj.eventId = (byte) level.objects.stream().filter(e -> e.type == 6).collect(Collectors.toList()).size();
		}
		if (type == 11) {
			CannonObject cObj = (CannonObject) obj;
			cObj.playerId = level.bounceObject;
			cObj.power = 80;
			cObj.length = 16;
		}
		obj.doAbs(level.objects);
		level.objects.add(obj);
		return obj;
	}
	
	public void onKeyReleased(KeyEvent evt) {
		switch(evt.getCode()) {
			case CONTROL: ctrlHeld = false; break;
			default: break;
		}
	}
	
	public void onScroll(ScrollEvent evt) {
		if (ctrlHeld) {
			size = Math.min(100, Math.max(0.05, size*(1 + Math.signum(evt.getDeltaY())/5)));
		} else {
			leftOffset += evt.getDeltaX()*2/size;
			topOffset += evt.getDeltaY()*2/size;
		}
		draw();
	}
	
	public void onOpen() throws IOException, ZipException {
		onOpen(null);
		if (gameFile == null) return;
		addLayout();
		loadSprites();
	}
	
	public void onDebugOpen() throws IOException, ZipException {
		onOpen("C:\\Users\\rapha\\Documents\\KEmulator\\BounceTales.jar");
		addLayout();
		loadSprites();
	}
	
	public void onOpen(String path) throws IOException, ZipException {
		if (path == null) {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().addAll(new ExtensionFilter("Java Game", "*.jar", "*.jad"), new ExtensionFilter("All Files", "*.*"));
			gameFile = fc.showOpenDialog(null);
		} else {
			gameFile = new File(path);
		}
		if (gameFile == null) return;
		ZipFile zipfile = new ZipFile(gameFile);
		zipfile.extractAll(gameFile.getParent() + "\\BTMaker_dir");
		for (char l = 'f'; l <= 't'; l++) {
			File f = new File(gameFile.getParent() + "\\BTMaker_dir\\b" + l);
			File f2 = new File(gameFile.getParent() + "\\BTMaker_dir\\c" + l);
			Files.copy(f.toPath(), f2.toPath(), StandardCopyOption.REPLACE_EXISTING);
			levels.set(l - 'f', new Level(gameFile.getParent() + "\\BTMaker_dir\\b" + l));
		}
		Level cannon = new Level(gameFile.getParent() + "\\BTMaker_dir\\bu");
		for (GameObject object: cannon.objects.subList(1, 4)) {
			cannonShapes.add((GeometryObject)object);
		}
		cannonShapes.sort(new Comparator<GameObject>() {
			@Override
			public int compare(GameObject o1, GameObject o2) {
				return o2.zcoord - o1.zcoord;
			}
		});
		File f = new File(gameFile.getParent() + "\\BTMaker_dir\\a");
		File f2 = new File(gameFile.getParent() + "\\BTMaker_dir\\a2");
		Files.copy(f.toPath(), f2.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void addLayout() {
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
				selectedID.set(-1);
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
		}
		if (pane.getOnMousePressed() == null) {
			addDrawListener();
		}
		dragButton("Ground", 4);
		dragButton("Event", 6);
		dragButton("Cannon", 11);
		save.setDisable(false);
		gridCheck.setDisable(false);
		invisibleCheck.setDisable(false);
		draw();
	}
	
	public void dragButton(String str, int type) {
		StackPane ground = new StackPane();
		ground.setAlignment(Pos.CENTER);
		ground.setStyle("-fx-border-color: white; -fx-border-width: 1;");
		ground.getChildren().add(new Label(str));
		ground.setPrefHeight(100);
		ground.setOnMousePressed(e -> {
			selectedID.set(-1);
			draw();
		});
		ground.setOnMouseDragged(e -> {
			Bounds b = vBox.localToScene(vBox.getBoundsInLocal());
			Bounds b2 = dragBox.localToScene(dragBox.getBoundsInLocal());
			double myX = e.getSceneX() - b.getWidth() - 0.1;
			double myY = e.getSceneY() - b.getMinY();
			short x = (short) (myX/size + level.xMin - xOffset);
			short y = (short) (level.yMax - myY/size + yOffset);
			if (selectedID.get() == -1) {
				if (!b2.contains(e.getSceneX(), e.getSceneY())) {
					selectedID.set(createObject((byte) type, x, y).id);
				}
			} else {
				GameObject obj = level.objects.get(selectedID.get());
				obj.xPos = x;
				obj.yPos = y;
				obj.absSet = false;
				obj.doAbs(level.objects);
				draw();
			}	
		});
		dragBox.getChildren().add(ground);
	}
	
	public void loadSprites() throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(gameFile.getParent()+"\\BTMaker_dir\\a"));
		short count = dis.readShort();
		for (short i = 0; i < count; i++) {
			rscMap.put(i, new Resource(dis.readUTF(), dis.readInt()));
			dis.readInt();
		}
		count = dis.readShort();
		for (short i = 0; i < count; i++) {
			byte type = dis.readByte();
			byte count2 = dis.readByte();
			Resource main = rscMap.get(dis.readShort());
			ArrayList<Resource> rscs = new ArrayList<Resource>();
			for (short j = 0; j < count2; j++) {
				rscs.add(rscMap.get(dis.readShort()));
			}
			if (type == 2) rscBatch.put(main, rscs);
		}
		dis.close();
		for (Resource b: rscBatch.keySet()) {
			dis = new DataInputStream(new FileInputStream(gameFile.getParent()+"\\BTMaker_dir\\b"));
			dis.skip(b.offset);
			dis.readByte();
			short baseImageId = dis.readShort();
			short count1 = dis.readShort();
			short count2 = dis.readShort();
			short count3 = dis.readShort();
			HashMap<Short, Short> sprites = new HashMap<Short, Short>();
			for (short i = 0; i < count1; i++) {
				sprites.put(dis.readShort(), dis.readShort());
			}
			byte[] data = dis.readNBytes(count2);
			for (short id: sprites.keySet()) {
				short offset = sprites.get(id);
				DataInputStream sprStream = new DataInputStream(new ByteArrayInputStream(data));
				sprStream.skip(offset);
				byte flags = sprStream.readByte();
				boolean is16 = (flags & 4) != 0;
				switch (flags % 4) {
					case 0:
						imageMap.put(id, new ImageMap(sprStream, b, baseImageId, is16));
						break;
					case 1:
						Function<DataInputStream, Integer> func = is16 ? ImageMap::readShort : ImageMap::readByte;
						for (int i = 0; i < 4; i++) func.apply(sprStream);
						ArrayList<SubSprite> subsprites = new ArrayList<SubSprite>();
						short subCount = sprStream.readShort();
						for (int i = 0; i < subCount; i++) {
							subsprites.add(new SubSprite(sprStream, func));
						}
						compounds.put(id, subsprites);
						break;
					case 2:
						int animCount = sprStream.readUnsignedShort();
						List<Short> images = new ArrayList<Short>();
						for (int i = 0; i < animCount; i++) {
							images.add((short) sprStream.readUnsignedShort());
						}
						animated.put(id, images);
						break;
					default:
						break;
				}
			}
			for (short i = 0; i < count3; i++) {
				imageMap.put(dis.readShort(), new ImageMap(dis, b, baseImageId));
			}
			heartBeat.put((short) 467, 50.0);
			heartBeat.put((short) 504, 150.0);
			heartBeat.put((short) 442, 250.0);
			heartBeat.put((short) 474, 150.0);
			heartBeat.put((short) 480, 150.0);
			heartBeat.put((short) 485, 150.0);
			dis.close();
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
		if (f == null) return;
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
