package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.clients.AgregadorProxy;
import com.example.TelegramBotGrupo2.dtos.ConsensoDTO;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class CambiarConsensoCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;

    public CambiarConsensoCommand(TelegramBoot bot, ObjectMapper mapper) {
        this.bot = bot;
        this.mapper = mapper;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        List<String> partes = List.of(messageText.split("\\s+"));

        String nuevoConsenso = partes.get(1);
        String nombreColeccion = partes.get(2);

        if (!nuevoConsenso.equals("TODOS") && !nuevoConsenso.equals("AL_MENOS_2") && !nuevoConsenso.equals("ESTRICTO")) {
            bot.enviarMensaje(chatId, "❌ Consenso inválido: " + nuevoConsenso);
            bot.enviarMensaje(chatId, "❗ Ingresa un consenso válido y asegurate de que esté en mayúsculas");
            return;
        }

        try {
            ConsensoDTO consensoDTO = new ConsensoDTO(nombreColeccion, nuevoConsenso);

            AgregadorProxy agregador = new AgregadorProxy("https://two025-tp-entrega-2-gabrieliacomini.onrender.com", mapper);
            agregador.cambiarConsenso(consensoDTO);

            bot.enviarMensaje(chatId, "✅ Se cambio el consenso de la coleccion "+ nombreColeccion + " a "+nuevoConsenso);
            bot.enviarListaDeComandos(chatId);

        } catch (RuntimeException e) {
            bot.enviarMensaje(chatId, "❌ Ocurrió un error critico cambiando el consenso de la coleccion");
            bot.enviarListaDeComandos(chatId);
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "/cambiarconsenso";
    }

    @Override
    public String getDescription() {
        return "Cambiar el consenso del agregador";
    }
}
