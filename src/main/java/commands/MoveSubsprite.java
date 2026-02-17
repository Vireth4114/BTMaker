package commands;

import BTMaker.BTMaker.Controller;
import model.SpriteObject;

public class MoveSubsprite implements ICommand {
    public int id;
    public int subspriteId;
    public short oldX;
    public short oldY;
    public short newX;
    public short newY;

    public MoveSubsprite(int id, int subspriteId, short oldX, short oldY, short newX, short newY) {
        this.id = id;
        this.subspriteId = subspriteId;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void execute(Controller controller) {
        SpriteObject sObj = (SpriteObject) Controller.level.objects.get(id);
        sObj.trueX[subspriteId] = newX;
        sObj.trueY[subspriteId] = newY;
        sObj.xList[subspriteId] = sObj.trueX[subspriteId] - sObj.xAbs;
        sObj.yList[subspriteId] = sObj.trueY[subspriteId] - sObj.yAbs;
    }

    @Override
    public void undo(Controller controller) {
        SpriteObject sObj = (SpriteObject) Controller.level.objects.get(id);
        sObj.trueX[subspriteId] = oldX;
        sObj.trueY[subspriteId] = oldY;
        sObj.xList[subspriteId] = sObj.trueX[subspriteId] - sObj.xAbs;
        sObj.yList[subspriteId] = sObj.trueY[subspriteId] - sObj.yAbs;
    }
}
