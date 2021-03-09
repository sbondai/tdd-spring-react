import React from "react";
import { connect } from "react-redux";
import ButtonWithProgress from "../../components/ButtonWIthProgress";
import Input from "../../components/Input";

import * as authActions from "../../redux/authActions";

export class UserSignupPage extends React.Component {
  state = {
    displayName: "",
    username: "",
    password: "",
    repassword: "",
    pendingApiCall: false,
    errors: {},
    passwordRepeatConfirmed: true,
  };

  onChangeDisplayName = (event) => {
    const value = event.target.value;
    const errors = { ...this.state.errors };
    delete errors.displayName;
    this.setState({
      displayName: value,
      errors,
    });
  };

  onChangeUsername = (event) => {
    const value = event.target.value;
    const errors = { ...this.state.errors };
    delete errors.username;
    this.setState({
      username: value,
      errors,
    });
  };

  onChangePassword = (event) => {
    const value = event.target.value;
    const passwordRepeatConfirmed = this.state.repassword === value;
    const errors = { ...this.state.errors };
    delete errors.password;
    errors.repassword = passwordRepeatConfirmed
      ? ""
      : "Does not match to password";
    this.setState({
      password: value,
      passwordRepeatConfirmed,
      errors,
    });
  };

  onChangeRePassword = (event) => {
    const value = event.target.value;
    const passwordRepeatConfirmed = this.state.password === value;
    const errors = { ...this.state.errors };
    errors.repassword = passwordRepeatConfirmed
      ? ""
      : "Does not match to password";
    this.setState({
      repassword: value,
      passwordRepeatConfirmed,
      errors,
    });
  };

  onClickSignup = () => {
    const user = {
      username: this.state.username,
      displayName: this.state.displayName,
      password: this.state.password,
    };
    this.setState({ pendingApiCall: true });
    this.props.actions
      .postSignup(user)
      .then((response) => {
        this.setState({ pendingApiCall: false }, () => {
          this.props.history.push("/");
        });
      })
      .catch((apiError) => {
        let errors = { ...this.state.errors };

        if (apiError.response.data && apiError.response.data.validationErrors) {
          errors = { ...apiError.response.data.validationErrors };
        }
        console.log(errors);
        this.setState({ pendingApiCall: false, errors });
      });
  };

  render() {
    return (
      <div className="container">
        <h1 className="text-center">Sign Up</h1>
        <div className="col-12 mb-3">
          <Input
            label="Display Name"
            onChange={this.onChangeDisplayName}
            placeholder="Your display name"
            value={this.state.displayName}
            hasError={this.state.errors.displayName && true}
            error={this.state.errors.displayName}
          />
        </div>
        <div className="col-12 mb-3">
          <Input
            label="Enter Username"
            placeholder="Your username"
            onChange={this.onChangeUsername}
            value={this.state.username}
            hasError={this.state.errors.username && true}
            error={this.state.errors.username}
          />
        </div>
        <div className="col-12 mb-3">
          <Input
            label="Enter Password"
            type="password"
            placeholder="Your password"
            onChange={this.onChangePassword}
            value={this.state.password}
            hasError={this.state.errors.password && true}
            error={this.state.errors.password}
          />
        </div>
        <div className="col-12 mb-3">
          <Input
            label="Re-enter Password"
            type="password"
            placeholder="Confirm your password"
            onChange={this.onChangeRePassword}
            value={this.state.repassword}
            hasError={this.state.errors.repassword && true}
            error={this.state.errors.repassword}
          />
        </div>
        <div className="text-center">
          <ButtonWithProgress
            onClick={this.onClickSignup}
            disabled={
              this.state.pendingApiCall || !this.state.passwordRepeatConfirmed
            }
            pendingApiCall={this.state.pendingApiCall}
            text="Sign Up"
          />
        </div>
      </div>
    );
  }
}

UserSignupPage.defaultProps = {
  actions: {
    postSignup: () => {
      new Promise((resolve, reject) => {
        resolve({});
      });
    },
  },
  history: {
    push: () => {},
  },
};

const mapDispatchToProps = (dispatch) => {
  return {
    actions: {
      postSignup: (user) => dispatch(authActions.signupHandler(user)),
    },
  };
};

export default connect(null, mapDispatchToProps)(UserSignupPage);
