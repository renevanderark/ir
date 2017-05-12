import React from "react";
import RepositoryForm from "./repository-form";

class NewRepository extends React.Component {
    shouldComponentUpdate(nextProps) {
        return nextProps.underEdit.name !== this.props.underEdit.name ||
            nextProps.underEdit.url !== this.props.underEdit.url ||
            nextProps.underEdit.set !== this.props.underEdit.set ||
            nextProps.underEdit.metadataPrefix !== this.props.underEdit.metadataPrefix ||
            nextProps.underEdit.dateStamp !== this.props.underEdit.dateStamp ||
            nextProps.validationResultsUnderEdit !== this.props.validationResultsUnderEdit
    }

    render() {
        const { onValidateNewRepository, validationResultsUnderEdit, onSaveRepository } = this.props;

        return (
            <RepositoryForm
                onValidateNewRepository={onValidateNewRepository}
                onSaveRepository={onSaveRepository}
                validationResultsUnderEdit={validationResultsUnderEdit}
                underEdit={this.props.underEdit} />
        );
    }
}

export default NewRepository;