package com.example.TelegramBotGrupo2.service.commands;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandAction {
    void execute(Update update);
    String getName();
    String getDescription();
}
