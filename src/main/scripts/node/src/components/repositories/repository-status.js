import React from "react";
import Panel from "../layout/panel";
import {RunState} from "../../etc/enums";
import EnableToggle from "../widgets/enable-toggle";
import StartStopButton from "../widgets/start-stop-button";

class RepositoryStatus extends React.Component {

    render() {
        const {repository, recordStatus, errorStatus, runState: { runState } } = this.props;


        const errorTable = errorStatus.length > 0
            ? (<table className="table">
                <thead>
                    <tr>
                        <th>Foutcode</th>
                        <th>Uitleg</th>
                        <th className="text-right">Aantal fouten</th>
                    </tr>
                </thead>
                <tbody>
                    {errorStatus.map((status, i) => (
                        <tr key={i}>
                            <td>{status.code}</td>
                            <td>{status.statusText}</td>
                            <td className="text-right">{status.amount}</td>
                        </tr>
                    ))}
                </tbody>
            </table>)
            : (<i>Geen fouten aangetroffen</i>);

        return (
            <div>
                <Panel title={`Harvest definitie: ${repository.name}`}>

                    <div className="row">
                        <label className="col-md-6">Naam</label>
                        <span className="col-md-12">{repository.name}</span>
                        <div className="pull-right col-md-6">
                            <label >Harvest starten</label>
                            <StartStopButton runState={runState}
                                             disabled={!repository.enabled}
                                             onStopClick={() => this.props.onInterruptHarvest(repository.id)}
                                             onStartClick={() => this.props.onStartHarvest(repository.id)}/>
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">URL</label>
                        <span className="col-md-26">{repository.url}</span>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Set</label>
                        <span className="col-md-26">{repository.set}</span>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Metadata prefix</label>
                        <span className="col-md-26">{repository.metadataPrefix}</span>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Laatste datestamp</label>
                        <span className="col-md-26">{repository.dateStamp || "- nog niet geharvest -"}</span>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Actief</label>
                        <EnableToggle enabled={repository.enabled}
                                      toggleEnabled={runState === RunState.WAITING}
                                      onEnableClick={() => this.props.onEnableRepository(repository.id)}
                                      onDisableClick={() => this.props.onDisableRepository(repository.id)}/>
                    </div>


                </Panel>
                <div className="col-md-15">
                    <Panel title={`Verwerkingsoverzicht: ${repository.name}`}>
                        <table className="table">
                            <thead>
                                <tr><th>Status</th><th className="text-right">Aantal</th></tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Wachtrij</td><td className="text-right">{recordStatus.pending || 0}</td>
                                </tr>
                                <tr>
                                    <td>Downloaden</td><td className="text-right">{recordStatus.processing || 0}</td>
                                </tr>
                                <tr>
                                    <td>Fout</td><td className="text-right">{recordStatus.failure || 0}</td>
                                </tr>
                                <tr>
                                    <td>Download klaar</td><td className="text-right">{recordStatus.processed|| 0}</td>
                                </tr>
                            </tbody>
                        </table>
                    </Panel>
                </div>
                <div className="col-md-1" />
                <div className="col-md-16">
                    <Panel title={`Foutrapportage: ${repository.name}`}>
                        {errorTable}
                    </Panel>
                </div>
            </div>
        );
    }
}

export default RepositoryStatus;