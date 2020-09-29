package de.kosit.xrechnung.validationtool.suite;

public class XrechnungValidationException extends Exception {

   private static final long serialVersionUID = -1895890011769373545L;

   public XrechnungValidationException(String message, Exception e) {
      super(message, e);
   }

}
