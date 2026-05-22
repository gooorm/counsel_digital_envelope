
import envelope.DigitalEnvelope.EnvelopeContent;
import people.Alice;
import people.Bob;
import people.Charlie;

public class Main {

	public static void main(String[] args) {

		Alice alice = new Alice();
		Bob bob = new Bob();
		Charlie charlie = new Charlie();

		// Phase 2: Alice → Bob (동의서)
		EnvelopeContent env = alice.sendConsentToBob(bob.getPublicKey());
		bob.receiveConsent(env);

		// Phase 3: Bob → Alice (의료기록)
		EnvelopeContent env2 = bob.sendMedicalRecord(alice.getPublicKey());
		alice.receiveRecord(env2);

		// Phase 4: Alice → Charlie (이중서명 전달)
		EnvelopeContent env3 = alice.sendToCharlie(charlie.getPublicKey());
		charlie.receive(env3);
	}
}
