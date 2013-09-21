package ch.eitchnet.converter.nokiaandroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageConverter {

	private static final Logger logger = LoggerFactory.getLogger(MessageConverter.class);

	public void convert(File input, File output) {

		int nrOfMessages = 0;
		try (BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"))) {
			String line = "";
			while ((line = fin.readLine()) != null) {
				if (line.startsWith("sms"))
					nrOfMessages++;
			}
		} catch (Exception e) {
			if (output.exists())
				output.delete();
			throw new RuntimeException("Failed reading from " + input, e);
		}
		logger.info("Found " + nrOfMessages + " messages to parse. (MMS are skipped)");

		int lineNr = 0;
		String line = "";
		try (BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
				BufferedWriter fout = new BufferedWriter(new FileWriter(output))) {

			fout.write("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
			fout.write("<smses count=\"" + nrOfMessages + "\">\n");

			while ((line = fin.readLine()) != null) {
				lineNr++;
				if (!line.startsWith("sms"))
					continue;

				// sms;deliver;"+41794618789";"";"";"2013.09.20 13:48";"";"Küssli!"
				// <sms protocol="0" address="+41792264029" date="1295380212996" type="1" subject="null" body="Test" toa="null" sc_toa="null" service_center="+41794999000" read="1" status="-1" locked="0" />

				NokiaMessage nm = new NokiaMessage();
				Position position = Position.PROTOCOL;
				DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");

				try (Scanner scanner = new Scanner(line);) {
					scanner.useDelimiter(";");

					while (scanner.hasNext()) {

						if (position != Position.BODY) {
							String value = scanner.next().replaceAll("\"", "").trim();

							switch (position) {
							case PROTOCOL:
								nm.protocol = value;
								break;
							case TYPE:
								nm.type = value;
								break;
							case CONTACT:
								nm.contact = value;
								break;
							case DATE_TIME:
								String timestamp = Long.toString(df.parse(value).getTime());
								nm.dateTime = timestamp;
								break;
							case SOMETHING_1:
							case SOMETHING_2:
							case SOMETHING_3:
							case BODY:
							default:
								break;
							}

							position = position.next();
						} else {

							String value = scanner.nextLine();
							value = value.substring(2, value.length() - 1);
							value = value.replaceAll("\"\"", "\"");
							value = value.replaceAll("\"", "&quot;");
							nm.body = value;
						}
					}

					logger.info("Parsed SMS: " + nm);
				}

				fout.write("  <sms");
				fout.write(" protocol=\"" + nm.protocol + "\"");
				fout.write(" address=\"" + nm.contact + "\"");
				fout.write(" date=\"" + nm.dateTime + "\"");
				fout.write(" type=\"1\"");
				fout.write(" subject=\"null\"");
				//fout.write(" body=\"<![CDATA[" + nm.body + "]]>\"");
				fout.write(" body=\"" + nm.body + "\"");
				fout.write(" toa=\"null\"");
				fout.write(" sc_toa=\"null\"");
				fout.write(" service_center=\"+41794999000\"");
				fout.write(" read=\"1\"");
				fout.write(" status=\"-1\"");
				fout.write(" locked=\"0\"");
				fout.write(" />\n");
			}

			fout.write("</smses>");
			logger.info("Parsed " + nrOfMessages + " messages and wrote XML to " + output.getAbsolutePath());

		} catch (Exception e) {
			if (output.exists())
				output.delete();
			throw new RuntimeException("Failed reading from " + input + " at line " + lineNr + ": " + line, e);
		}
	}

	public class NokiaMessage {
		String protocol;
		String type;
		String contact;
		String dateTime;
		String body;

		@Override
		public String toString() {
			return "NokiaMessage [protocol=" + this.protocol + ", type=" + this.type + ", contact=" + this.contact
					+ ", dateTime=" + this.dateTime + ", body=" + this.body + "]";
		}
	}

	public enum Position {
		PROTOCOL, TYPE, CONTACT, SOMETHING_1, SOMETHING_2, DATE_TIME, SOMETHING_3, BODY;

		public boolean hasNext() {
			return this != BODY;
		}

		public Position next() {
			if (this == BODY)
				throw new RuntimeException("No next for position " + this);
			return values()[ordinal() + 1];
		}
	}
}
