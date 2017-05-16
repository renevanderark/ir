const RunState = {
    WAITING: "WAITING",
    RUNNING: "RUNNING",
    INTERRUPTED: "INTERRUPTED"
};

const Schedule = {
    DAILY: {label: "Dagelijks", enumValue: 0},
    WEEKLY: {label: "Wekelijks", enumValue: 1},
    MONTHLY: {label: "Maandelijks", enumValue: 2}
};

export { RunState, Schedule }