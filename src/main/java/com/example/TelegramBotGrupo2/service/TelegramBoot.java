package com.example.TelegramBotGrupo2.service;

import com.example.TelegramBotGrupo2.clients.AgregadorProxy;
import com.example.TelegramBotGrupo2.clients.SolicitudProxy;
import com.example.TelegramBotGrupo2.dtos.BusquedaReqDTO;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.service.commands.CommandAction;
import com.example.TelegramBotGrupo2.service.commands.CommandRegistry;
import com.example.TelegramBotGrupo2.service.commands.impl.*;
import com.example.TelegramBotGrupo2.service.utils.PaginatorFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.example.TelegramBotGrupo2.clients.FuenteProxy;
import com.example.TelegramBotGrupo2.clients.ProcesadorPdiProxy;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class TelegramBoot extends TelegramLongPollingBot {

    private final CommandRegistry registry = new CommandRegistry();
    private final ObjectMapper mapper;

    public TelegramBoot() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        this.mapper = objectMapper;
        mapper.findAndRegisterModules();
        registrarComandos();
    }

    private void registrarComandos() {
        registry.register(new StartCommandAction(this));
        registry.register(new HechosColeccionCommand(this, mapper));
        registry.register(new HechoCommand(this, mapper));
        registry.register(new AgregarHechoFuenteCommand(this, mapper));
        registry.register(new AgregarPdiHechoCommand(this, mapper));
        registry.register(new CambiarEstadoSolicitudCommand(this, mapper));
        registry.register(new SolicitudBorradoCommand(this, mapper));
        registry.register(new CambiarConsensoCommand(this, mapper));
        registry.register(new BusquedaHechosCommand(this, mapper));
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            // comandos normales
            String text = update.getMessage().getText().split("\\s+")[0];
            CommandAction command = registry.getCommand(text);

            if (command != null) {
                command.execute(update);
            } else {
                enviarMensaje(update.getMessage().getChatId(), "❓ Comando desconocido.");
            }
        } else if (update.hasCallbackQuery()) {
            manejarCallback(update);
        }
    }

    public SolicitudProxy solicitudProxy(ObjectMapper objectMapper) {
        return new SolicitudProxy(
                "https://two025-tp-dds-solicitudes.onrender.com",
                objectMapper
        );
    }

    public FuenteProxy fuenteProxy(ObjectMapper objectMapper) {
        return new FuenteProxy(
                "https://tp-dds-2025-fuente-grupo2-2-fuentes-2.onrender.com",
                objectMapper
        );
    }

    public ProcesadorPdiProxy pdiProxy(ObjectMapper mapper) {
        return new ProcesadorPdiProxy(
                "https://tp-dds-2025-procesadorpdi-grupo2-1.onrender.com/api/",
                mapper
        );
    }

    public void enviarListaDeComandos(Long chatId) {
        String comandos = """
            📜 <b>Comandos disponibles:</b>
            
            ❔ Buscá los Hechos pertenecientes a una colección específica.
            Ejemplo:
            <a href="tg://msg?text=/hechoscoleccion">/hechoscoleccion &lt;nombre_coleccion&gt;</a>
            
            👁️ Visualizá un Hecho concreto, que incluye sus PDIs e imágenes.
            Ejemplo:
            <a href="tg://msg?text=/hecho">/hecho &lt;id_hecho&gt;</a>
            
            ⚡ Agregá un Hecho a una fuente concreta.
            Ejemplo:
            <a href="tg://msg?text=/agregarhechofuente">/agregarhechofuente &lt;nombre_coleccion&gt; &lt;titulo&gt; &lt;etiquetas_separadas_por_coma&gt; &lt;categoria&gt; &lt;ubicacion&gt; &lt;momento&gt; &lt;origen&gt;</a>
            
            📍 Agregá un PDI (Punto de Interés) a un hecho.
            Ejemplo:
            <a href="tg://msg?text=/agregarpdihecho">/agregarpdihecho &lt;id_hecho&gt; &lt;descripcion&gt; &lt;lugar&gt; &lt;momento&gt; &lt;url&gt;</a>
            
            🗑️ Permite hacer una solicitud de borrado.
            Ejemplo:
            <a href="tg://msg?text=/solicitudborrado">/solicitudborrado &lt;id_hecho&gt; &lt;ESTADO&gt; &lt;descripcion&gt;</a>
            
            🔀 Permite cambiar el estado de una solicitud de borrado.
            Ejemplo:
            <a href="tg://msg?text=/cambiarestadosolicitud">/cambiarestadosolicitud &lt;id_solicitud&gt; &lt;NUEVO_ESTADO&gt;</a>
            
            🔀 Permite cambiar el consenso de una coleccion.
            Ejemplo:
            <a href="tg://msg?text=/cambiarconsenso">/cambiarconsenso &lt;NUEVO_CONSENSO&gt; &lt;nombre_coleccion&gt;</a>
            
            🔍 <b>Buscar hechos por palabras clave y tags</b>
            Ejemplo:
            <a href="tg://msg?text=/buscarhechos ">/buscarhechos &lt;palabras_clave&gt;</a>
            """;

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(comandos)
                .parseMode("HTML")
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(Long chatId, String texto) {
        SendMessage message = new SendMessage(chatId.toString(), texto);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensajeConTeclado(Long chatId, String texto, InlineKeyboardMarkup teclado) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(texto);
        message.setParseMode("Markdown");
        message.setReplyMarkup(teclado);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void manejarCallback(Update update) {
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        String[] partes = data.split("_", 4);
        String tipo = partes[0];      // "hechos" o "buscar"
        String contexto = partes[2];  // coleccionId o hash de búsqueda
        int pagina = Integer.parseInt(partes[3]);

        AgregadorProxy agregador = new AgregadorProxy("https://two025-tp-entrega-2-gabrieliacomini.onrender.com", mapper);

        switch (tipo) {
            case "hechos":
                List<HechoDTO> hechos = agregador.getHechos(contexto);

                new PaginatorFormatter(this).mostrarPagina(chatId, "hechos", contexto, pagina, hechos, hechos.size());
                break;
            case "buscar":
                BusquedaReqDTO req = new BusquedaReqDTO();
                req.setTerminos(Arrays.asList(contexto.split("\\s+")));
                req.setPageIdx(pagina);
                req.setPageSize(3);

                var resp = agregador.buscarHechosPorPalabrasClaves(req);

                new PaginatorFormatter(this).mostrarPagina(chatId, "buscar", contexto, pagina, resp.hechos, resp.total);
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
                    new BotCommand("cambiarestadosolicitud", "Permite cambiar el estado de una solicitud de borrado"),
                    new BotCommand("cambiarconsenso", "Permite cambiar el consenso del agregador"),
                    new BotCommand("buscarhechos", "Permite buscar hechos por una o mas palabras claves, y por tag")
            ));
            setMyCommands.setScope(new BotCommandScopeDefault());

            execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}

