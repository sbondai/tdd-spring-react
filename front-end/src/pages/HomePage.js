import React from "react";
import { connect } from "react-redux";
import HoaxFeed from "../components/HoaxFeed";
import HoaxSubmit from "../components/HoaxSubmit";
import UserList from "../components/UserList";

class HomePage extends React.Component {
  render() {
    return (
      <div data-testid="homepage">
        <div className="row">
          <div className="col-8">
            {this.props.loggedInUser.isLoggedIn && <HoaxSubmit />}
            <HoaxFeed />
          </div>
          <div className="col-4">
            <UserList />
          </div>
        </div>
      </div>
    );
  }
}
const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};
export default connect(mapStateToProps)(HomePage);
