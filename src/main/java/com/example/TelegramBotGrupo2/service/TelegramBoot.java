package com.example.TelegramBotGrupo2.service;

import com.example.TelegramBotGrupo2.Enums.EstadoSolicitudBorradoEnum;
import com.example.TelegramBotGrupo2.clients.AgregadorProxy;
import com.example.TelegramBotGrupo2.clients.SolicitudProxy;
import com.example.TelegramBotGrupo2.dtos.SolicitudDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.example.TelegramBotGrupo2.clients.FuenteProxy;
import com.example.TelegramBotGrupo2.clients.ProcesadorPdiProxy;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.dtos.PdIDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class TelegramBoot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTextReceived = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            System.out.println("Message received: " + messageTextReceived);

            String[] partes = messageTextReceived.trim().split("\\s+");

            switch (messageTextReceived.split(" ")[0]) {
                case "/start":
                    enviarListaDeComandos(chatId);
                    break;

                case "/hechoscoleccion":
                    String coleccionId = partes[1];
                    AgregadorProxy agregadorProxy = new AgregadorProxy(
                            "https://two025-tp-entrega-2-gabrieliacomini.onrender.com",
                            objectMapper
                    );

                    try {
                        enviarMensaje(chatId, "Buscando hechos para la coleccion...");
                        List<HechoDTO> hechosDeColeccion = agregadorProxy.getHechos(coleccionId);

                        if (hechosDeColeccion.isEmpty()) {
                            enviarMensaje(chatId, "📭 No se encontraron hechos para la colección `" + coleccionId + "`");
                        } else {
                            String listaHechos = hechosDeColeccion.stream()
                                    .map(h -> hechoAString(h))
                                    .reduce("", (a, b) -> a + "\n" + b);

                            enviarMensaje(chatId, "📄 *Hechos de la colección " + coleccionId + ":*\n\n" + listaHechos);
                        }
                        enviarListaDeComandos(chatId);
                    } catch (Exception e) {
                        enviarMensaje(chatId, "❌ Ocurrió un error al obtener los hechos de la colección `" + coleccionId + "`");
                        enviarListaDeComandos(chatId);
                        e.printStackTrace();
                    }
                    break;

                case "/hecho":
                    String hechoId = partes[1];
                    FuenteProxy fuenteProxy = fuenteProxy(objectMapper);
                    ProcesadorPdiProxy pdiProxy = pdiProxy(objectMapper);

                    try {
                        enviarMensaje(chatId, "Buscando informacion del hecho...");
                        HechoDTO hecho = fuenteProxy.getHecho(hechoId);
                        if (hecho == null) {
                            enviarMensaje(chatId, "❌ No se encontro informacion del hecho: " + hechoId);
                            enviarListaDeComandos(chatId);
                            return;
                        }
                        List<String> pdiIdsDeHecho = fuenteProxy.getPdis(hechoId);
                        List<PdIDTO> pdisDeHecho = pdiIdsDeHecho.stream().map(pdiId -> pdiProxy.getPdi(pdiId)).toList();

                        enviarMensaje(chatId, "📄 *Hecho:*\n\n" + hechoAString((hecho)));

                        String mensajePdis = pdisDeHecho.stream()
                                .map(p -> pdiAString(p))
                                .reduce("", (a, b) -> a + "\n" + b);

                        enviarMensaje(chatId, "📄 *Pdis del hecho " + hechoId + ":*\n\n" + mensajePdis);
                        enviarListaDeComandos(chatId);
                    } catch (Exception e) {
                        enviarMensaje(chatId, "❌ Ocurrió un error al obtener la informacion del hecho `" + hechoId + "`");
                        enviarListaDeComandos(chatId);
                        e.printStackTrace();
                    }

                    break;

                case "/agregarhechofuente":
                    try {
                        String hechoString = messageTextReceived.substring(messageTextReceived.indexOf(' ')+1);
                        HechoDTO nuevoHecho = parseStringToHechoDTO(hechoString);

                        var fuente = fuenteProxy(objectMapper);

                        var hechoRetornado = fuente.postHecho(nuevoHecho);
                        if (hechoRetornado == null) {
                            enviarMensaje(chatId, "❌ Ocurrió un error dando de alta el hecho");
                            enviarListaDeComandos(chatId);
                        } else {
                            enviarMensaje(chatId, "➕ Dado de alta el hecho:\n\n" + hechoAString(hechoRetornado));
                        }
                    } catch (Exception e) {
                        enviarMensaje(chatId, "❌ Ocurrió un error dando de alta el hecho");
                        enviarListaDeComandos(chatId);
                        e.printStackTrace();
                    }
                    break;

                case "/agregarpdihecho":
                    String hechoId3 = partes[1];
                    String descripcion = partes[2];
                    String lugar = partes[3];
                    String momento = partes[4];
                    String url = partes[5];

                    PdIDTO nuevoPDI = new PdIDTO(null, hechoId3, descripcion, lugar, LocalDateTime.parse(momento), "", url, null);

                    var apiFuente = fuenteProxy(objectMapper);
                    var pdiCreado = apiFuente.postPdI(hechoId3, nuevoPDI);

                    if (pdiCreado == null) {
                        enviarMensaje(chatId, "❌ Ocurrió un error dando de alta el PdI");
                        enviarListaDeComandos(chatId);
                    } else {
                        enviarMensaje(chatId, "➕ Dado de alta el PdI:\n\n" + pdiAString(pdiCreado));
                        enviarListaDeComandos(chatId);
                    }
                    break;

                case "/solicitudborrado":
                    String hechoId2 = partes[1];
                    String estadoString = partes[2];
                    String desc = messageTextReceived.substring(messageTextReceived.indexOf(' ')+1);
                    desc = desc.substring(desc.indexOf(' ')+1);
                    desc = desc.substring(desc.indexOf(' ')+1);

                    EstadoSolicitudBorradoEnum estadoEnum = stringAEstadoSolicitud(estadoString);
                    if (estadoEnum == null) {
                        enviarMensaje(chatId, "❌ Estado invalido: " + estadoString);
                        enviarMensaje(chatId, "❗ Ingresa un estado válido y asegurate de que este en mayúsculas");
                        return;
                    }

                    SolicitudDto nuevaSolicitud = new SolicitudDto(null, desc, estadoEnum, hechoId2);

                    var api = solicitudProxy(objectMapper);
                    var solicitudCreada = api.postSolicitud(nuevaSolicitud);

                    if (solicitudCreada == null) {
                        enviarMensaje(chatId, "❌ Ocurrió un error dando de alta el hecho");
                        enviarListaDeComandos(chatId);
                    } else {
                        enviarMensaje(chatId, "➕ Dada de alta la solicitud:\n\n" + solicitudAString(solicitudCreada));
                        enviarListaDeComandos(chatId);
                    }

                    break;

                case "/cambiarestadosolicitud":
                    try {
                        String solicitudId = partes[1];
                        String nuevoEstado = partes[2];
                        EstadoSolicitudBorradoEnum nuevoEstadoEnum = stringAEstadoSolicitud(nuevoEstado);
                        if (nuevoEstadoEnum == null) {
                            enviarMensaje(chatId, "❌ Estado invalido: " + nuevoEstado);
                            enviarMensaje(chatId, "❗ Ingresa un estado válido y asegurate de que este en mayúsculas");
                            return;
                        }

                        var solicitudes = solicitudProxy(objectMapper);

                        var solicitud = solicitudes.getSolicitud(Integer.parseInt(solicitudId));
                        if (solicitud == null) {
                            enviarMensaje(chatId, "❌ Ocurrió un error buscando la solicitud");
                            enviarListaDeComandos(chatId);
                            return;
                        }

                        solicitud.setEstado(nuevoEstadoEnum);
                        var retorno = solicitudes.patchSolicitud(solicitud);

                        if (retorno == null) {
                            enviarMensaje(chatId, "❌ Ocurrió un error cambiando el estado de la solicitud");
                            enviarListaDeComandos(chatId);
                            return;
                        }

                        enviarMensaje(chatId, "✅ Se cambio el estado de la solicitud "+solicitudId+ " a "+nuevoEstado);
                        enviarListaDeComandos(chatId);

                    } catch (Exception e) {
                        enviarMensaje(chatId, "❌ Ocurrió un error critico cambiando el estado de la solicitud");
                        enviarListaDeComandos(chatId);
                        e.printStackTrace();
                    }

                    break;

                default:
                    enviarMensaje(chatId, "Recibí: " + messageTextReceived + "\nUsá /start para ver los comandos disponibles.");
                    break;
            }

        }
    }

    private EstadoSolicitudBorradoEnum stringAEstadoSolicitud(String estadoString) {
        EstadoSolicitudBorradoEnum estadoEnum = null;
        switch (estadoString.toUpperCase(Locale.ROOT)) {
            case "CREADA":
                estadoEnum = EstadoSolicitudBorradoEnum.CREADA;
                break;
            case "VALIDADA":
                estadoEnum = EstadoSolicitudBorradoEnum.VALIDADA;
                break;
            case "EN_DISCUCION":
                estadoEnum = EstadoSolicitudBorradoEnum.EN_DISCUCION;
                break;
            case "ACEPTADA":
                estadoEnum = EstadoSolicitudBorradoEnum.ACEPTADA;
                break;
            case "RECHAZADA":
                estadoEnum = EstadoSolicitudBorradoEnum.RECHAZADA;
                break;
        }
        return estadoEnum;
    }

    private String solicitudAString(SolicitudDto h) {
        return String.format("""
                                            🆔 %s
                                            🪪 %s
                                            📘 %s
                                            🟢 %s
                                            """,
                h.getId(),
                h.getHechoId(),
                h.getDescripcion(),
                h.getEstado().toString()
        );
    }

    private String hechoAString(HechoDTO h) {
        return String.format("""
                                            📘 %s
                                            🆔 %s
                                            📍 %s
                                            📅 %s
                                            🏷️ Categoría: %s
                                            🌐 Origen: %s
                                            """,
                h.getTitulo(),
                h.getId(),
                h.getUbicacion(),
                h.getFecha() != null ? h.getFecha().toString().replace('T', ' ') : "Sin fecha",
                h.getCategoria() != null ? h.getCategoria() : "Sin categoría",
                h.getOrigen() != null ? h.getOrigen() : "Sin origen"
        );
    }

    private String pdiAString(PdIDTO p) {
        return String.format("""
                                            🆔 %s
                                            📍 %s
                                            📘 %s
                                            🏷️ Categoría: %s
                                            📅 %s
                                            🌐: %s
                                            """,
                p.getId(),
                p.getLugar(),
                p.getContenido(),
                p.getDescripcion(),
                p.getMomento() != null ? p.getMomento().toString().replace('T', ' ') : "Sin fecha",
                p.getUrlImagen()
        );
    }

    private SolicitudProxy solicitudProxy(ObjectMapper objectMapper) {
        return new SolicitudProxy(
                "https://two025-tp-dds-solicitudes.onrender.com",
                objectMapper
        );
    }

    private FuenteProxy fuenteProxy(ObjectMapper objectMapper) {
        return new FuenteProxy(
                "https://tp-dds-2025-fuente-grupo2-2-fuentes-2.onrender.com",
                objectMapper
        );
    }

    private ProcesadorPdiProxy pdiProxy(ObjectMapper mapper) {
        return new ProcesadorPdiProxy(
                "https://tp-dds-2025-procesadorpdi-grupo2-1.onrender.com/api/",
                mapper
        );
    }

    private void enviarListaDeComandos(Long chatId) {
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


    // Define the primary separator for DTO fields.
    //private static final String PRIMARY_SEPARATOR = "\\|";
    private static final String PRIMARY_SEPARATOR = " ";
    // Define the secondary separator for list items (e.g., tags).
    private static final String SECONDARY_SEPARATOR = ",";

    /**
     * A Java 8 Function that parses a formatted string into an HechoDTO.
     * <p>
     * The expected string format is:
     * /agregarhechofuente coleccionTest titulo etiqueta1,etiqueta2 OTRO ubicacion 2016-03-16T13:56:39 origen
     * "nombreColeccion|titulo|etiqueta1,etiqueta2|categoria|ubicacion|fecha|origen"
     * "nombreColeccion titulo etiqueta1,etiqueta2 categoria ubicacion fecha origen"
     * </p>
     * It handles potential missing parts gracefully by assigning null.
     */
    private HechoDTO parseStringToHechoDTO(String inputString) {
        if (inputString == null || inputString.trim().isEmpty()) {
            // Return an empty DTO if the input is null or blank.
            return new HechoDTO();
        }

        String[] parts = inputString.split(PRIMARY_SEPARATOR, -1); // -1 to include trailing empty strings

        HechoDTO dto = new HechoDTO();

        // Assign parts to DTO fields based on their index.
        dto.setNombreColeccion(getPart(parts, 0));
        dto.setTitulo(getPart(parts, 1));

        // The 'etiquetas' (tags) part is a comma-separated list.
        String etiquetasString = getPart(parts, 2);
        if (etiquetasString != null && !etiquetasString.isEmpty()) {
            List<String> etiquetas = Arrays.stream(etiquetasString.split(SECONDARY_SEPARATOR))
                    .map(String::trim)
                    .collect(Collectors.toList());
            dto.setEtiquetas(etiquetas);
        } else {
            dto.setEtiquetas(Collections.emptyList());
        }

        dto.setCategoria(getPart(parts, 3));
        dto.setUbicacion(getPart(parts, 4));
        dto.setFecha(getPart(parts, 5));
        dto.setOrigen(getPart(parts, 6));

        return dto;
    };

    /**
     * Helper method to safely get a part from the split array.
     * Returns null if the index is out of bounds.
     *
     * @param parts The array of string parts.
     * @param index The index to retrieve.
     * @return The string at the specified index or null.
     */
    private static String getPart(String[] parts, int index) {
        if (index < parts.length && !parts[index].trim().isEmpty()) {
            return parts[index].trim();
        }
        return null;
    }
}

