import React from "react";
import Header from "./layout/header";

class App extends React.Component {

    render() {
        return (
            <div>
                <Header />
                <div className="container">
                    {this.props.children}
                </div>
            </div>
        )
    }
}

export default App;