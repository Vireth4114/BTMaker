package commands;

import BTMaker.BTMaker.Controller;
import model.GameObject;

public class ChangeChapter implements ICommand{
    public int oldChapterID;
    public int newChapterID;

    public ChangeChapter(int oldChapterID, int newChapterID) {
        this.oldChapterID = oldChapterID;
        this.newChapterID = newChapterID;
    }

    @Override
    public void execute(Controller controller) {
        controller.clearSelection();
        controller.setBackgroundColor(newChapterID);
        Controller.level = controller.levels.get(newChapterID);
        controller.resetLayoutSize();
    }

    @Override
    public void undo(Controller controller) {
        controller.clearSelection();
        controller.setBackgroundColor(oldChapterID);
        Controller.level = controller.levels.get(oldChapterID);
        controller.resetLayoutSize();
    }
}
