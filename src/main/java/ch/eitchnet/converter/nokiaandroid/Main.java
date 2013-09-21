package ch.eitchnet.converter.nokiaandroid;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {

		File contactInputFile = new File("data/nokia_contacts.csv"); //$NON-NLS-1$
		File contactOutputFile = new File("target/converted_contacts.vcf"); //$NON-NLS-1$
		File messagesInputFile = new File("data/nokia_sms.csv"); //$NON-NLS-1$
		File messagesOutputFile = new File("target/converted_sms.xml"); //$NON-NLS-1$

		ContactConverter contactConverter = new ContactConverter();
		contactConverter.convert(contactInputFile, contactOutputFile);

		MessageConverter messageConverter = new MessageConverter();
		messageConverter.convert(messagesInputFile, messagesOutputFile);
	}
}
