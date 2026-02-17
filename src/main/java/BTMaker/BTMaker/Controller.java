package BTMaker.BTMaker;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import commands.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import model.*;
import model.EventObject;

public class Controller implements Initializable {
	@FXML public Pane pane;
	@FXML public VBox leftPanel;
	@FXML public VBox openVBox;
	@FXML public VBox rightPanel;
	@FXML public HBox hBox;
	@FXML public BorderPane borderPane;
	@FXML public CheckMenuItem gridCheck;
	@FXML public CheckMenuItem invisibleCheck;
	@FXML public CheckMenuItem triangleCheck;
	@FXML public MenuItem save;
	@FXML public MenuItem export;
	public final int SCALE = 8;
	public HashMap<String, Image> pngs = new HashMap<>();
	public File gameFile = null;
	public static Level level = null;
	public ArrayList<Level> levels = new ArrayList<>(Arrays.asList(new Level[15]));
	public double size;
	public double leftOffset = 0;
	public double topOffset = 0;
	public boolean ctrlHeld = false;
	public HashMap<Node, Short> shapeIDs = new HashMap<>();
	public SimpleIntegerProperty selectedID = new SimpleIntegerProperty();
	public SimpleIntegerProperty subSelected = new SimpleIntegerProperty();
	public MovingCircle selectedVertex = null;
	public double paneOffsetX = 0;
	public double paneOffsetY = 0;
	public double trueMouseX = 0;
	public double trueMouseY = 0;
	public EventTarget hoveredElement = null;
	public double startX = 0;
	public double startY = 0;
	public short objectX = 0;
	public short objectY = 0;
	public double xOffset = 0;
	public double yOffset = 0;
	public boolean draggingObject = false;
	public boolean grid = true;
	public boolean drawInvisible = false;
	public boolean showTriangulation = false;
	public int rectangleResizing = 0;
	public ObjectProperty<Cursor> cursor = new SimpleObjectProperty<>(Cursor.DEFAULT);
	public boolean redraw = true;
	public static Controller instance;
	public Deque<ICommand> undoStack = new ArrayDeque<>();
	public Deque<ICommand> redoStack = new ArrayDeque<>();
	public boolean draggingPosition = false;

