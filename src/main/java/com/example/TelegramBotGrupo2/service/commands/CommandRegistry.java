package com.example.TelegramBotGrupo2.service.commands;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final Map<String, CommandAction> commands = new HashMap<>();

    public void register(CommandAction command) {
        commands.put(command.getName(), command);
    }

    public CommandAction getCommand(String name) {
        return commands.get(name);
    }
}
