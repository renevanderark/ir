import React from "react";
import Panel from "../layout/panel";
import ButtonWithModalWarning from "../modals/button-with-modal-warning";

class RecordStatus extends React.Component {

    componentWillReceiveProps(nextProps) {
        const { onFetchRecord } = this.props;

        if (nextProps.kbObjId !== this.props.kbObjId) {
            onFetchRecord(nextProps.kbObjId);
        }
    }

    componentDidMount() {
        const {record, kbObjId, onFetchRecord } = this.props;

        if (!record || record.kbObjId !== kbObjId) {
            onFetchRecord(kbObjId);
        }
    }


    render() {
        const { record, errorReport, processStatuses, repositories, errorStatuses, onReset } = this.props;
        if (!record) { return null; }

        const errorReportPanel = errorReport
            ? (
                <Panel title="Foutrapport">
                    <div className="row">
                        <label className="col-md-6">Statuscode</label>
                        <div className="col-md-26">
                            {errorReport.statusCode} - {errorStatuses[errorReport.statusCode] || ""}
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Melding</label>
                        <div className="col-md-26">
                            {errorReport.message}
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">URL</label>
                        <div className="col-md-26">
                            <a href={errorReport.url} target="_blank">
                                {errorReport.url}
                            </a>
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Stacktrace</label>
                        <pre className="col-md-26">
                            {errorReport.stackTrace}
                        </pre>
                    </div>
                </Panel>
            )
            : null;

        const downloadDiv = record.state > 10 && record.state < 1000 ? (
            <div className="row">
                <label className="col-md-6">Download als ZIP</label>
                <div className="col-md-26">
                    <a href={`/records/download/${record.kbObjId}`}>
                        Downloaden <span className="glyphicon glyphicon-download-alt" />
                    </a>
                </div>
            </div>
        ) : null;

        const resetButton = processStatuses[record.state] === "failure" ? (
                <ButtonWithModalWarning
                    className="btn btn-default" label="Terugzetten in wachtrij"
                    onConfirm={(doClose) => {
                        onReset(record.kbObjId)
                        doClose();
                    }}>

                    Weet u zeker dat u deze publicatie wilt terugzetten in de wachtrij?
                </ButtonWithModalWarning>
            ) : null;

        return (
            <div>
                <Panel title="Overzicht IP">
                    <div className="row">
                        <label className="col-md-6">OAI/PMH identifier</label>
                        <div className="col-md-20">
                            <a target="_blank"
                               href={`http://oai.gharvester.dans.knaw.nl/?verb=GetRecord&metadataPrefix=nl_didl_norm&identifier=${encodeURIComponent(record.oaiIdentifier)}`}>
                                {record.oaiIdentifier}
                            </a>
                        </div>
                        <div className="col-md-6">
                            {resetButton}
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">KB object identifier</label>
                        <div className="col-md-26">
                            {record.kbObjId}
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Status</label>
                        <div className="col-md-26">
                            {processStatuses[record.state]}
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Bron</label>
                        <div className="col-md-26">
                            {repositories.find(repo => repo.id === record.repositoryId).name}
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Aangemaakt</label>
                        <div className="col-md-26">
                            {record.tsCreate}
                        </div>
                    </div>
                    <div className="row">
                        <label className="col-md-6">Verwerkt</label>
                        <div className="col-md-26">
                            {record.tsProcessed || "-"}
                        </div>
                    </div>
                    {downloadDiv}

                </Panel>
                {errorReportPanel}
            </div>
        );
    }
}

export default RecordStatus;