	public static HashMap<Short, Resource> rscMap = new HashMap<>();
	public static HashMap<Resource, List<Resource>> rscBatch = new HashMap<>();
	public static HashMap<Short, ImageMap> imageMap = new HashMap<>();
	public static HashMap<Short, List<SubSprite>> compounds = new HashMap<>();
	public static HashMap<Short, List<Short>> animated = new HashMap<>();
	public static HashMap<Short, Double> animationSpeed = new HashMap<>();
	public SpriteAnimation spriteAnimation;
	public ArrayList<GeometryObject> cannonShapes = new ArrayList<>();


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		instance = this;
		spriteAnimation = new SpriteAnimation(this);
		spriteAnimation.start();
		pane.widthProperty().addListener((obs, prev, newV) -> {
			openVBox.setPrefHeight(pane.getHeight());
			openVBox.setPrefWidth(pane.getWidth());
		});
		pane.heightProperty().addListener((obs, prev, newV) -> {
			openVBox.setPrefHeight(pane.getHeight());
			openVBox.setPrefWidth(pane.getWidth());
		});
		leftPanel.setAlignment(Pos.CENTER);
		leftPanel.setSpacing(10);
		leftPanel.setPadding(new Insets(10));
		rightPanel.setAlignment(Pos.CENTER);
		rightPanel.setSpacing(10);
		rightPanel.setPadding(new Insets(10));
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
				System.err.println("Error opening file");
				throw new RuntimeException(e1);
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
		triangleCheck.setSelected(false);
		triangleCheck.selectedProperty().addListener((obs, prevV, newV) -> {
			showTriangulation = newV;
			draw();
		});
		selectedID.set(-1);
		subSelected.set(-1);
	}

	public boolean isObjectSelected() {
		return selectedID.get() != -1;
	}

	public void clearSelection() {
		selectedID.set(-1);
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

	public void onScroll(ScrollEvent evt) {
		if (ctrlHeld) {
			size = Math.min(100, Math.max(0.05, size*(1 + Math.signum(evt.getDeltaY())/5)));
		} else {
			leftOffset += evt.getDeltaX()*2/size;
			topOffset += evt.getDeltaY()*2/size;
		}
		draw();
	}

	public void onMouseMoveLevel(MouseEvent e) {
		cursor.set(Cursor.DEFAULT);
		trueMouseX = e.getX();
		trueMouseY = e.getY();
		hoveredElement = e.getTarget();
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
	}

	public void onMousePressedLevel(MouseEvent e) {
		subSelected.set(-1);
		startX = e.getX();
		startY = e.getY();
		paneOffsetX = e.getX();
		paneOffsetY = e.getY();
		Node eventTarget = (Node) e.getTarget();
		if ((eventTarget instanceof Line) || !(eventTarget instanceof Shape || eventTarget instanceof ImageView)) {
			return;
		}
		if (eventTarget instanceof MovingCircle) {
			GameObject obj = level.objects.get(selectedID.get());
			MovingCircle vertex = (MovingCircle) eventTarget;
			if (e.getButton().equals(MouseButton.SECONDARY) && obj instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) obj;
				gObj.removeVertex(vertex.id);
				draw();
			} else {
				selectedVertex = vertex;
				objectX = (short) viewXtoLevelX(selectedVertex.getCenterX());
				objectY = (short) viewYtoLevelY(selectedVertex.getCenterY());
			}
		} else if (eventTarget instanceof PositionCircle) {
			PositionCircle positionCircle = (PositionCircle) eventTarget;
			draggingPosition = true;
			objectX = (short) viewXtoLevelX(positionCircle.getCenterX());
			objectY = (short) viewYtoLevelY(positionCircle.getCenterY());
		} else {
			redraw = false;
			selectedID.set(shapeIDs.get(eventTarget));
			redraw = true;
			GameObject obj = level.objects.get(selectedID.get());
			if (e.getButton().equals(MouseButton.SECONDARY) && obj instanceof SpriteObject) {
				SpriteObject sObj = (SpriteObject) obj;
				int id = 0;
				for (Group sprite : sObj.sprites) {
					if (sprite.getChildren().contains(eventTarget)) break;
					id++;
				}
				objectX = (short) sObj.trueX[id];
				objectY = (short) sObj.trueY[id];
				subSelected.set(id);
			} else {
				objectX = obj.xPos;
				objectY = obj.yPos;
				if (obj instanceof RectangleObject) {
                    assert eventTarget instanceof Rectangle;
                    setRectangleResizing((RectangleObject) obj, (Rectangle) eventTarget);
				} else if (rectangleResizing == 0) {
					draggingObject = true;
				}
			}
		}
	}

	public void onMouseDraggedLevel(MouseEvent e) {
		if (selectedVertex != null) {
			GameObject obj = selectedVertex.object;
			int vertexID = selectedVertex.id;
			int x = (int) viewXtoLevelX(e.getX());
			int y = (int) viewYtoLevelY(e.getY());
			moveVertex(obj, vertexID, x, y, true);
			if (obj instanceof GeometryObject)
				((GeometryObject) obj).triangulate();
		} else if (draggingPosition) {
			GameObject obj = level.objects.get(selectedID.get());
			short deltaX = (short) (viewXtoLevelX(e.getX()) - objectX);
			short deltaY = (short) (viewYtoLevelY(e.getY()) - objectY);
			obj.xPos += deltaX;
			obj.yPos += deltaY;
			obj.absSet = false;
			obj.doAbs(level.objects);
			if (obj instanceof GeometryObject) {
				GeometryObject gObj = (GeometryObject) obj;
				for (int i = 0; i < gObj.angles; i++) {
					moveVertex(obj, i, gObj.trueX[i] - deltaX, gObj.trueY[i] - deltaY, true);
				}
			}
			objectX = obj.xAbs;
			objectY = obj.yAbs;
		} else if (isObjectSelected()) {
			GameObject obj = level.objects.get(selectedID.get());
			if (draggingObject) {
				obj.xPos = (short) (objectX + Math.round((e.getX() - paneOffsetX) / size));
				obj.yPos = (short) (objectY - Math.round((e.getY() - paneOffsetY) / size));
				for (GameObject obj1 : level.objects) {
					obj1.absSet = false;
				}
				for (GameObject obj1 : level.objects) {
					obj1.doAbs(level.objects);
				}
			} else if (rectangleResizing != 0) {
				short x = (short) (objectX + Math.round((e.getX()- paneOffsetX)/size));
				short y = (short) (objectY - Math.round((e.getY()- paneOffsetY)/size));
				resizeRectangle((RectangleObject) obj, rectangleResizing, x, y);
			} else if (subSelected.get() != -1) {
				SpriteObject sObj = (SpriteObject) obj;
				sObj.trueX[subSelected.get()] = (short) (objectX + Math.round((e.getX() - paneOffsetX) / size));
				sObj.trueY[subSelected.get()] = (short) (objectY - Math.round((e.getY() - paneOffsetY) / size));
				sObj.xList[subSelected.get()] = sObj.trueX[subSelected.get()] - sObj.xAbs;
				sObj.yList[subSelected.get()] = sObj.trueY[subSelected.get()] - sObj.yAbs;
			} else {
				leftOffset += (e.getX()- paneOffsetX)/size;
				topOffset += (e.getY()- paneOffsetY)/size;
				paneOffsetX = e.getX();
				paneOffsetY = e.getY();
			}
		} else {
			leftOffset += (e.getX()- paneOffsetX)/size;
			topOffset += (e.getY()- paneOffsetY)/size;
			paneOffsetX = e.getX();
			paneOffsetY = e.getY();
		}
		draw();
	}

	public void onMouseReleasedLevel(MouseEvent e) {
		if (e.getX() == startX && e.getY() == startY && !draggingObject) {
			clearSelection();
		}
		if (draggingObject) {
			GameObject obj = level.objects.get(selectedID.get());
			addToStack(new MoveObject(selectedID.get(), objectX, objectY, obj.xPos, obj.yPos));
		}
		if (subSelected.get() != -1) {
			SpriteObject obj = (SpriteObject) level.objects.get(selectedID.get());
			addToStack(new MoveSubsprite(selectedID.get(), subSelected.get(), objectX, objectY, obj.xPos, obj.yPos));
		}
		if (selectedVertex != null) {
			addToStack(new MoveVertex(selectedID.get(), selectedVertex.id, objectX, objectY,
					(short) viewXtoLevelX(e.getX()), (short) viewYtoLevelY(e.getY())));
		}
		if (rectangleResizing != 0) {
			short x = (short) (objectX + Math.round((e.getX()- paneOffsetX)/size));
			short y = (short) (objectY - Math.round((e.getY()- paneOffsetY)/size));
			addToStack(new ResizeRectangle(selectedID.get(), rectangleResizing,
					objectX, objectY, x, y));
		}
		selectedVertex = null;
		rectangleResizing = 0;
		draggingObject = false;
		draggingPosition = false;
		draw();
	}

	public void addDrawListener() {
		pane.setOnScroll(this::onScroll);
		pane.setOnMouseMoved(this::onMouseMoveLevel);
		pane.setOnMousePressed(this::onMousePressedLevel);
		pane.setOnMouseDragged(this::onMouseDraggedLevel);
		pane.setOnMouseReleased(this::onMouseReleasedLevel);
		selectedID.addListener((obs, prevV, newV) -> {
			hBox.getChildren().clear();
			if ((int)newV < 0) return;
			hBox.getChildren().add(new Label(String.valueOf((int)newV)));
			level.objects.get((int)newV).onClick(this);
			if (redraw) {
				draw();
			}
		});
		subSelected.addListener((obs, prevV, newV) -> {
			if ((int)newV < 0 || selectedID.get() < 0) return;
			hBox.getChildren().clear();
			level.objects.get(selectedID.get()).onClick(this);
		});
	}

	public void setRectangleResizing(RectangleObject wObj, Rectangle r) {
		if (Math.abs(paneOffsetY - r.getY()) < 5) {
			rectangleResizing |= 0x1;
			objectY = wObj.minY;
		}
		if (Math.abs(paneOffsetX - (r.getX() + r.getWidth())) < 5) {
			rectangleResizing |= 0x2;
			objectX = wObj.maxX;
		}
		if (Math.abs(paneOffsetY - (r.getY() + r.getHeight())) < 5) {
			rectangleResizing |= 0x4;
			objectY = wObj.maxY;
		}
		if (Math.abs(paneOffsetX - r.getX()) < 5) {
			rectangleResizing |= 0x8;
			objectX = wObj.minX;
		}
	}

	public void resizeRectangle(RectangleObject obj, int resizing, int x, int y) {
		if ((resizing & 0x1) != 0) {
			obj.minY = (short)y;
		}
		if ((resizing & 0x2) != 0) {
			obj.maxX = (short)x;
		}
		if ((resizing & 0x4) != 0) {
			obj.maxY = (short)y;
		}
		if ((resizing & 0x8) != 0) {
			obj.minX = (short)x;
		}
	}

	public void moveVertex(GameObject obj, int vertexID, int x, int y, boolean absolute) {
		if (absolute) {
			x = (short) (x - obj.xAbs);
			y = (short) (y - obj.yAbs);
		}
		if (obj instanceof GeometryObject) {
			GeometryObject gObj = (GeometryObject) obj;
			gObj.xList[vertexID] = x;
			gObj.yList[vertexID] = y;
			gObj.trueX[vertexID] = x + gObj.xAbs;
			gObj.trueY[vertexID] = y + gObj.yAbs;
		} else if (obj instanceof EnemyObject) {
			EnemyObject eObj = (EnemyObject) obj;
			switch (vertexID) {
				case 0: eObj.path.setStartX(x); eObj.path.setStartY(y); break;
				case 1: eObj.path.setEndX(x); eObj.path.setEndY(y); break;
			}
		}
	}

	public void draw() {
		shapeIDs.clear();
		pane.getChildren().clear();
		xOffset = (pane.getWidth()/size - (level.xMax-level.xMin))/2 + leftOffset;
		yOffset = (pane.getHeight()/size - (level.yMax-level.yMin))/2 + topOffset;

		List<GameObject> zObjects = new ArrayList<>(level.objects);
		zObjects.sort((o1, o2) -> o2.zcoord - o1.zcoord);

		if (!drawInvisible) {
			zObjects = zObjects.stream().filter(obj -> obj.noDraw == 0).collect(Collectors.toList());
		}

		if (grid) {
			addGrid(Math.pow(10, 1 - Math.floor(Math.log(size) / Math.log(20))), 1);
		}

		for (GameObject obj: zObjects) {
			for (Node shape: obj.getShapes(this)) {
				shape.setScaleX(obj.xScale);
				shape.setScaleY(obj.yScale);
				registerShape(shape, obj.id);
				pane.getChildren().add(shape);
			}
		}

		if (selectedID.get() != -1) {
			GameObject obj = level.objects.get(selectedID.get());
			for (Node shape : obj.getOverlay(this)) {
				shape.setScaleX(obj.xScale);
				shape.setScaleY(obj.yScale);
				pane.getChildren().add(shape);
			}
		}
	}

	public void unregisterShape(Node shape) {
		if (shape instanceof Parent) {
			for (Node child: ((Parent)shape).getChildrenUnmodifiable())
				unregisterShape(child);
		}
		if (!shapeIDs.containsKey(shape)) return;
		shapeIDs.remove(shape);
	}

	public void registerShape(Node shape, short id) {
		if (shape instanceof Parent) {
			for (Node child: ((Parent)shape).getChildrenUnmodifiable())
				registerShape(child, id);
		}
		shapeIDs.put(shape, id);
	}

	public void addGrid(double gridSize, double strokeWidth) {
		for (double x = levelXtoViewX(0) % (gridSize*size); x < borderPane.getWidth(); x += gridSize*size) {
			Line l = new Line(x, 0, x, borderPane.getHeight());
			l.setStrokeWidth(strokeWidth);
			pane.getChildren().add(l);
		}
		for (double y = levelYtoViewY(0) % (gridSize*size); y < borderPane.getHeight(); y += gridSize*size) {
			Line l = new Line(0, y, borderPane.getWidth(), y);
			l.setStrokeWidth(strokeWidth);
			pane.getChildren().add(l);
		}
		if (gridSize < 1000)
			addGrid(gridSize*10, strokeWidth+1);
		else {
			double x = levelXtoViewX(0);
			Line l = new Line(x, 0, x, borderPane.getHeight());
			l.setStrokeWidth(strokeWidth+1);
			pane.getChildren().add(l);
			double y = levelYtoViewY(0);
			Line l2 = new Line(0, y, borderPane.getWidth(), y);
			l2.setStrokeWidth(strokeWidth+1);
			pane.getChildren().add(l2);
		}
	}

	public double levelXtoViewX(double x) {return (x - level.xMin + xOffset)*size;}
	public double levelYtoViewY(double y) {return (level.yMax - y + yOffset)*size;}
	public double viewXtoLevelX(double x) {return x/size + level.xMin - xOffset;}
	public double viewYtoLevelY(double y) {return level.yMax - y/size + yOffset;}

	public Group getImageById(short id) {
		return getImageById(id, (short)-1);
	}

	public Group getImageById(short id, short objID) {
		if (imageMap.isEmpty()) {
			try {
				loadSprites();
			} catch (IOException e) {
				System.err.println("Error loading sprites");
			}
		}
		ImageMap myImage = imageMap.get(id);
		Group g = new Group();
		if (myImage != null) {
			ImageView i = new ImageView(pngs.get(myImage.image));
			i.setViewport(new Rectangle2D(myImage.atlasX*SCALE, myImage.atlasY*SCALE, myImage.width*SCALE, myImage.height*SCALE));
			i.setFitWidth(myImage.width*SCALE);
			i.setFitHeight(myImage.height*SCALE);
			double imageSize = 65536.0/43266.0*size/SCALE;
			i.setScaleX(imageSize);
			i.setScaleY(imageSize);
			g.getChildren().add(i);
			g.setTranslateX(-myImage.originX*SCALE * i.getScaleX() - g.getBoundsInLocal().getMinX());
			g.setTranslateY(-myImage.originY*SCALE * i.getScaleY() - g.getBoundsInLocal().getMinY());
			return g;
		}
		List <SubSprite> subSprites = compounds.get(id);
		if (subSprites != null) {
			for (SubSprite spr: compounds.get(id)) {
				Group sub = getImageById((short) spr.image);
				Node smolChild = Collections.min(sub.getChildren(), (o1, o2) -> (int) (o1.getScaleX() - o2.getScaleX()));
				sub.setLayoutX(spr.drawX*SCALE * smolChild.getScaleX());
				sub.setLayoutY(spr.drawY*SCALE * smolChild.getScaleY());
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
		ArrayList<GameObject> objects = new ArrayList<>(level.objects);
		for (GameObject obj: objects) {
			if (obj != bounceObj && obj != baseObj) {
				level.objects.remove(obj);
				level.deletedObjects.put(obj.id, obj.length);
			}
		}
	}

	public void deleteObject(short id) {
		GameObject deletedObj = level.objects.get(id);
		if (deletedObj.type == 8 || id == 0) return;
		level.deletedObjects.put(deletedObj.id, deletedObj.length);
        for (int i = deletedObj.id+1; i < level.objects.size(); i++) {
            GameObject obj = level.objects.get(i);
			obj.id--;
			if (obj.previousID >= deletedObj.id) obj.previousID--;
			if (obj.parentID >= deletedObj.id) obj.parentID--;
			if (deletedObj.type == 6 && obj.type == 6) {
				EventObject eObj = (EventObject) obj;
				eObj.eventId--;
			}
		}

		if (id < level.bounceObject) {
			level.bounceObject--;
		}

		List<Node> nodes = hBox.getChildren();
		ArrayList<GameObject> currentObjects = new ArrayList<>(level.objects);
		for (GameObject obj: currentObjects) {
			if (obj instanceof CannonObject) {
				CannonObject cObj = (CannonObject) obj;
				cObj.playerId = level.bounceObject;
                continue;
			}
			if (!(obj instanceof EventObject)) continue;

            EventObject eObj = (EventObject) obj;
            nodes.clear();
            eObj.onClick(this);
			decreaseObjectEvent(nodes, deletedObj, eObj);
		}
		level.objects.remove(deletedObj.id);
		nodes.clear();
		draw();
		clearSelection();
	}

	public void decreaseObjectEvent(List<Node> nodes, GameObject deletedObj, EventObject eObj) {
		List<Node> rows = ((VBox)((ScrollPane)nodes.get(nodes.size()-1)).getContent()).getChildren();
		for (int i = rows.size() - 1; i >= 0; i--) {
			Node eventLine = rows.get(i);
			if (!(eventLine instanceof HBox)) continue;
			HBox eventText = (HBox) ((HBox)eventLine).getChildren().get(1);
			for (Node child: eventText.getChildren()) {
				if (!(child instanceof TextField)) continue;
				TextField field;
				short deletedId;
				if (child instanceof FieldObject) {
					field = (FieldObject) child;
					deletedId = deletedObj.id;
				} else if (child instanceof FieldEvent && deletedObj.type == 6) {
					field = (FieldEvent) child;
					deletedId = ((EventObject) deletedObj).eventId;
				} else {
					continue;
				}
				short cmpId = Short.parseShort(field.getText());
				if (deletedId < cmpId) {
					field.setText(String.valueOf(cmpId-1));
				}
				if (deletedId == cmpId) {
					eObj.eventList.remove(i);
					eObj.eventData.remove(i);
				}
			}
		}
		eObj.triggerId = level.bounceObject;
	}

	public GameObject createObject(byte type, int x, int y) {
		GameObject obj;
        try {
            obj = ObjectType.fromCode(type).cls.getConstructor(short.class).newInstance((short) level.objects.size());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        obj.length = 0;
		obj.parentID = 0;
		obj.previousID = -1;
		for (int i = level.objects.size()-1; i >= 0; i--) {
			if (level.objects.get(i).parentID == 0) {
				obj.previousID = (short)i; break;
			}
		}
		obj.transformFlags = 1;
		obj.rotation = 0;
		obj.xScale = 1;
		obj.yScale = 1;
		obj.xPos = (short) x;
		obj.yPos = (short) y;
		obj.flags = 24;
		obj.zcoord = 24;
		obj.noDraw = 0;
		obj.createParams();
		obj.doAbs(level.objects);
		level.objects.add(obj);
		return obj;
	}

	public void onKeyReleased(KeyEvent evt) {
        if (Objects.requireNonNull(evt.getCode()) == KeyCode.CONTROL) {
            ctrlHeld = false;
			draw();
        }
	}

	public BoundingBox getBoundsLayoutView() {
		Bounds leftBounds = leftPanel.localToScene(leftPanel.getBoundsInLocal());
		Bounds rightBounds = rightPanel.localToScene(rightPanel.getBoundsInLocal());
		return new BoundingBox(
			leftBounds.getMaxX(),
			leftBounds.getMinY(),
			rightBounds.getMinX() - leftBounds.getMaxX(),
			leftBounds.getHeight()
		);
	}

	public void onOpen() throws IOException {
		onOpen(null);
		if (gameFile == null) return;
		addLayout();
		loadSprites();
	}

	public void onDebugOpen() throws IOException {
		onOpen("C:\\Users\\rapha\\Documents\\kemnnmod\\bounce203.jar");
		addLayout();
		loadSprites();
	}

	public void onOpen(String path) throws IOException {
		if (path == null) {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().addAll(new ExtensionFilter("Java Game", "*.jar", "*.jad"), new ExtensionFilter("All Files", "*.*"));
			gameFile = fc.showOpenDialog(null);
		} else {
			gameFile = new File(path);
		}
		if (gameFile == null)
			return;

	 	URI uri = URI.create("jar:" + gameFile.toURI());
		try (FileSystem fs = FileSystems.newFileSystem(uri, Map.of())) {
			for (int levelIndex = 0; levelIndex < 15; levelIndex++) {
				Path levelPath = fs.getPath(getLevelFileName(levelIndex));
				levels.set(levelIndex, new Level(levelPath));
			}
			Level cannon = new Level(fs.getPath("/bu"));
			for (GameObject object: cannon.objects.subList(1, 4)) {
				cannonShapes.add((GeometryObject)object);
			}
			cannonShapes.sort((Comparator<GameObject>) (o1, o2) -> o2.zcoord - o1.zcoord);
		}
	}

	public String getChapterName(int chapterIndex) {
		if (chapterIndex < 12) {
			return "Chapter " + (chapterIndex + 1);
		} else {
			return "Bonus Chapter " + (chapterIndex - 11);
		}
	}

	public void setBackgroundColor(int chapterIndex) {
		String color = "darkorange";
		if (chapterIndex < 4) {
			color = "lightblue";
		} else if (chapterIndex < 8) {
			color = "aqua";
		} else if (chapterIndex < 12) {
			color = "#000040";
		}
		pane.setStyle(String.format("-fx-background-color: %s;", color));
	}

	public void resetLayoutSize() {
		if ((level.xMax - level.xMin) > level.yMax-level.yMin) {
			size = pane.getWidth()/(level.xMax-level.xMin+20);
		} else {
			size = pane.getHeight()/(level.yMax-level.yMin+20);
		}
	}

	public Button getChapterButton(int chapterIndex) {
		Button button = new Button();
		button.setText(getChapterName(chapterIndex));
		button.setPrefHeight(30);
		button.setPrefWidth(150);
		button.setOnAction((event) -> {
			clearSelection();
			setBackgroundColor(chapterIndex);
			addToStack(new ChangeChapter(levels.indexOf(level), chapterIndex));
			level = levels.get(chapterIndex);
			resetLayoutSize();
			draw();
		});
		return button;
	}

	public void addLayout() {
		leftPanel.getChildren().clear();
		for (int index = 0; index < 15; index++) {
			leftPanel.getChildren().add(getChapterButton(index));
		}
		level = levels.get(0);
		setBackgroundColor(0);
		resetLayoutSize();
		if (pane.getOnMousePressed() == null) {
			addDrawListener();
		}
		addObjectButtons();
		setMenuitemStates(true);
		draw();
	}

	public void addObjectButtons() {
		rightPanel.getChildren().clear();
		addObjectButton("Geometry", 4);
		addObjectButton("Event", 6);
	}

	public void setMenuitemStates(boolean loaded) {
		save.setDisable(!loaded);
		export.setDisable(!loaded);
		gridCheck.setDisable(!loaded);
		invisibleCheck.setDisable(!loaded);
		triangleCheck.setDisable(!loaded);
	}

	public void addObjectButton(String str, int type) {
		StackPane button = new StackPane();
		button.setAlignment(Pos.CENTER);
		button.setStyle("-fx-border-color: white; -fx-border-width: 1;");
		button.getChildren().add(new Label(str));
		button.setPrefHeight(100);
		button.setOnMousePressed(e -> {
			clearSelection();
			draw();
		});
		button.setOnMouseDragged(e -> {
			Bounds bounds = getBoundsLayoutView();
			double myX = e.getSceneX() - bounds.getMinX() - 0.1;
			double myY = e.getSceneY() - bounds.getMinY();
			short x = (short) viewXtoLevelX(myX);
			short y = (short) viewYtoLevelY(myY);
			if (selectedID.get() == -1) {
				if (bounds.contains(e.getSceneX(), e.getSceneY())) {
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
		rightPanel.getChildren().add(button);
	}

	public void loadResources() throws IOException {
		try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + gameFile.toURI()), Map.of())) {
			DataInputStream dis = new DataInputStream(Files.newInputStream(fs.getPath("/a")));
			short resourceCount = dis.readShort();
			for (short i = 0; i < resourceCount; i++) {
				rscMap.put(i, new Resource(dis.readUTF(), dis.readInt()));
				dis.readInt();
			}
			short batchCount = dis.readShort();
			for (short i = 0; i < batchCount; i++) {
				ResourceType type = ResourceType.fromCode(dis.readByte());
				byte resourceCountInBatch = dis.readByte();
				Resource main = rscMap.get(dis.readShort());
				ArrayList<Resource> resources = new ArrayList<>();
				for (short j = 0; j < resourceCountInBatch; j++) {
					resources.add(rscMap.get(dis.readShort()));
				}
				if (type == ResourceType.IMAGE) {
					rscBatch.put(main, resources);
				}
			}
			dis.close();
		}
	}

	public void loadPNGs() throws IOException {
		try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + gameFile.toURI()), Map.of())) {
			for (Resource rsc : rscMap.values()) {
				if (rsc.name.endsWith(".png")) {
					try (InputStream imageStream = Files.newInputStream(fs.getPath(rsc.name))) {
						Image originalImage = new Image(imageStream);
						Image scaledImage = new Image(
							Files.newInputStream(fs.getPath(rsc.name)),
							originalImage.getWidth() * SCALE,
							originalImage.getHeight() * SCALE,
							true,
							true
						);
						pngs.put(rsc.name, scaledImage);
					} catch (IOException e) {
						System.err.println("Error opening PNG: " + rsc.name);
						throw e;
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error opening game file for loading PNGs");
			throw e;
		}
	}

	public void loadSprites() throws IOException {
		loadResources();
		loadPNGs();
		DataInputStream dis;
		try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + gameFile.toURI()), Map.of())) {
			for (Resource batch: rscBatch.keySet()) {
				HashMap<Short, Short> extraOffsets = new HashMap<>();

				dis = new DataInputStream(Files.newInputStream(fs.getPath("/b")));
				dis.skip(batch.offset + 1);
				short baseImageId = dis.readShort();
				short spriteCount = dis.readShort();
				short extraLength = dis.readShort();
				short imageMapCount = dis.readShort();
				for (short i = 0; i < spriteCount; i++) {
					extraOffsets.put(dis.readShort(), dis.readShort());
				}
				byte[] extraData = dis.readNBytes(extraLength);
				loadExtraSprites(extraData, extraOffsets, batch, baseImageId);
				for (short i = 0; i < imageMapCount; i++) {
					imageMap.put(dis.readShort(), new ImageMap(dis, batch, baseImageId));
				}
				dis.close();
			}
		} catch (IOException e) {
			System.err.println("Error opening game file for loading sprites");
			throw e;
		}
		addAnimationSpeeds();
	}

	/*
		Taken from the game's code itself
	 */
	public void addAnimationSpeeds() {
		animationSpeed.put((short) 467, 50.0);
		animationSpeed.put((short) 504, 150.0);
		animationSpeed.put((short) 442, 250.0);
		animationSpeed.put((short) 474, 150.0);
		animationSpeed.put((short) 480, 150.0);
		animationSpeed.put((short) 485, 150.0);
	}

	public void loadExtraSprites(byte[] extraData, HashMap<Short, Short> extraOffsets, Resource imageResource, short baseImageId) throws IOException {
		for (short id: extraOffsets.keySet()) {
			short offset = extraOffsets.get(id);
			DataInputStream spriteStream = new DataInputStream(new ByteArrayInputStream(extraData));
			spriteStream.skip(offset);
			byte flags = spriteStream.readByte();
			Function<DataInputStream, Integer> readFromStream;
			if ((flags & 4) != 0) {
				readFromStream = ImageMap::readShort;
			} else {
				readFromStream = ImageMap::readByte;
			}
			SpriteType type = SpriteType.fromCode((byte) (flags % 4));
			switch (type) {
				case STATIC:
					imageMap.put(id, new ImageMap(spriteStream, imageResource, baseImageId, readFromStream));
					break;
				case COMPOUND:
					for (int i = 0; i < 4; i++) {
						readFromStream.apply(spriteStream); // unknown values
					}
					ArrayList<SubSprite> subsprites = new ArrayList<>();
					short subspriteCount = spriteStream.readShort();
					for (int i = 0; i < subspriteCount; i++) {
						subsprites.add(new SubSprite(spriteStream, readFromStream));
					}
					compounds.put(id, subsprites);
					break;
				case ANIMATED:
					int imageCount = spriteStream.readUnsignedShort();
					List<Short> images = new ArrayList<>();
					for (int i = 0; i < imageCount; i++) {
						images.add((short) spriteStream.readUnsignedShort());
					}
					animated.put(id, images);
					break;
				default:
					throw new IOException("Unknown sprite type");
			}
		}
	}

	public void onSave() throws IOException {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(new ExtensionFilter("Java Game", "*.jar", "*.jad"), new ExtensionFilter("All Files", "*.*"));
		File output = fc.showSaveDialog(null);
		if (output == null) return;
		Files.copy(gameFile.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);

		URI originalFileURI = URI.create("jar:" + gameFile.toURI());
		try (FileSystem originalFS = FileSystems.newFileSystem(originalFileURI, Map.of())) {
			URI outputFileURI = URI.create("jar:" + output.toURI());
			try (FileSystem outputFS = FileSystems.newFileSystem(outputFileURI, Map.of())) {
				InputStream is = Files.newInputStream(originalFS.getPath("/a"));
				DataInputStream dis = new DataInputStream(is);
				OutputStream os = Files.newOutputStream(outputFS.getPath("/a"));
				DataOutputStream dos = new DataOutputStream(os);
				dos.write(dis.readNBytes(1266));
				for (int index = 0; index < 15; index++) {
					dos.writeUTF(getLevelFileName(index));
					dos.writeInt(0);
					dos.writeInt(levels.get(index).writeObjects(
						outputFS.getPath(getLevelFileName(index)),
						originalFS.getPath(getLevelFileName(index))
					));
					dis.skip(12);
				}
				dos.write(dis.readAllBytes());
				os.close();
				dos.close();
				is.close();
				dis.close();
			}
		}
	}

	public String getLevelFileName(int index) {
		return "b" + (char)('f' + index);
	}

	public void onExport() throws IOException {
		File f = new File(gameFile.getParent() + "\\" + gameFile.getName().substring(0, gameFile.getName().length()-4) + ".txt");
		FileWriter writer = new FileWriter(f);
		for (int i = 0; i < levels.size(); i++) {
			writer.write("Level " + (i+1));
			writer.write("\n=====================================\n" +
						 "=====================================\n");
			ArrayList<GameObject> myObjects = levels.get(i).objects;
			for (GameObject obj: myObjects) {
				if (obj.type == 9) continue;
				writer.write(obj.getExport());
				writer.write("\n=====================================\n");
			}
			writer.write("=====================================\n" +
						 "=====================================\n");
		}
		writer.close();
	}

	public void undoAction() {
		if (undoStack.isEmpty()) return;
		ICommand cmd = undoStack.pop();
		cmd.undo(this);
		redoStack.push(cmd);
		draw();
	}

	public void redoAction() {
		if (redoStack.isEmpty()) return;
		ICommand cmd = redoStack.pop();
		cmd.execute(this);
		undoStack.push(cmd);
		draw();
	}

	public void addToStack(ICommand command) {
		redoStack.clear();
		undoStack.push(command);
	}

	public void addVertexToSelected() {
		if (!isObjectSelected()) return;
		GameObject obj = level.objects.get(selectedID.get());
		if (!(obj instanceof GeometryObject)) return;
		GeometryObject gObj = (GeometryObject) obj;

		short x = (short) viewXtoLevelX(trueMouseX);
		short y = (short) viewYtoLevelY(trueMouseY);
		int closestId = -1, secondClosestId = -1;
		double closestDistance = Double.POSITIVE_INFINITY, secondClosestDistance = Double.POSITIVE_INFINITY;
		for (int i = 0; i < gObj.angles; i++) {
			double distance = Math.hypot(gObj.trueX[i] - x, gObj.trueY[i] - y);
			if (distance < closestDistance) {
				secondClosestId = closestId;
				secondClosestDistance = closestDistance;
				closestId = i;
				closestDistance = distance;
			} else if (distance < secondClosestDistance) {
				secondClosestId = i;
				secondClosestDistance = distance;
			}
		}
		x -= gObj.xAbs;
		y -= gObj.yAbs;
		if (Math.abs(closestId - secondClosestId) == 1) {
			gObj.addVertex(Math.min(closestId, secondClosestId), x, y);
		} else if ((closestId == 0 && secondClosestId == gObj.angles - 1) ||
				   (closestId == gObj.angles - 1 && secondClosestId == 0)) {
			gObj.addVertex(gObj.angles - 1, x, y);
		} else {
			gObj.addVertex(closestId, x, y);
		}
	}

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
				case NUMPAD0:  topOffset = 0; leftOffset = 0; resetLayoutSize(); break;
				case DELETE:   deleteObject((short) selectedID.get()); break;
				case N:		   addVertexToSelected(); break;
				case R:		   deleteAll(); break;
				case Z:        if (ctrlHeld) undoAction(); break;
				case Y:        if (ctrlHeld) redoAction(); break;
				default: return;
			}
			draw();
		}
	}
}
