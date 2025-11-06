package com.example.TelegramBotGrupo2.service.utils;

import com.example.TelegramBotGrupo2.Enums.EstadoSolicitudBorradoEnum;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.dtos.PdIDTO;
import com.example.TelegramBotGrupo2.dtos.SolicitudDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MessageFormatter {

    public static String solicitudAString(SolicitudDto h) {
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

    public static String hechoAString(HechoDTO h) {
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

    public static String pdiAString(PdIDTO p) {
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

    public static HechoDTO parseStringToHechoDTO(String inputString) {
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
    }

    public static EstadoSolicitudBorradoEnum stringAEstadoSolicitud(String estadoString) {
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

    private static String getPart(String[] parts, int index) {
        if (index < parts.length && !parts[index].trim().isEmpty()) {
            return parts[index].trim();
        }
        return null;
    }

    private static final String PRIMARY_SEPARATOR = " ";
    // Define the secondary separator for list items (e.g., tags).
    private static final String SECONDARY_SEPARATOR = ",";
}
