package ch.eitchnet.converter.nokiaandroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactConverter {
	private static final Logger logger = LoggerFactory.getLogger(ContactConverter.class);

	public void convert(File input, File output) {

		int lineNr = 0;
		String line = "";
		try (BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(input), "ISO-8859-1"));
				BufferedWriter fout = new BufferedWriter(new FileWriter(output))) {

			line = fin.readLine();
			if (line == null)
				throw new RuntimeException("No first line found! Can't evaluate column names!");
			String firstLine = line;
			lineNr++;

			// "Title";"First name";"Middle name";"Last name";"Suffix";"Job title";
			// "Company";"Birthday";"SIP address";"Push-to-talk";"Share view";"User ID";
			// "Notes";"General mobile";"General phone";"General e-mail";"General fax";
			// "General video call";"General web address";"General VOIP address";"General P.O.Box";
			// "General extension";"General street";"General postal/ZIP code";"General city";
			// "General state/province";"General country/region";"Home mobile";"Home phone";"Home e-mail";
			// "Home fax";"Home video call";"Home web address";"Home VOIP address";"Home P.O.Box";
			// "Home extension";"Home street";"Home postal/ZIP code";"Home city";"Home state/province";
			// "Home country/region";"Business mobile";"Business phone";"Business e-mail";"Business fax";
			// "Business video call";"Business web address";"Business VOIP address";"Business P.O.Box";
			// "Business extension";"Business street";"Business postal/ZIP code";"Business city";
			// "Business state/province";"Business country/region";""

			String[] columnNames = firstLine.split(";");

			for (int i = 0; i < columnNames.length; i++) {
				columnNames[i] = columnNames[i].replaceAll("\"", "");
				logger.info("Found column name: " + columnNames[i]);
			}

			while ((line = fin.readLine()) != null) {
				lineNr++;

				NokiaContact nk = new NokiaContact();

				String[] values = line.split(";");
				for (int i = 0; i < columnNames.length; i++) {
					String columnName = columnNames[i];
					String value = values[i].replaceAll("\"", "").trim();
					if (value.isEmpty()) {
						continue;
					}

					if (columnName.equals("First name")) {
						nk.firstName = value;
					} else if (columnName.equals("Last name")) {
						nk.lastName = value;
					} else if (columnName.equals("General mobile")) {
						nk.mobile = value;
					} else if (columnName.equals("General phone")) {
						nk.phone = value;
					} else if (columnName.equals("Home phone")) {
						nk.phone = value;
					} else {
						throw new RuntimeException("Unhandled column: " + columnName);
					}
				}

				if (nk.firstName.isEmpty() && nk.lastName.isEmpty()) {
					logger.info("Skipping contact on line " + lineNr + " both firstname and lastname are empty!");
					continue;
				}

				fout.write("BEGIN:VCARD\n");
				fout.write("VERSION:2.1\n");
				if (nk.firstName.isEmpty())
					fout.write("FN:" + nk.lastName + "\n");
				else if (nk.lastName.isEmpty())
					fout.write("FN:" + nk.firstName + "\n");
				else
					fout.write("FN:" + nk.firstName + " " + nk.lastName + "\n");
				fout.write("N:" + nk.lastName + ";" + nk.firstName + "\n");

				if (!nk.homePhone.isEmpty())
					fout.write("TEL;HOME:" + nk.homePhone + "\n");
				if (!nk.phone.isEmpty())
					fout.write("TEL;HOME:" + nk.phone + "\n");
				if (!nk.mobile.isEmpty())
					fout.write("TEL;CELL:" + nk.mobile + "\n");

				fout.write("END:VCARD\n\n");
				logger.info("Wrote: " + nk);
			}

			logger.info("Parsed " + (lineNr - 1) + " contacts and wrote VCF to " + output.getAbsolutePath());

		} catch (Exception e) {
			if (output.exists())
				output.delete();
			throw new RuntimeException("Failed reading from " + input + " at line " + lineNr + ": " + line, e);
		}
	}

	public class NokiaContact {
		private String firstName = "";
		private String lastName = "";
		private String mobile = "";
		private String phone = "";
		private String homePhone = "";

		@Override
		public String toString() {
			return "NokiaContact [firstName=" + this.firstName + ", lastName=" + this.lastName + ", mobile="
					+ this.mobile + ", phone=" + this.phone + ", homePhone=" + this.homePhone + "]";
		}
	}
}
