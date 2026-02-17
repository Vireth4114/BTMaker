package commands;

import BTMaker.BTMaker.Controller;
import model.GameObject;

public class MoveObject implements ICommand{
    public int id;
    public short oldX;
    public short oldY;
    public short newX;
    public short newY;

    public MoveObject(int id, short oldX, short oldY, short newX, short newY) {
        this.id = id;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void execute(Controller controller) {
        GameObject obj = Controller.level.objects.get(id);
        obj.xPos = newX;
        obj.yPos = newY;
        for (GameObject obj1 : Controller.level.objects) {
            obj1.absSet = false;
        }
        for (GameObject obj1 : Controller.level.objects) {
            obj1.doAbs(Controller.level.objects);
        }
    }

    @Override
    public void undo(Controller controller) {
        GameObject obj = Controller.level.objects.get(id);
        obj.xPos = oldX;
        obj.yPos = oldY;
        for (GameObject obj1 : Controller.level.objects) {
            obj1.absSet = false;
        }
        for (GameObject obj1 : Controller.level.objects) {
            obj1.doAbs(Controller.level.objects);
        }
    }
}
