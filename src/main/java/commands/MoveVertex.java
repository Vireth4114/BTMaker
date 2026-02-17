package commands;

import BTMaker.BTMaker.Controller;
import model.GameObject;

public class MoveVertex implements ICommand {
    public int id;
    public int vertexId;
    public short oldX;
    public short oldY;
    public short newX;
    public short newY;

    public MoveVertex(int id, int vertexId, short oldX, short oldY, short newX, short newY) {
        this.id = id;
        this.vertexId = vertexId;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void execute(Controller controller) {
        GameObject obj = Controller.level.objects.get(id);
        controller.moveVertex(obj, vertexId, newX, newY, true);
    }

    @Override
    public void undo(Controller controller) {
        GameObject obj = Controller.level.objects.get(id);
        controller.moveVertex(obj, vertexId, oldX, oldY, true);
    }
}
