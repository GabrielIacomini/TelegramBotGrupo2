package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.clients.FuenteProxy;
import com.example.TelegramBotGrupo2.dtos.PdIDTO;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.example.TelegramBotGrupo2.service.utils.MessageFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.List;

public class AgregarPdiHechoCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;

    public AgregarPdiHechoCommand(TelegramBoot bot, ObjectMapper mapper) {
        this.bot = bot;
        this.mapper = mapper;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        List<String> partes = List.of(text.split("\\s+"));

        if (partes.size() < 6) {
            bot.enviarMensaje(chatId, "⚠️ Uso incorrecto del comando.\n\nFormato esperado:\n/agregarpdihecho <hechoId> <descripcion> <lugar> <momento> <url>");
            bot.enviarListaDeComandos(chatId);
            return;
        }

        try {
            String hechoId = partes.get(1);
            String descripcion = partes.get(2);
            String lugar = partes.get(3);
            String momento = partes.get(4);
            String url = partes.get(5);

            FuenteProxy fuenteProxy = bot.fuenteProxy(mapper);

            PdIDTO nuevoPDI = new PdIDTO(
                    null,
                    hechoId,
                    descripcion,
                    lugar,
                    LocalDateTime.parse(momento),
                    "",
                    url,
                    null
            );

            PdIDTO pdiCreado = fuenteProxy.postPdI(hechoId, nuevoPDI);

            if (pdiCreado == null) {
                bot.enviarMensaje(chatId, "❌ Ocurrió un error dando de alta el PdI.");
                bot.enviarListaDeComandos(chatId);
            } else {
                bot.enviarMensaje(chatId, "➕ Dado de alta el PdI:\n\n" + MessageFormatter.pdiAString(pdiCreado));
                bot.enviarListaDeComandos(chatId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            bot.enviarMensaje(chatId, "❌ Error al procesar el comando /agregarpdihecho");
            bot.enviarListaDeComandos(chatId);
        }
    }

    @Override
    public String getName() {
        return "/agregarpdihecho";
    }

    @Override
    public String getDescription() {
        return "Agrega un nuevo PDI asociado a un hecho existente";
    }
}



