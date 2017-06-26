import React from "react";
import Panel from "../layout/panel";

import { Link } from "react-router";
import { urls } from "../../etc/urls";
import RepositoryRow from "./repository-row";
import EnableToggle from "../widgets/enable-toggle";
import {FetcherRunState, RunState} from "../../etc/enums";
import FindField from "../forms/find-field";

class Repositories extends React.Component {

    render() {
        return (
            <Panel title="Harvest definities">
                <div>
                    <span className="col-md-6">
                        Publicatie opzoeken
                    </span>

                    <div className="col-md-16">
                        <FindField recordList={this.props.recordList}
                                   repositories={this.props.repositories.list}
                                   onFindRecords={this.props.onFindRecords}
                                   processStatuses={this.props.processStatuses}
                                   onClearFoundRecords={this.props.onClearFoundRecords}
                                   onNavigateTo={this.props.onNavigateTo}
                        />
                    </div>
                    <div className="clearfix" />
                </div>
                <hr />
                <div>
                    <span className="col-md-6">
                        Object Harvester ({this.props.fetcherRunstate}){" "}
                    </span>
                    <EnableToggle
                        enabled={this.props.fetcherRunstate === FetcherRunState.RUNNING || this.props.fetcherRunstate === FetcherRunState.DISABLING}
                        toggleEnabled={this.props.fetcherRunstate !== FetcherRunState.DISABLING}
                        onEnableClick={this.props.onStartOaiRecordFetcher}
                        onDisableClick={this.props.onDisableOaiRecordFetcher}/>
                </div>
                <hr />
                <Link to={urls.newRepository()}>
                    <span className="glyphicon glyphicon-plus-sign" />{" "}
                    Nieuwe harvest definitie toevoegen
                </Link>
                <hr />

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
                            <th>Wachtrij</th>
                            <th>Downloaden</th>
                            <th>Fout</th>
                            <th>Download klaar</th>
                            <th>In preproces</th>
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
                                       recordStatus={this.props.repositories.recordStatus[repository.id] || {}}
                                       repository={repository}
                                       runState={this.props.repositories.harvesterRunStates[repository.id]
                                        || {runState: RunState.WAITING}}
                        />
                    ))}
                    </tbody>
                </table>
            </Panel>
        );
    }
}

export default Repositories;