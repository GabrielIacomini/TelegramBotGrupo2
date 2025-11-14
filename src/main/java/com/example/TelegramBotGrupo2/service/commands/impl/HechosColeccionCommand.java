package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.clients.AgregadorProxy;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.example.TelegramBotGrupo2.service.utils.PaginatorFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class HechosColeccionCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;

    public HechosColeccionCommand(TelegramBoot bot, ObjectMapper mapper) {
        this.bot = bot;
        this.mapper = mapper;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String[] partes = update.getMessage().getText().split("\\s+");

        if (partes.length < 2) {
            bot.enviarMensaje(chatId, "⚠️ Debes indicar una colección. Ej: /hechoscoleccion coleccion1");
            return;
        }

        String coleccionId = partes[1];

        try {
            AgregadorProxy agregador = new AgregadorProxy("https://two025-tp-entrega-2-gabrieliacomini.onrender.com", mapper);
            List<HechoDTO> hechos = agregador.getHechos(coleccionId);

            new PaginatorFormatter(bot).mostrarPagina(chatId, "hechos", coleccionId, 0, hechos, hechos.size());
        } catch (Exception e) {
            bot.enviarMensaje(chatId, "❌ Error al obtener hechos de la colección");
            bot.enviarListaDeComandos(chatId);
        }
    }

    @Override
    public String getName() {
        return "/hechoscoleccion";
    }

    @Override
    public String getDescription() {
        return "Obtiene los hechos de una colección";
    }
}
