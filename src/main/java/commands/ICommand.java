package commands;

import BTMaker.BTMaker.Controller;

public interface ICommand {
    void execute(Controller controller);
    void undo(Controller controller);
}
