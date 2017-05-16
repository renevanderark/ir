import React from "react";
import Panel from "../layout/panel";

import { Link } from "react-router";
import { urls } from "../../router";
import RepositoryRow from "./repository-row";

class Repositories extends React.Component {

    render() {
        return (
            <Panel title="Harvest definities">
                <p>
                    <Link to={urls.newRepository()}>
                        <span className="glyphicon glyphicon-plus-sign" />{" "}
                        Nieuwe harvest definitie toevoegen
                    </Link>
                </p>
                <table className="table">
                    <thead>
                        <tr>
                            <th>
                                <span className="glyphicon glyphicon-refresh"
                                      style={{cursor: "pointer"}}
                                      onClick={() => this.props.onRefetchRepositories()}
                                />
                            </th>
                            <th>Naam</th>
                            <th>Set</th>
                            <th>Laatste datestamp</th>
                            <th>Harvest schema</th>
                            <th colSpan={2}>
                                Actief
                            </th>
                        </tr>
                    </thead>
                    <tbody>

                    {this.props.repositories.list.map((repository, i) => (
                        <RepositoryRow key={i}
                                       onEnableRepository={this.props.onEnableRepository}
                                       onDisableRepository={this.props.onDisableRepository}
                                       onSetSchedule={this.props.onSetSchedule}
                                       onStartHarvest={this.props.onStartHarvest}
                                       onInterruptHarvest={this.props.onInterruptHarvest}
                                       repository={repository} />
                    ))}
                    </tbody>
                </table>
            </Panel>
        );
    }
}

export default Repositories;