import React from "react";
import PropTypes from "prop-types";

class EnableToggle extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.enabled !== nextProps.enabled || this.props.toggleEnabled !== nextProps.toggleEnabled;
    }

    render () {
        const {enabled, toggleEnabled, onEnableClick, onDisableClick} = this.props;
        return enabled ? (
            <div className="badge" style={{padding: 2, borderRadius: 4}}>
                <div className="enable-toggle">
                    Aan
                </div>
                <button className="btn btn-danger btn-xs" onClick={onDisableClick}
                        disabled={!toggleEnabled}>
                    Uitzetten
                </button>
            </div>
        ) : (
            <div className="badge" style={{padding: 2, borderRadius: 4}}>
                <div className="enable-toggle off">
                    Uit
                </div>
                <button className="btn btn-success btn-xs" onClick={onEnableClick}
                        disabled={!toggleEnabled}>
                    Aanzetten
                </button>
            </div>
        );
    }
}

EnableToggle.propTypes = {
    enabled: PropTypes.bool.isRequired,
    toggleEnabled: PropTypes.bool.isRequired,
    onEnableClick: PropTypes.func.isRequired,
    onDisableClick: PropTypes.func.isRequired,
};

export default EnableToggle;