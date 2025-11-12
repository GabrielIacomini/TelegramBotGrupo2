package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.clients.AgregadorProxy;
import com.example.TelegramBotGrupo2.dtos.BusquedaReqDTO;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.example.TelegramBotGrupo2.service.utils.PaginatorFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

        String[] partes = text.split("\\s+", 2);
        if (partes.length < 2) {
            bot.enviarMensaje(chatId, "⚠️ Debes indicar al menos una palabra clave. Ejemplo:\n/buscarhechos incendio tag:CABA");
            return;
        }

        String query = partes[1].replace(",", " ").trim();
        String[] tokens = query.split("\\s+");

        List<String> keywords = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        for (String token : tokens) {
            if (token.toLowerCase().startsWith("tag:")) tags.add(token.substring(4).trim());
            else keywords.add(token.trim());
        }

        bot.enviarMensaje(chatId,
                "🔍 *Búsqueda recibida:*\n" +
                        "🗝️ Palabras clave: " + String.join(", ", keywords) + "\n" +
                        "🏷️ Tags: " + (tags.isEmpty() ? "ninguno" : String.join(", ", tags))
        );

        try {
            AgregadorProxy agregador = new AgregadorProxy("https://two025-tp-entrega-2-gabrieliacomini.onrender.com", mapper);
            BusquedaReqDTO req = new BusquedaReqDTO();
            req.setTerminos(keywords);
            req.setPageIdx(Optional.of(0));
            req.setPageSize(Optional.of(3));

            var resp = agregador.buscarHechosPorPalabrasClaves(req);

            new PaginatorFormatter(bot).mostrarPagina(chatId, "buscar", query, 0, resp.hechos);

        } catch (Exception e) {
            bot.enviarMensaje(chatId, "❌ Error al buscar hechos.");
        }
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
