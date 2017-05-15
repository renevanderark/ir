import React from "react";
import {RunState} from "../../enums";
import PropTypes from "prop-types";

class StartStopButton extends React.Component {

    render() {
        return this.props.runState === RunState.RUNNING
            ? (<button className="btn btn-default pull-right" onClick={this.props.onStopClick}>
                <span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right" disabled={this.props.runState === RunState.INTERRUPTED} onClick={this.props.onStartClick}>
                <span className="glyphicon glyphicon-play" /></button>);

    }
}

StartStopButton.propTypes = {
    runState: PropTypes.string.isRequired,
    onStopClick: PropTypes.func.isRequired,
    onStartClick: PropTypes.func.isRequired
};

export default StartStopButton;