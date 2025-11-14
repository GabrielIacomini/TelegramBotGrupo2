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

    private static final int PAGE_SIZE = 3;
    private final TelegramBoot bot;

    public PaginatorFormatter(TelegramBoot bot) {
        this.bot = bot;
    }

    public void mostrarPagina(Long chatId, String tipo, String contexto, int page, List<HechoDTO> hechos, int total) {
        if (hechos.isEmpty()) {
            bot.enviarMensaje(chatId, "📭 No se encontraron hechos.");
            return;
        }

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, hechos.size());
        List<HechoDTO> sublist;
        if (hechos.size() == total) {
            sublist = hechos.subList(start, end);
        } else {
            end = start + hechos.size();
            sublist = hechos;
        }


        String texto = sublist.stream()
                .map(MessageFormatter::hechoAString)
                .collect(Collectors.joining("\n\n"));

        texto += String.format("\n\n📄 Mostrando %d-%d de %d hechos.",
                start + 1, end, total);

        InlineKeyboardMarkup keyboard = crearBotonesPaginado(tipo, contexto, page, total);

        bot.enviarMensajeConTeclado(chatId, texto, keyboard);
    }

    private static InlineKeyboardMarkup crearBotonesPaginado(String tipo, String contexto, int page, int total) {
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> botones = new ArrayList<>();
        int totalPages = (int) Math.ceil(total / (double) PAGE_SIZE);

        if (page > 0) {
            InlineKeyboardButton prev = new InlineKeyboardButton();
            prev.setText("◀️ Anterior");
            prev.setCallbackData(tipo + "_prev_" + contexto + "_" + (page - 1));
            botones.add(prev);
        }

        if (page < totalPages - 1) {
            InlineKeyboardButton next = new InlineKeyboardButton();
            next.setText("Siguiente ▶️");
            next.setCallbackData(tipo + "_next_" + contexto + "_" + (page + 1));
            botones.add(next);
        }

        if (!botones.isEmpty()) filas.add(botones);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(filas);
        return markup;
    }
}
