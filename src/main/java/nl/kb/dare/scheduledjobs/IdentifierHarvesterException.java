package nl.kb.dare.scheduledjobs;

class IdentifierHarvesterException extends Exception {
    IdentifierHarvesterException(String message, Exception ex) {
        super(message, ex);
    }
}
