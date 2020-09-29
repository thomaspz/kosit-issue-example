package de.kosit.xrechnung.validationtool.suite;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Xrechnung {

   private String filename = "dateiname-unbekannt";

   private InputStream daten;

}
