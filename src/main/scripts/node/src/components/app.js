import React from "react";
import Header from "./layout/header";

class App extends React.Component {

    render() {
        return (
            <div>
                <Header socketClosed={this.props.socketClosed} />
                <div className="container">
                    {this.props.children}
                </div>
            </div>
        )
    }
}

export default App;