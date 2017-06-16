import React from "react";

class FindField extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            query: ""
        }
    }

    onChange(ev) {
        this.setState({query: ev.target.value});

        if (ev.target.value.length > 4) {
            this.props.onFindRecords(ev.target.value);
        }
    }

    render() {
        const { query } = this.state;
        return (
            <div>
                <input className="form-control" type="text" value={query} onChange={this.onChange.bind(this)} />

            </div>
        );
    }
}

export default FindField;