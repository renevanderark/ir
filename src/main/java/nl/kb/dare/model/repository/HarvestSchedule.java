package nl.kb.dare.model.repository;

public enum HarvestSchedule {
    DAILY(0), WEEKLY(1), MONTHLY(2);

    private final Integer code;

    HarvestSchedule(Integer code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static HarvestSchedule forCode(final int code) {
        for (HarvestSchedule s : HarvestSchedule.values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }
}
