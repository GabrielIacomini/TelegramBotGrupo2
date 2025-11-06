package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.clients.FuenteProxy;
import com.example.TelegramBotGrupo2.clients.ProcesadorPdiProxy;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.dtos.PdIDTO;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.example.TelegramBotGrupo2.service.utils.MessageFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class HechoCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;

    public HechoCommand(TelegramBoot bot, ObjectMapper mapper) {
        this.bot = bot;
        this.mapper = mapper;
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        String[] partes = update.getMessage().getText().split("\\s+");

        if (partes.length < 2) {
            bot.enviarMensaje(chatId, "⚠️ Debes indicar un ID de hecho. Ej: /hecho hecho1");
            return;
        }

        String hechoId = partes[1];
        FuenteProxy fuenteProxy = bot.fuenteProxy(mapper);
        ProcesadorPdiProxy pdiProxy = bot.pdiProxy(mapper);

        try {
            bot.enviarMensaje(chatId, "🔎 Buscando información del hecho...");

            HechoDTO hecho = fuenteProxy.getHecho(hechoId);
            if (hecho == null) {
                bot.enviarMensaje(chatId, "❌ No se encontró información del hecho: " + hechoId);
                bot.enviarListaDeComandos(chatId);
                return;
            }

            List<String> pdiIdsDeHecho = fuenteProxy.getPdis(hechoId);
            List<PdIDTO> pdisDeHecho = pdiIdsDeHecho.stream()
                    .map(pdiProxy::getPdi)
                    .toList();

            bot.enviarMensaje(chatId, "📄 *Hecho:*\n\n" + MessageFormatter.hechoAString(hecho));

            String mensajePdis = pdisDeHecho.stream()
                    .map(MessageFormatter::pdiAString)
                    .reduce("", (a, b) -> a + "\n" + b);

            bot.enviarMensaje(chatId, "📄 *PDIs del hecho " + hechoId + ":*\n\n" + mensajePdis);
            bot.enviarListaDeComandos(chatId);

        } catch (Exception e) {
            bot.enviarMensaje(chatId, "❌ Ocurrió un error al obtener la información del hecho `" + hechoId + "`");
            bot.enviarListaDeComandos(chatId);
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "/hecho";
    }

    @Override
    public String getDescription() {
        return "Obtiene la información de un hecho en concreto";
    }
}
