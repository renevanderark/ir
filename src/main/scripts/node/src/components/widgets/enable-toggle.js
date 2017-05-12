import React from "react";
import PropTypes from "prop-types";

class EnableToggle extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.enabled !== nextProps.enabled;
    }

    render () {
        const {enabled, onEnableClick, onDisableClick} = this.props;
        return enabled ? (
            <div className="badge" style={{padding: 2, borderRadius: 4}}>
                <div style={{
                    display: "inline-block",
                    position: "relative",
                    marginRight: 4,
                    marginLeft: 4,
                    top: 1,
                    color: "#6e6",
                    textShadow: "0px 1px 0px rgba(255,255,255,.3), 0px -1px 0px rgba(0,0,0,.7)"
                }}>
                    Aan
                </div>
                <button className="btn btn-danger btn-xs" onClick={onDisableClick}
                        style={{
                            opacity: ".9",
                            borderTopLeftRadius: "0",
                            borderBottomLeftRadius: "0",
                            boxShadow: "0px 1px 1px #aaa5, inset 0px 1px 1px #fff5"
                        }}>
                    Uitzetten
                </button>
            </div>
        ) : (
            <div className="badge" style={{padding: 2, borderRadius: 4}}>
                <div style={{
                    display: "inline-block",
                    position: "relative",
                    marginRight: 4,
                    marginLeft: 4,
                    top: 1,
                    color: "#faa",
                    textShadow: "0px 1px 0px rgba(128,128,128,.4), 0px -1px 0px rgba(0,0,0,.7)"
                }}>
                    Uit
                </div>
                <button className="btn btn-success btn-xs" onClick={onEnableClick}
                        style={{
                            opacity: ".9",
                            borderTopLeftRadius: "0",
                            borderBottomLeftRadius: "0",
                            boxShadow: "0px 1px 1px #aaa5, inset 0px 1px 1px #fff5"
                        }}>
                    Aanzetten
                </button>
            </div>
        );
    }
}

EnableToggle.propTypes = {
    enabled: PropTypes.bool.isRequired,
    onEnableClick: PropTypes.func.isRequired,
    onDisableClick: PropTypes.func.isRequired,
};

export default EnableToggle;