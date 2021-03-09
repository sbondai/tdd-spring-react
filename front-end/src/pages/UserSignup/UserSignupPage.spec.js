import React from "react";
import {
  render,
  fireEvent,
  waitForElementToBeRemoved,
  waitFor,
} from "@testing-library/react";
import { UserSignupPage } from "./UserSignupPage";

describe("UserSignupPage", () => {
  describe("Layout", () => {
    it("has header of Sign Up", () => {
      const { container } = render(<UserSignupPage />);
      const header = container.querySelector("h1");
      expect(header).toHaveTextContent("Sign Up");
    });

    it("has input for display name", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const usernameInput = queryByPlaceholderText("Your display name");
      expect(usernameInput).toBeInTheDocument();
    });

    it("has input for display Username", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const displayNameInput = queryByPlaceholderText("Your username");
      expect(displayNameInput).toBeInTheDocument();
    });

    it("has input for display Password", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const passwordInput = queryByPlaceholderText("Your password");
      expect(passwordInput).toBeInTheDocument();
    });

    it("has password type for password input", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const passwordInput = queryByPlaceholderText("Your password");
      expect(passwordInput.type).toBe("password");
    });

    it("has input for display Password 1", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const repeatPassword = queryByPlaceholderText("Confirm your password");
      expect(repeatPassword).toBeInTheDocument();
    });

    it("has input for display Password 2", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const repeatPassword = queryByPlaceholderText("Your password");
      expect(repeatPassword.type).toBe("password");
    });

    it("must contain a submit button", () => {
      const { container } = render(<UserSignupPage />);
      const submitButton = container.querySelector("button");
      expect(submitButton).toBeInTheDocument();
    });
  });

  describe("Interactions", () => {
    const changeEvent = (content) => {
      return {
        target: {
          value: content,
        },
      };
    };

    const mockAsyncDelayed = () => {
      return jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });
    };

    let button, displayNameInput, usernameInput, passwordInput, passwordRepeat;

    const setupForSubmit = (props) => {
      const rendered = render(<UserSignupPage {...props} />);

      const { container, queryByPlaceholderText } = rendered;

      displayNameInput = queryByPlaceholderText("Your display name");
      usernameInput = queryByPlaceholderText("Your username");
      passwordInput = queryByPlaceholderText("Your password");
      passwordRepeat = queryByPlaceholderText("Confirm your password");

      fireEvent.change(displayNameInput, changeEvent("my-display-name"));
      fireEvent.change(usernameInput, changeEvent("my-user-name"));
      fireEvent.change(passwordInput, changeEvent("P4ssword"));
      fireEvent.change(passwordRepeat, changeEvent("P4ssword"));

      button = container.querySelector("button");

      return rendered;
    };

    it("set the displayName value into State", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const displayNameInput = queryByPlaceholderText("Your display name");

      fireEvent.change(displayNameInput, changeEvent("my-display-name"));
      expect(displayNameInput).toHaveValue("my-display-name");
    });

    it("set the Username value into State", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const usernameInput = queryByPlaceholderText("Your username");

      fireEvent.change(usernameInput, changeEvent("my-user-name"));
      expect(usernameInput).toHaveValue("my-user-name");
    });

    it("set the Password value into State", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const passwordInput = queryByPlaceholderText("Your password");

      fireEvent.change(passwordInput, changeEvent("password"));
      expect(passwordInput).toHaveValue("password");
    });

    it("set the Re-enter Password value into State", () => {
      const { queryByPlaceholderText } = render(<UserSignupPage />);
      const passwordRepeat = queryByPlaceholderText("Confirm your password");

      fireEvent.change(passwordRepeat, changeEvent("passwordRepeat"));
      expect(passwordRepeat).toHaveValue("passwordRepeat");
    });

    it("calls postSignup when the fields are valid and the actions are provided in props", () => {
      const actions = {
        postSignup: jest.fn().mockResolvedValueOnce({}),
      };

      setupForSubmit({ actions });
      fireEvent.click(button);
      expect(actions.postSignup).toHaveBeenCalledTimes(1);
    });

    xit("does not throw exception when clicking the button when actions not provided in props", () => {
      setupForSubmit();
      expect(() => fireEvent.click(button)).not.toThrow();
    });

    it("calls post with user body when the fields are valid", () => {
      const actions = {
        postSignup: jest.fn().mockResolvedValueOnce({}),
      };
      setupForSubmit({ actions });

      fireEvent.click(button);

      const expectedUserObject = {
        username: "my-user-name",
        displayName: "my-display-name",
        password: "P4ssword",
      };

      expect(actions.postSignup).toHaveBeenLastCalledWith(expectedUserObject);
    });

    it("does not allow user to click the Sign Up button when there is an ongoing API call", () => {
      const actions = {
        postSignup: mockAsyncDelayed(),
      };
      setupForSubmit({ actions });
      fireEvent.click(button);
      fireEvent.click(button);

      expect(actions.postSignup).toHaveBeenCalledTimes(1);
    });

    it("display spinnner when there is an ongoing api call", () => {
      const actions = {
        postSignup: mockAsyncDelayed(),
      };
      const { queryByText } = setupForSubmit({ actions });
      fireEvent.click(button);

      const spinner = queryByText("Loading...");
      expect(spinner).toBeInTheDocument();
    });

    it("hides spinner after api call finishes successfully", async () => {
      const actions = {
        postSignup: mockAsyncDelayed(),
      };
      const { queryByText } = setupForSubmit({ actions });
      fireEvent.click(button);

      const spinner = queryByText("Loading...");
      await waitForElementToBeRemoved(spinner);

      expect(spinner).not.toBeInTheDocument();
    });

    it("hides spinner after api call finishes with error", async () => {
      const actions = {
        postSignup: jest.fn().mockImplementation(() => {
          return new Promise((resolve, reject) => {
            setTimeout(() => {
              reject({
                response: { data: {} },
              });
            }, 300);
          });
        }),
      };
      const { queryByText } = setupForSubmit({ actions });
      fireEvent.click(button);

      const spinner = queryByText("Loading...");
      await waitForElementToBeRemoved(spinner);
      expect(spinner).not.toBeInTheDocument();
    });

    it("displays validation error for displayName when error is received for the field", async () => {
      const actions = {
        postSignup: jest.fn().mockRejectedValue({
          response: {
            data: {
              validationErrors: {
                displayName: "Cannot be null",
              },
            },
          },
        }),
      };

      const { findByText } = setupForSubmit({ actions });
      fireEvent.click(button);

      const errorMessage = await findByText("Cannot be null");
      expect(errorMessage).toBeInTheDocument();
    });

    it("enables the signup button when password and repeat password have the same value", () => {
      setupForSubmit();
      expect(button).not.toBeDisabled();
    });

    it("disables the signup button when password repeat does not match password", () => {
      setupForSubmit();
      fireEvent.change(passwordRepeat, changeEvent("new-pass"));
      expect(button).toBeDisabled();
    });

    it("disables the signup button when password does not match repeat password", () => {
      setupForSubmit();
      fireEvent.change(passwordInput, changeEvent("new-pass"));
      expect(button).toBeDisabled();
    });

    it("displays error style for password repeat input when passowrd repeat input mismatch", () => {
      const { queryByText } = setupForSubmit();
      fireEvent.change(passwordRepeat, changeEvent("new-pass"));
      const mismatchWarning = queryByText("Does not match to password");
      expect(mismatchWarning).toBeInTheDocument();
    });

    it("displays error style for password repeat input when passowrd input mismatch", () => {
      const { queryByText } = setupForSubmit();
      fireEvent.change(passwordInput, changeEvent("new-pass"));
      const mismatchWarning = queryByText("Does not match to password");
      expect(mismatchWarning).toBeInTheDocument();
    });

    it("hides the validation error when user changes the content of displayName", async () => {
      const actions = {
        postSignup: jest.fn().mockRejectedValue({
          response: {
            data: {
              validationErrors: {
                displayName: "Cannot be null",
              },
            },
          },
        }),
      };
      const { findByText } = setupForSubmit({ actions });
      fireEvent.click(button);

      const errorMessage = await findByText("Cannot be null");
      fireEvent.change(displayNameInput, changeEvent("name updated"));

      expect(errorMessage).not.toBeInTheDocument();
    });

    it("hides the validation error when user changes the content of username", async () => {
      const actions = {
        postSignup: jest.fn().mockRejectedValue({
          response: {
            data: {
              validationErrors: {
                username: "Username cannot be null",
              },
            },
          },
        }),
      };
      const { findByText } = setupForSubmit({ actions });
      fireEvent.click(button);

      const errorMessage = await findByText("Username cannot be null");
      fireEvent.change(usernameInput, changeEvent("name updated"));

      expect(errorMessage).not.toBeInTheDocument();
    });

    it("hides the validation error when user changes the content of password", async () => {
      const actions = {
        postSignup: jest.fn().mockRejectedValue({
          response: {
            data: {
              validationErrors: {
                password: "Cannot be null",
              },
            },
          },
        }),
      };
      const { findByText } = setupForSubmit({ actions });
      fireEvent.click(button);

      const errorMessage = await findByText("Cannot be null");
      fireEvent.change(passwordInput, changeEvent("updated-password"));

      expect(errorMessage).not.toBeInTheDocument();
    });

    it("redirects to homePage after successful signup", async () => {
      const actions = {
        postSignup: jest.fn().mockResolvedValue({}),
      };

      const history = {
        push: jest.fn(),
      };
      setupForSubmit({ actions, history });
      fireEvent.click(button);
      await waitFor(() => expect(history.push).toHaveBeenCalledWith("/"));
    });

    //last line
  });
});
console.error = () => {};
