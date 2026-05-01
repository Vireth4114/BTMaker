package commands;

import btmaker.Controller;

public interface ICommand {
    void execute(Controller controller);
    void undo(Controller controller);
}
