import React from "react";
import { fireEvent, render } from "@testing-library/react";
import ProfileImageWIthDefault from "./ProfileImageWithDefault";

describe("ProfileImageWithDefault", () => {
  describe("Layout", () => {
    it("has image", () => {
      const { container } = render(<ProfileImageWIthDefault />);
      const image = container.querySelector("img");
      expect(image).toBeInTheDocument();
    });
    it("displays default image when image propertynot provided", () => {
      const { container } = render(<ProfileImageWIthDefault />);
      const image = container.querySelector("img");
      expect(image.src).toContain("/profile.png");
    });

    it("displays user image when image property provided", () => {
      const { container } = render(
        <ProfileImageWIthDefault image="profile1.png" />
      );
      const image = container.querySelector("img");
      expect(image.src).toContain("/images/profile/profile1.png");
    });
    it("displays default image when provided image loading fails", () => {
      const { container } = render(
        <ProfileImageWIthDefault image="profile1.png" />
      );
      const image = container.querySelector("img");
      fireEvent.error(image);
      expect(image.src).toContain("/profile.png");
    });
    it("displays the image provided through src property", () => {
      const { container } = render(
        <ProfileImageWIthDefault src="image-from-src.png" />
      );
      const image = container.querySelector("img");
      expect(image.src).toContain("/image-from-src.png");
    });
  });
});
