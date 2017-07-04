import React from "react";

class UnsafeQuery extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            q: ""
        }
    }

    render() {
        const {data, error} = this.props;

        const errDiv = error ? <div>Er zat een fout in de query</div> : null;

        const keys = data.length ? Object.keys(data[0]) : [];
        const result = data.length ? (
            <table>
                <thead>
                    <tr>
                        {keys.map(key => (
                            <th key={key} style={{padding: "1em"}}>
                                {key.toUpperCase()}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {data.map((d, i) => (
                        <tr key={i}  style={{border: "1px solid #aaa"}}>
                            {keys.map(k => (
                                <td key={k} style={{padding: "1em"}}>
                                    {d[k]}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>

        ): null;


        return (
            <div>
                <textarea value={this.state.q}
                          onChange={(ev) => this.setState({q: ev.target.value})} />
                <button onClick={() => this.props.submitUnsafeQuery(this.state.q)}>
                    Query
                </button>
                {errDiv}
                {result}
            </div>
        );
    }
}

export default UnsafeQuery;