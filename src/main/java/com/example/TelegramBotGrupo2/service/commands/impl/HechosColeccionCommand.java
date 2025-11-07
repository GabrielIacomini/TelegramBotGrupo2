package com.example.TelegramBotGrupo2.service.commands.impl;

import com.example.TelegramBotGrupo2.clients.AgregadorProxy;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.example.TelegramBotGrupo2.service.utils.MessageFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HechosColeccionCommand implements CommandAction {

    private final TelegramBoot bot;
    private final ObjectMapper mapper;
    private static final int PAGE_SIZE = 5;

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
        mostrarPagina(chatId, coleccionId, 0);

    }

    public void mostrarPagina(Long chatId, String coleccionId, int page) {
        AgregadorProxy agregador = new AgregadorProxy("https://two025-tp-entrega-2-gabrieliacomini.onrender.com", mapper);

        try {
            List<HechoDTO> hechos = agregador.getHechos(coleccionId);

            if (hechos.isEmpty()) {
                bot.enviarMensaje(chatId, "📭 No se encontraron hechos para la colección `" + coleccionId + "`");
                return;
            }

            int start = page * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, hechos.size());
            List<HechoDTO> sublist = hechos.subList(start, end);

            String texto = sublist.stream()
                    .map(h -> MessageFormatter.hechoAString(h))
                    .collect(Collectors.joining("\n\n"));

            texto += String.format("\n\n📄 Mostrando %d-%d de %d hechos.",
                    start + 1, end, hechos.size());

            InlineKeyboardMarkup keyboard = crearBotonesPaginado(coleccionId, page, hechos.size());

            bot.enviarMensajeConTeclado(chatId, texto, keyboard);
            bot.enviarListaDeComandos(chatId);

        } catch (Exception e) {
            bot.enviarMensaje(chatId, "❌ Error al obtener hechos de la colección");
            bot.enviarListaDeComandos(chatId);
        }
    }

    private InlineKeyboardMarkup crearBotonesPaginado(String coleccionId, int page, int total) {
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> botones = new ArrayList<>();

        int totalPages = (int) Math.ceil(total / (double) PAGE_SIZE);

        if (page > 0) {
            InlineKeyboardButton prev = new InlineKeyboardButton();
            prev.setText("◀️ Anterior");
            prev.setCallbackData("hechos_prev_" + coleccionId + "_" + (page - 1));
            botones.add(prev);
        }

        if (page < totalPages - 1) {
            InlineKeyboardButton next = new InlineKeyboardButton();
            next.setText("Siguiente ▶️");
            next.setCallbackData("hechos_next_" + coleccionId + "_" + (page + 1));
            botones.add(next);
        }

        if (!botones.isEmpty()) {
            filas.add(botones);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(filas);
        return markup;
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
