package com.example.TelegramBotGrupo2.service.commands.impl;


import com.example.TelegramBotGrupo2.clients.FuenteProxy;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.example.TelegramBotGrupo2.service.utils.MessageFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

public class AgregarHechoFuenteCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;

    public AgregarHechoFuenteCommand(TelegramBoot bot, ObjectMapper mapper) {
        this.bot = bot;
        this.mapper = mapper;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageTextReceived = update.getMessage().getText();

        try {
            // ⚠️ Verificamos que el usuario haya pasado datos luego del comando
            if (!messageTextReceived.contains(" ")) {
                bot.enviarMensaje(chatId, "⚠️ Debes enviar los datos del hecho. Ejemplo:\n" +
                        "/agregarhechofuente {\"titulo\":\"hecho1\",\"ubicacion\":\"loc1\",...}");
                return;
            }

            // ✂️ Extraemos el JSON después del comando
            String hechoString = messageTextReceived.substring(messageTextReceived.indexOf(' ') + 1);

            // 🧩 Convertimos el texto JSON a un HechoDTO
            HechoDTO nuevoHecho = MessageFormatter.parseStringToHechoDTO(hechoString);

            // 🌐 Llamamos al proxy fuente
            FuenteProxy fuente = bot.fuenteProxy(mapper);
            HechoDTO hechoRetornado = fuente.postHecho(nuevoHecho);

            if (hechoRetornado == null) {
                bot.enviarMensaje(chatId, "❌ Ocurrió un error dando de alta el hecho");
                bot.enviarListaDeComandos(chatId);
            } else {
                bot.enviarMensaje(chatId, "➕ Dado de alta el hecho:\n\n" + MessageFormatter.hechoAString(hechoRetornado));
            }

        } catch (Exception e) {
            bot.enviarMensaje(chatId, "❌ Ocurrió un error dando de alta el hecho");
            bot.enviarListaDeComandos(chatId);
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "/agregarhechofuente";
    }

    @Override
    public String getDescription() {
        return "Agrega un hecho nuevo en la fuente de datos";
    }
}
