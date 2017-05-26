import React from "react";
import Panel from "../layout/panel";

class RepositoryStatus extends React.Component {

    render() {
        const {repository, recordStatus, errorStatus } = this.props;


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
            <div className="row">
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