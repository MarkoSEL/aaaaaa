package proyectofinal.utp.legal.command;

import java.util.ArrayList;
import java.util.List;

public class CommandInvoker {
    private final List<Command> queue = new ArrayList<>();

    public CommandInvoker add(Command c) {
        queue.add(c);
        return this;
    }

    public void runAll() {
        for (Command c : queue) {
            try {
                c.execute();
                System.out.println("[CMD] " + c.name() + " OK");
            } catch (Exception ex) {
                System.out.println("[CMD] " + c.name() + " ERROR: " + ex.getMessage());
            }
        }
        queue.clear();
    }
}
