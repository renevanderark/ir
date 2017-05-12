import React from "react";
import PropTypes from "prop-types";

class ValidationMarker extends React.Component {

    render() {
        const { validates, messageOk, messageFail } = this.props;

        return typeof validates === 'undefined'
            ? null
            : validates
                ? <span title={messageOk} className="glyphicon glyphicon-ok pull-right"
                        style={{color: "green", cursor: "pointer"}} />
                : <span title={messageFail} className="glyphicon glyphicon-remove pull-right"
                        style={{color: "red", cursor: "pointer"}} />;
    }
}

ValidationMarker.propTypes = {
    validates: PropTypes.bool,
    messageOk: PropTypes.string.isRequired,
    messageFail: PropTypes.string.isRequired
};

export default ValidationMarker;