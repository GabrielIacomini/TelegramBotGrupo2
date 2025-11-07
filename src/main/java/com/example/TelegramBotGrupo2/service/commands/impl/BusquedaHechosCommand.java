package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class BusquedaHechosCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;

    public BusquedaHechosCommand(TelegramBoot bot, ObjectMapper mapper) {
        this.bot = bot;
        this.mapper = mapper;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        // Quitamos el comando "/buscarhechos"
        String[] partes = text.split("\\s+", 2);
        if (partes.length < 2) {
            bot.enviarMensaje(chatId, "⚠️ Debes indicar al menos una palabra clave. Ejemplo:\n/buscarhechos incendio tag:CABA");
            return;
        }

        String query = partes[1].replace(",", " ").trim(); // reemplaza comas por espacios
        String[] tokens = query.split("\\s+");

        List<String> keywords = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        for (String token : tokens) {
            token = token.trim();
            if (token.toLowerCase().startsWith("tag:")) {
                String tagValue = token.substring(4).trim();
                if (!tagValue.isEmpty()) tags.add(tagValue);
            } else if (!token.isEmpty()) {
                keywords.add(token);
            }
        }

        // Mostramos resultado separado por comas
        bot.enviarMensaje(chatId,
                "🔍 *Búsqueda recibida:*\n" +
                        "🗝️ Palabras clave: " + String.join(", ", keywords) + "\n" +
                        "🏷️ Tags: " + (tags.isEmpty() ? "ninguno" : String.join(", ", tags))
        );
    }

    @Override
    public String getName() {
        return "/buscarhechos";
    }

    @Override
    public String getDescription() {
        return "Buscar hechos por palabras clave y tags";
    }
}
