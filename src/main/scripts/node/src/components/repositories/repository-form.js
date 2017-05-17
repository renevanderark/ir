import React from "react";
import Panel from "../layout/panel";
import TextField from "../forms/text-field";
import DatestampField from "../forms/datestamp-field";
import ValidationMarker from "../widgets/validation-marker";
import { validateDateStamp } from "../forms/datestamp-field";
import ButtonWithModalWarning from "../modals/button-with-modal-warning";
import { Schedule } from "../../enums";

class RepositoryForm extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            repository: props.underEdit,
            changed: false
        };
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            changed: false,
            repository: nextProps.underEdit
        });
    }


    componentDidMount() {
        this.setState({
            changed: false,
            repository: this.props.underEdit
        });
    }

    onChange(field, ev) {
        this.setState({
            changed: true,
            repository: {
                ...this.state.repository,
                [field]: ev.target.value
            }
        });
    }

    render() {

        const { repository, changed } = this.state;

        const { onDeleteRepository, onValidateNewRepository, validationResultsUnderEdit, onSaveRepository } = this.props;

        const {urlIsValidOAI, setExists, metadataFormatSupported } = validationResultsUnderEdit;

        if (!repository) { return null; }

        const allowedToSave =
            !repository.enabled &&
            repository.name &&
            validateDateStamp(repository.dateStamp) &&
            !changed &&
            urlIsValidOAI &&
            setExists &&
            metadataFormatSupported;

        const deleteButton = repository.id ? (
            <ButtonWithModalWarning
                className="btn btn-danger pull-right"
                label="Verwijderen"
                onConfirm={() => onDeleteRepository(repository.id)}>
                <p>
                    Het verwijderen van een harvest definitie veroorzaakt ook een verwijdering van alle onverwerkte
                    publicaties.
                </p>
                <p>Weet u zeker dat u deze harvest definitie wilt verwijderen?</p>
            </ButtonWithModalWarning>
        ) : null;

        return repository.enabled
            ? (<Panel title={repository.id ? "Harvest definitie bewerken" : "Nieuwe harvest definitie"}>
                <p>* Op dit moment staat deze harvest definitie aan,
                    om hem te mogen bewerken moet de harvest definitie eerst worden uitgeschakeld.</p>
            </Panel>)
            : (<Panel title={repository.id ? "Harvest definitie bewerken" : "Nieuwe harvest definitie"}>
                <TextField label="Naam" value={repository.name} onChange={this.onChange.bind(this, "name")} />
                <TextField label="Url" value={repository.url} onChange={this.onChange.bind(this, "url")}>
                    <ValidationMarker validates={urlIsValidOAI}
                                      messageOk="Url is a valid OAI endpoint" messageFail="Url is not a valid OAI endpoint" />
                </TextField>
                <TextField label="Set" value={repository.set} onChange={this.onChange.bind(this, "set")}>
                    <ValidationMarker validates={setExists}
                                      messageOk="Set exists in this repository"
                                      messageFail="Set does not exist in this repository" />
                </TextField>
                <TextField label="Metadata prefix" value={repository.metadataPrefix} onChange={this.onChange.bind(this, "metadataPrefix")}>
                    <ValidationMarker validates={metadataFormatSupported}
                                      messageOk="Metadata format is supported by this repository"
                                      messageFail="Metadata format is not supported by this repository"
                    />
                </TextField>
                <DatestampField label="Datestamp" value={repository.dateStamp} onChange={this.onChange.bind(this, "dateStamp")} />
                <div className="form-group row">
                    <label className="col-md-4 col-sm-32 col-xs-32">
                        Schema
                    </label>
                    <span className="col-md-28">
                        <select value={repository.schedule}
                                onChange={this.onChange.bind(this, "schedule")}
                                className="form-control">

                            <option value="DAILY">{Schedule.DAILY.label}</option>
                            <option value="WEEKLY">{Schedule.WEEKLY.label}</option>
                            <option value="MONTHLY">{Schedule.MONTHLY.label}</option>
                        </select>
                    </span>
                </div>
                <button className="btn btn-default"
                        disabled={!validateDateStamp(repository.dateStamp)}
                        onClick={() => onValidateNewRepository(repository)}>
                    Test configuratie
                </button>
                <button className="btn btn-default"
                        disabled={!allowedToSave}
                        onClick={onSaveRepository}>
                    Opslaan
                </button>

                {deleteButton}
            </Panel>
        );
    }
}

export default RepositoryForm;