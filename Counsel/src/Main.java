

import java.util.Scanner;

import envelope.DigitalEnvelope.EnvelopeContent;
import people.Alice;
import people.Bob;
import people.Charlie;

public class Main {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		Alice alice = new Alice();
		Bob bob = new Bob();
		Charlie charlie = new Charlie();
		EnvelopeContent env =  alice.sendConsentToBob(/*...*/);

		bob.receiveConsent(env);
		
		EnvelopeContent env2 =   bob.sendMedicalRecord(/*...*/);

		alice.receiveRecord(env2);
			
		EnvelopeContent env3 =  alice.sendToCharlie(/*...*/);

		charlie.receive(env3);
		
		sc.close();
	}

}
