package com.proyecto.WebRopa.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class DiasHabilesHelper {

    // Suma N días hábiles (lun-vie) a una fecha base.
    // Nota: no contempla feriados peruanos — mejora futura.
    public static LocalDateTime agregarDiasHabiles(LocalDateTime base, int diasHabiles) {
        LocalDateTime resultado = base;
        int contados = 0;
        while (contados < diasHabiles) {
            resultado = resultado.plusDays(1);
            DayOfWeek dia = resultado.getDayOfWeek();
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {
                contados++;
            }
        }
        return resultado;
    }
}
