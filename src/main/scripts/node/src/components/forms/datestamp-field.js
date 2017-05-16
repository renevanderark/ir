import React from "react";
import PropTypes from "prop-types";

const Parts = {
    YEAR: 0, MON: 1, DAY: 2, HOURS: 3, MIN: 4, SEC: 5
};


const validate = (value) =>
    value === null || (
        !isNaN(Date.parse(value)) &&
        value.match(/^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$/) !== null
    );

const validateYear = (year) =>
    year.match(/^[0-9]{4}$/) !== null;

const validateOther = (value, minValue, maxValue) =>
    parseInt(value, 10) >= minValue &&
    parseInt(value, 10) <= maxValue &&
    value.match(/^[0-9]{2}$/) !== null;

class DatestampField extends React.Component {

    onChange(partIdx, ev) {
        const { value } = this.props;
        const [year, mon, day, hours, minutes, seconds] = value
            .split(/[-T:Z]/g)
            .map((val, idx) => partIdx === idx ? ev.target.value : val);

        const validLength = partIdx === Parts.YEAR ? 4 : 2;

        if (ev.target.value.length <= validLength && ev.target.value.match(/^[0-9]*$/)) {
            this.props.onChange({target: {value: `${year}-${mon}-${day}T${hours}:${minutes}:${seconds}Z`}});
        }
    }

    generateDefaultDatestamp() {
        this.props.onChange({target: {value: "2000-01-01T00:00:00Z"}});
    }

    render() {
        const { label, value } = this.props;

        if (value === null) {
            return (<div className="form-group row">
                <label className="col-md-4 col-sm-32 col-xs-32">
                    {label}
                </label>
                <button className="btn btn-default" onClick={this.generateDefaultDatestamp.bind(this)}>Set datestamp</button>
            </div>);
        }

        const [year, mon, day, hours, minutes, seconds] = value.split(/[-T:Z]/g);
        return (
            <div className="form-group row">
                <label className="col-md-4 col-sm-32 col-xs-32" style={{color: validate(value) ? "#333" : "red"}}>
                    {label}
                </label>
                <div className="col-md-2 col-sm-3 col-xs-3">
                    <input className="form-control" type="text" value={year}
                           onChange={this.onChange.bind(this, Parts.YEAR)}
                           onFocus={(ev) => ev.target.select()}
                           style={{ borderColor: validateYear(year) ? "#ccc" : "red"}}
                    />
                </div>
                <div className="col-md-1 col-sm-2 col-xs-2">
                    <input className="form-control" type="text" value={mon}
                           style={{borderColor: validateOther(mon, 1, 12) ? "#ccc" : "red", paddingRight: 6, paddingLeft: 6}}
                           onChange={this.onChange.bind(this, Parts.MON)}
                           onFocus={(ev) => ev.target.select()}
                    />
                </div>
                <div className="col-md-1 col-sm-2 col-xs-2">
                    <input className="form-control" type="text" value={day}
                           style={{borderColor: validateOther(day, 1, 31) ? "#ccc" : "red",paddingRight: 6, paddingLeft: 6}}
                           onChange={this.onChange.bind(this, Parts.DAY)}
                           onFocus={(ev) => ev.target.select()}
                    />
                </div>
                <div className="col-md-1 col-sm-1 col-xs-1" />
                <div className="col-md-1 col-sm-2 col-xs-2">
                    <input className="form-control" type="text" value={hours}
                           style={{borderColor: validateOther(hours, 0, 23) ? "#ccc" : "red", paddingRight: 6, paddingLeft: 6}}
                           onChange={this.onChange.bind(this, Parts.HOURS)}
                           onFocus={(ev) => ev.target.select()}
                    />
                </div>
                <div className="col-md-1 col-sm-2 col-xs-2">
                    <input className="form-control" type="text" value={minutes}
                           style={{borderColor: validateOther(minutes, 0, 59) ? "#ccc" : "red", paddingRight: 6, paddingLeft: 6}}
                           onChange={this.onChange.bind(this, Parts.MIN)}
                           onFocus={(ev) => ev.target.select()}
                    />
                </div>
                <div className="col-md-1 col-sm-2 col-xs-2">
                    <input className="form-control" type="text" value={seconds}
                           style={{borderColor: validateOther(seconds, 0, 59) ? "#ccc" : "red", paddingRight: 6, paddingLeft: 6}}
                           onChange={this.onChange.bind(this, Parts.SEC)}
                           onFocus={(ev) => ev.target.select()}
                    />
                </div>
                <div className="col-md-1 col-sm-1 col-xs-1" />
                <button className="btn btn-danger" onClick={() => this.props.onChange({target: {value: null}})}>
                    Verwijder datestamp
                </button>
            </div>
        );
    }
}

DatestampField.propTypes = {
    label: PropTypes.string.isRequired,
    value: PropTypes.string,
    onChange: PropTypes.func.isRequired
};

export default DatestampField;
export { validate as validateDateStamp };