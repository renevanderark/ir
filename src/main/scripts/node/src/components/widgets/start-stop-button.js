import React from "react";
import {RunState} from "../../etc/enums";
import PropTypes from "prop-types";

class StartStopButton extends React.Component {

    render() {
        switch (this.props.runState) {
            case RunState.RUNNING:
                return (
                    <button className="btn btn-default pull-right" onClick={this.props.onStopClick}>
                        <span style={{color: "#aa0000"}} className="glyphicon glyphicon-stop" />
                    </button>
                );
            case RunState.QUEUED:
                return (
                    <button className="btn btn-default pull-right" onClick={this.props.onStopClick}>
                        <span className="glyphicon glyphicon-time" />
                    </button>
                );
            case RunState.INTERRUPTED:
                return (
                    <button className="btn btn-default pull-right" disabled={true}>
                        <span className="glyphicon glyphicon-play" />
                    </button>
                );
            case RunState.WAITING:
            default:
                return (
                    <button className="btn btn-default pull-right" disabled={this.props.disabled}
                            onClick={this.props.onStartClick} >
                        <span className="glyphicon glyphicon-play" />
                    </button>
                );
        }
    }
}

StartStopButton.propTypes = {
    runState: PropTypes.string.isRequired,
    disabled: PropTypes.bool.isRequired,
    onStopClick: PropTypes.func.isRequired,
    onStartClick: PropTypes.func.isRequired
};

export default StartStopButton;