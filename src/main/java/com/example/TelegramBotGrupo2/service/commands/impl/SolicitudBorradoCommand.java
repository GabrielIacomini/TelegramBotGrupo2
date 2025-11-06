package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.Enums.EstadoSolicitudBorradoEnum;
import com.example.TelegramBotGrupo2.clients.SolicitudProxy;
import com.example.TelegramBotGrupo2.dtos.SolicitudDto;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.example.TelegramBotGrupo2.service.utils.MessageFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class SolicitudBorradoCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;

    public SolicitudBorradoCommand(TelegramBoot bot, ObjectMapper mapper) {
        this.bot = bot;
        this.mapper = mapper;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        List<String> partes = List.of(messageText.split("\\s+"));

        if (partes.size() < 3) {
            bot.enviarMensaje(chatId,
                    "⚠️ Uso incorrecto del comando.\n\nFormato esperado:\n" +
                            "/solicitudborrado <hechoId> <estado> <descripcion>");
            bot.enviarListaDeComandos(chatId);
            return;
        }

        try {
            String hechoId = partes.get(1);
            String estadoString = partes.get(2);

            // reconstruir descripción completa (por si tiene espacios)
            String desc = messageText.substring(messageText.indexOf(' ') + 1);
            desc = desc.substring(desc.indexOf(' ') + 1);
            desc = desc.substring(desc.indexOf(' ') + 1);

            EstadoSolicitudBorradoEnum estadoEnum = MessageFormatter.stringAEstadoSolicitud(estadoString);
            if (estadoEnum == null) {
                bot.enviarMensaje(chatId, "❌ Estado inválido: " + estadoString);
                bot.enviarMensaje(chatId, "❗ Ingresa un estado válido (en mayúsculas).");
                return;
            }

            SolicitudDto nuevaSolicitud = new SolicitudDto(null, desc, estadoEnum, hechoId);

            SolicitudProxy proxy = bot.solicitudProxy(mapper);
            SolicitudDto solicitudCreada = proxy.postSolicitud(nuevaSolicitud);

            if (solicitudCreada == null) {
                bot.enviarMensaje(chatId, "❌ Ocurrió un error dando de alta la solicitud.");
                bot.enviarListaDeComandos(chatId);
            } else {
                bot.enviarMensaje(chatId, "➕ Dada de alta la solicitud:\n\n" + MessageFormatter.solicitudAString(solicitudCreada));
                bot.enviarListaDeComandos(chatId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            bot.enviarMensaje(chatId, "❌ Error procesando el comando /solicitudborrado");
            bot.enviarListaDeComandos(chatId);
        }
    }

    @Override
    public String getName() {
        return "/solicitudborrado";
    }

    @Override
    public String getDescription() {
        return "Crea una solicitud de borrado para un hecho determinado";
    }
}
