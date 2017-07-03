package nl.kb.dare.identifierharvester;

public class IdentifierHarvesterException extends Exception {
    IdentifierHarvesterException(String message, Exception ex) {
        super(message, ex);
    }
}
