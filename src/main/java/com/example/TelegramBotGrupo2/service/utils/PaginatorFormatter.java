package com.example.TelegramBotGrupo2.service.utils;

import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.service.TelegramBoot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaginatorFormatter {

    private static final int PAGE_SIZE = 5;
    private static TelegramBoot bot = null;

    public PaginatorFormatter(TelegramBoot bot) {
        PaginatorFormatter.bot = bot;
    }

    public static void mostrarPagina(Long chatId, String coleccionId, int page, List<HechoDTO> hechos) {

        if (hechos.isEmpty()) {
            bot.enviarMensaje(chatId, "📭 No se encontraron hechos `");
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

    }

    private static InlineKeyboardMarkup crearBotonesPaginado(String coleccionId, int page, int total) {
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

}
