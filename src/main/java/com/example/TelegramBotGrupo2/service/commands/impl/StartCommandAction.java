package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StartCommandAction implements CommandAction {
    private final TelegramBoot bot;

    public StartCommandAction(TelegramBoot bot) {
        this.bot = bot;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        bot.enviarListaDeComandos(chatId);
    }

    @Override
    public String getName() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Muestra la lista de comandos disponibles";
    }
}
