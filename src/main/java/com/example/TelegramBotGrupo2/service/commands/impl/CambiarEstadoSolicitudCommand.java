package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.Enums.EstadoSolicitudBorradoEnum;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.example.TelegramBotGrupo2.service.utils.MessageFormatter.stringAEstadoSolicitud;

public class CambiarEstadoSolicitudCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;

    public CambiarEstadoSolicitudCommand(TelegramBoot bot, ObjectMapper mapper) {
        this.bot = bot;
        this.mapper = mapper;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        List<String> partes = List.of(messageText.split("\\s+"));

        try {
            String solicitudId = partes.get(1);
            String nuevoEstado = partes.get(2);
            EstadoSolicitudBorradoEnum nuevoEstadoEnum = stringAEstadoSolicitud(nuevoEstado);
            if (nuevoEstadoEnum == null) {
                bot.enviarMensaje(chatId, "❌ Estado invalido: " + nuevoEstado);
                bot.enviarMensaje(chatId, "❗ Ingresa un estado válido y asegurate de que este en mayúsculas");
                return;
            }

            var solicitudes = bot.solicitudProxy(mapper);

            var solicitud = solicitudes.getSolicitud(Integer.parseInt(solicitudId));
            if (solicitud == null) {
                bot.enviarMensaje(chatId, "❌ Ocurrió un error buscando la solicitud");
                bot.enviarListaDeComandos(chatId);
                return;
            }

            solicitud.setEstado(nuevoEstadoEnum);
            var retorno = solicitudes.patchSolicitud(solicitud);

            if (retorno == null) {
                bot.enviarMensaje(chatId, "❌ Ocurrió un error cambiando el estado de la solicitud");
                bot.enviarListaDeComandos(chatId);
                return;
            }

            bot.enviarMensaje(chatId, "✅ Se cambio el estado de la solicitud "+solicitudId+ " a "+nuevoEstado);
            bot.enviarListaDeComandos(chatId);

        } catch (Exception e) {
            bot.enviarMensaje(chatId, "❌ Ocurrió un error critico cambiando el estado de la solicitud");
            bot.enviarListaDeComandos(chatId);
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "/cambiarestadosolicitud";
    }

    @Override
    public String getDescription() {
        return "Cambiar el estado de una solicitud de borrado";
    }
}
