import React from "react";
import Panel from "../layout/panel";
import EnableToggle from "../widgets/enable-toggle";

const Schedule = {
    DAILY: {label: "Dagelijks", enumValue: 0},
    WEEKLY: {label: "Wekelijks", enumValue: 1},
    MONTHLY: {label: "Maandelijks", enumValue: 2}
};

class Repositories extends React.Component {

    render() {
        return (
            <Panel title="Harvest definities">

                <table className="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Naam</th>
                            <th>Set</th>
                            <th>Laatste datestamp</th>
                            <th>Harvest schema</th>
                            <th>Actief</th>
                        </tr>
                    </thead>
                    <tbody>
                    {this.props.repositories.list.map((repo, i) => (
                        <tr key={i}>
                            <td>{repo.id}</td>
                            <td>{repo.name}</td>
                            <td>{repo.set}</td>
                            <td>{repo.dateStamp || "- nog niet geharvest -"}</td>
                            <td>
                                <select value={Schedule[repo.schedule].enumValue}
                                        onChange={(ev) => this.props.onSetSchedule(repo.id, ev.target.value)}
                                        className="form-control">

                                    <option value={Schedule.DAILY.enumValue}>{Schedule.DAILY.label}</option>
                                    <option value={Schedule.WEEKLY.enumValue}>{Schedule.WEEKLY.label}</option>
                                    <option value={Schedule.MONTHLY.enumValue}>{Schedule.MONTHLY.label}</option>
                                </select>
                            </td>
                            <td>
                                <EnableToggle enabled={repo.enabled}
                                              onEnableClick={() => this.props.onEnableRepository(repo.id)}
                                              onDisableClick={() => this.props.onDisableRepository(repo.id)} />
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </Panel>
        );
    }
}

export default Repositories;