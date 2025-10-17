package com.example.TelegramBotGrupo2.service;

import com.example.TelegramBotGrupo2.clients.AgregadorProxy;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class TelegramBoot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTextReceived = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            System.out.println("Message received: " + messageTextReceived);

            switch (messageTextReceived.split(" ")[0]) {
                case "/start":
                    enviarListaDeComandos(chatId);
                    break;

                case "/hechoscoleccion":
                    String[] partes = messageTextReceived.trim().split("\\s+");

                    String coleccionId = partes[1];
                    AgregadorProxy agregadorProxy = new AgregadorProxy(
                            "https://two025-tp-entrega-2-gabrieliacomini.onrender.com",
                            new ObjectMapper()
                    );

                    try {
                        List<HechoDTO> hechosDeColeccion = agregadorProxy.getHechos(coleccionId);

                        if (hechosDeColeccion.isEmpty()) {
                            enviarMensaje(chatId, "📭 No se encontraron hechos para la colección `" + coleccionId + "`");
                        } else {
                            String listaHechos = hechosDeColeccion.stream()
                                    .map(HechoDTO::toString)
                                    .reduce("", (a, b) -> a + "\n• " + b);

                            enviarMensaje(chatId, "📄 *Hechos de la colección " + coleccionId + ":*\n" + listaHechos);
                        }
                    } catch (Exception e) {
                        enviarMensaje(chatId, "❌ Ocurrió un error al obtener los hechos de la colección `" + coleccionId + "`");
                        e.printStackTrace();
                    }
                    break;

                case "/hecho":
                    enviarMensaje(chatId, "📘 Permite visualizar un hecho concreto con sus PDIs e imágenes.\n\nEjemplo:\n`/hecho <id_hecho>`");
                    break;

                case "/agregarhechofuente":
                    enviarMensaje(chatId, "➕ Agregá un hecho a una fuente concreta.\n\nEjemplo:\n`/agregarhechofuente <id_fuente> <id_hecho>`");
                    break;

                case "/agregarpdihecho":
                    enviarMensaje(chatId, "📍 Agregá un PDI (Punto de Interés) a un hecho.\n\nEjemplo:\n`/agregarpdihecho <id_hecho> <id_pdi>`");
                    break;

                case "/solicitudborrado":
                    enviarMensaje(chatId, "🗑️ Crea una solicitud de borrado de un hecho o elemento.\n\nEjemplo:\n`/solicitudborrado <id_hecho>`");
                    break;

                case "/cambiarestadosolicitud":
                    enviarMensaje(chatId, "⚙️ Permite cambiar el estado de una solicitud de borrado.\n\nEjemplo:\n`/cambiarestadosolicitud <id_solicitud> <nuevo_estado>`");
                    break;

                default:
                    enviarMensaje(chatId, "Recibí: " + messageTextReceived + "\nUsá /start para ver los comandos disponibles.");
                    break;
            }

        }
    }

    private void enviarListaDeComandos(Long chatId) {
        String comandos = """
                📜 *Comandos disponibles:*
                /hechoscoleccion - Se obtienen los hechos de una colección específica.
                /hecho - Permite visualizar un hecho concreto, que incluye sus PDIs e imágenes.
                /agregarhechofuente - Permite agregar un hecho a una fuente concreta.
                /agregarpdihecho - Permite agregar un PDI a un hecho.
                /solicitudborrado - Permite hacer una solicitud de borrado.
                /cambiarestadosolicitud - Permite cambiar el estado de una solicitud de borrado.
                """;

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(comandos)
                .parseMode("Markdown")
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensaje(Long chatId, String texto) {
        SendMessage message = new SendMessage(chatId.toString(), texto);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return System.getenv("TELEGRAM_BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_TOKEN");
    }

    @Override
    public void onRegister() {
        super.onRegister();
        registrarComandosEnTelegram();
    }

    private void registrarComandosEnTelegram() {
        try {
            SetMyCommands setMyCommands = new SetMyCommands();
            setMyCommands.setCommands(List.of(
                    new BotCommand("hechoscoleccion", "Se obtienen los hechos de una colección específica"),
                    new BotCommand("hecho", "Permite visualizar un hecho concreto, que incluye sus PDIs e imágenes"),
                    new BotCommand("agregarhechofuente", "Permite agregar un hecho a una fuente concreta"),
                    new BotCommand("agregarpdihecho", "Permite agregar un PDI a un hecho"),
                    new BotCommand("solicitudborrado", "Permite hacer una solicitud de borrado"),
                    new BotCommand("cambiarestadosolicitud", "Permite cambiar el estado de una solicitud de borrado")
            ));
            setMyCommands.setScope(new BotCommandScopeDefault());

            execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

