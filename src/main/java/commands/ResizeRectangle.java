package commands;

import BTMaker.BTMaker.Controller;
import model.GameObject;
import model.RectangleObject;

public class ResizeRectangle implements ICommand {
    public int id;
    public int rectangleResizing;
    public short oldX;
    public short oldY;
    public short newX;
    public short newY;

    public ResizeRectangle(int id, int rectangleResizing, short oldX, short oldY, short newX, short newY) {
        this.id = id;
        this.rectangleResizing = rectangleResizing;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void execute(Controller controller) {
        GameObject obj = Controller.level.objects.get(id);
        controller.resizeRectangle((RectangleObject) obj, rectangleResizing, newX, newY);
    }

    @Override
    public void undo(Controller controller) {
        GameObject obj = Controller.level.objects.get(id);
        controller.resizeRectangle((RectangleObject) obj, rectangleResizing, oldX, oldY);
    }
}
