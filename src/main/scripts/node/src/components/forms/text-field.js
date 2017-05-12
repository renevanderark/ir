import React from "react";
import PropTypes from "prop-types";

const TextField = ({label, value, onChange, children}) => (
    <div className="form-group row">
        <label className="col-md-4">{label}</label>
        <div className="col-md-28">
            <div className={`${children ? "input-group" : ""}`}>
                <input className="form-control" type="text" value={value} onChange={onChange} />
                { children ? (
                    <span className="input-group-addon">
                        {children}
                    </span>
                ) : null}
            </div>
        </div>
    </div>
);

TextField.propTypes = {
    label: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired
};

export default TextField;