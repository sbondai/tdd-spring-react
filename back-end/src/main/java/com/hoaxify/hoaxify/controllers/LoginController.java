package com.hoaxify.hoaxify.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hoaxify.hoaxify.domain.User;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.viewModel.UserVM;

@RestController
public class LoginController {

	@PostMapping("/api/v1/login")
	UserVM handleLogin(@CurrentUser User loggedInUser) {

		return new UserVM(loggedInUser);
	}

}
