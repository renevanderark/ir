import React from "react";
import EnableToggle from "../widgets/enable-toggle";
import { RunState } from "../../enums";

const Schedule = {
    DAILY: {label: "Dagelijks", enumValue: 0},
    WEEKLY: {label: "Wekelijks", enumValue: 1},
    MONTHLY: {label: "Maandelijks", enumValue: 2}
};


class RepositoryRow extends React.Component {

    render() {
        const { repository } = this.props;

        return (
            <tr>
                <td>{repository.id}</td>
                <td>{repository.name}</td>
                <td>{repository.set}</td>
                <td>{repository.dateStamp || "- nog niet geharvest -"}</td>
                <td>
                    <select value={Schedule[repository.schedule].enumValue}
                            disabled={repository.runState !== RunState.WAITING}
                            onChange={(ev) => this.props.onSetSchedule(repository.id, ev.target.value)}
                            className="form-control">

                        <option value={Schedule.DAILY.enumValue}>{Schedule.DAILY.label}</option>
                        <option value={Schedule.WEEKLY.enumValue}>{Schedule.WEEKLY.label}</option>
                        <option value={Schedule.MONTHLY.enumValue}>{Schedule.MONTHLY.label}</option>
                    </select>
                </td>
                <td>
                    <EnableToggle enabled={repository.enabled}
                                  toggleEnabled={repository.runState === RunState.WAITING}
                                  onEnableClick={() => this.props.onEnableRepository(repository.id)}
                                  onDisableClick={() => this.props.onDisableRepository(repository.id)} />
                </td>
                <td>
                    {repository.runState}
                </td>
            </tr>
        );
    }
}

export default RepositoryRow;