import React from "react";
import EnableToggle from "../widgets/enable-toggle";
import { RunState } from "../../enums";
import StartStopButton from "../widgets/start-stop-button";

import { Link } from "react-router";
import { urls } from "../../router";

import { Schedule } from "../../enums";


class RepositoryRow extends React.Component {

    render() {
        const { repository } = this.props;

        return (
            <tr>
                <td>{repository.id}</td>
                <td>{repository.name}</td>
                <td>{repository.set}</td>
                <td>{repository.dateStamp || "- nog niet geharvest -"}</td>
                <td>{Schedule[repository.schedule].label}</td>
                <td>
                    <EnableToggle enabled={repository.enabled}
                                  toggleEnabled={repository.runState === RunState.WAITING}
                                  onEnableClick={() => this.props.onEnableRepository(repository.id)}
                                  onDisableClick={() => this.props.onDisableRepository(repository.id)} />
                </td>
                <td>
                    <Link to={urls.editRepository(repository.id)} className="btn btn-default">
                        <span className="glyphicon glyphicon-edit" />
                    </Link>
                    <StartStopButton runState={repository.runState}
                                     disabled={!repository.enabled}
                                     onStopClick={() => this.props.onInterruptHarvest(repository.id)}
                                     onStartClick={() => this.props.onStartHarvest(repository.id)}/>
                </td>
            </tr>
        );
    }
}

export default RepositoryRow;