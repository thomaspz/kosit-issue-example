package de.kosit.xrechnung.validationtool;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.xrechnung.validationtool.suite.Xrechnung;
import de.kosit.xrechnung.validationtool.suite.XrechnungValidationException;

public class ValidationtoolEineXrechnungTest {

	private static final String VALIDATOR_CONFIGURATION = "/validator-configuration-xrechnung_1.2.2_2019-12-30/scenarios.xml";

	@Test
	public void test() throws XrechnungValidationException {

		// GIVEN

		// Version aus Testsuuite von : xrechnung-1.2.2-testsuite-2019-12-30
		String filename = "01.15a-INVOICE_ubl.xml";
		InputStream resourceAsStream = ValidationtoolEineXrechnungTest.class
				.getResourceAsStream("/xrechnung-1.2.2-testsuite-2019-12-30/instances/" + filename);
		Xrechnung xrechnung = new Xrechnung(filename, resourceAsStream);

		// WHEN
		Result validationResult = kositValidation(xrechnung);

		// THEN
		boolean proceedDocument = proceedDocument(validationResult);

		// DEBUG - Error Report
		writeErrorReport(filename, validationResult, proceedDocument);

		/*
		 * True, weil der Report folgende Aussage trifft:
		 * "Bewertung: Es wird empfohlen  das Dokument anzunehmen und weiter zu verarbeiten."
		 */
		assertTrue(proceedDocument);

	}

	private void writeErrorReport(String filename, Result validationResult, boolean proceedDocument)
			throws XrechnungValidationException {
		if (!proceedDocument) {
			File parent = new File("./tmp");
			parent.mkdirs();
			File f = new File(parent, filename + ".html");
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, Charset.forName("UTF-8")))) {
				writer.write(document2String(validationResult.getReportDocument()));
				writer.flush();
			} catch (IOException e) {
				Assert.fail(e.getMessage());
			}
		}
	}

	private Result kositValidation(Xrechnung dokument) throws XrechnungValidationException {
		Result report = null;
		URL scenarios = this.getClass().getResource(VALIDATOR_CONFIGURATION);
		try {
			CheckConfiguration config = new CheckConfiguration(scenarios.toURI());
			Check validator = new DefaultCheck(config);
			Input document = InputFactory.read(dokument.getDaten(), dokument.getFilename());
			report = validator.checkInput(document);
		} catch (URISyntaxException e) {
			String msg = "Fehler";
			throw new XrechnungValidationException(msg, e);
		}
		return report;
	}

	private boolean proceedDocument(Result validationResult) {

		/*
		 * Ist in Klärung, ob wir uns auf acceptable verlassen können, bis dahin erstmal
		 * true // https://github.com/itplr-kosit/validator/issues/43
		 */
		boolean acceptable = false;
		switch (validationResult.getAcceptRecommendation()) {
		case ACCEPTABLE:
			acceptable = true;
			break;
		case UNDEFINED:
			acceptable = true;
			break;
		case REJECT:
			acceptable = false;
			break;
		}

		boolean processingSuccessful = validationResult.isProcessingSuccessful();
		boolean schemaValid = validationResult.isSchemaValid();
		boolean wellformed = validationResult.isWellformed();
		boolean result = acceptable && processingSuccessful && schemaValid && wellformed;

		return result;
	}

//	private String getErrorText(Result validationResult) {
//		StringBuilder builder = new StringBuilder("");
//		for (String err : validationResult.getProcessingErrors()) {
//			builder.append(err);
//			builder.append("\n");
//		}
//		return builder.toString();
//	}

	private String document2String(Document document) throws XrechnungValidationException {
		String result = "";
		if (document != null) {
			try (StringWriter writer = new StringWriter()) {

				DOMSource domSource = new DOMSource(document);
				StreamResult streamresult = new StreamResult(writer);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer;
				transformer = tf.newTransformer();
				transformer.transform(domSource, streamresult);
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				result = writer.toString();
				writer.flush();
			} catch (TransformerException | IOException e) {
				String message = "Fehler beim Umwandel des Fehlerreports zum String";
				throw new XrechnungValidationException(message, e);
			}
		}
		return result;

	}

}
