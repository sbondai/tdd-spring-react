import React, { Component } from "react";
import ButtonWithProgress from "./ButtonWIthProgress";

class Modal extends Component {
  render() {
    const {
      title,
      visible,
      body,
      okButton,
      cancelButton,
      onClickCancel,
      pendingApiCall,
      onClickOk,
    } = this.props;

    let rootClass = "modal fade";
    let rootStyle;
    if (visible) {
      rootClass += " d-block show";
      rootStyle = { backgroundColor: "#000000b0" };
    }
    return (
      <div className={rootClass} style={rootStyle} data-testid="modal-root">
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">{title}</h5>
            </div>
            <div className="modal-body">{body}</div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-secondary"
                disabled={pendingApiCall}
                onClick={onClickCancel}
              >
                {cancelButton}
              </button>
              <ButtonWithProgress
                className="btn btn-danger"
                disabled={pendingApiCall}
                pendingApiCall={pendingApiCall}
                onClick={onClickOk}
                text={okButton}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }
}

Modal.defaultProps = {
  okButton: "Ok",
  cancelButton: "Cancel",
};

export default Modal;
