package com.hoaxify.hoaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.hoaxify.hoaxify.configuration.AppConfiguration;
import com.hoaxify.hoaxify.domain.User;
import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.repos.UserRepository;
import com.hoaxify.hoaxify.services.UserService;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.viewModel.UserUpdateVM;
import com.hoaxify.hoaxify.viewModel.UserVM;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

	private static final String API_V1_USERS = "/api/v1/users";
	@Autowired
	TestRestTemplate testRestTemplate;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserService userService;

	@Autowired
	AppConfiguration appConfiguration;

	@Before
	public void cleanUp() {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}

	@Test
	public void postUser_whenUserIsValid_receiveOk() {
		User user = TestUtil.createValidUser();

		ResponseEntity<Object> response = postSignup(user, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postUser_whenUserIsValid_passwordIsHashedInDatabase() {
		User user = TestUtil.createValidUser();
		ResponseEntity<Object> response = postSignup(user, Object.class);
		List<User> users = userRepository.findAll();
		User inDB = users.get(0);
		assertThat(inDB.getPassword()).isNotEqualTo(user.getPassword());

	}

	@Test
	public void postUser_whenUserIsValid_receiveSuccessMessage() {
		User user = TestUtil.createValidUser();

		ResponseEntity<GenericResponse> response = postSignup(user, GenericResponse.class);

		assertThat(response.getBody().getMessage()).isNotNull();
	}

	@Test
	public void post_whenUserIsValid_userSavedToDatabase() {
		User user = TestUtil.createValidUser();
		testRestTemplate.postForEntity(API_V1_USERS, user, Object.class);
		assertThat(userRepository.count()).isEqualTo(1);
	}

	@Test
	public void postUser_whenUserHasNullUsername_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setUsername(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setDisplayName(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void postUser_whenUserHasNullPassword_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void postUser_whenUserHasLessThanRequiredCharacters_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setUsername("abc");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void postUser_whenPasswordHasLessThanRequiredCharacters_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("P4ss");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void postUser_whenDisplayNameHasLessThanRequiredCharacters_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setDisplayName("abc");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setUsername(valueOf256Chars);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setPassword(valueOf256Chars + "A1");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasDisplayNameExceedsTheLengthLimit_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setDisplayName(valueOf256Chars);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordIsToLowCase_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("alllowercase");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPassword_ALLUPPERCASE_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("ALLUPPERCASE");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPassword_AllNumerals_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("1234567890");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserIsInvalid_receiveApiError() {

		User user = new User();
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_V1_USERS);
	}

	@Test
	public void postUser_whenUserIsInvalid_receiveApiErrorWithValidationErrors() {

		User user = new User();
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
	}

	@Test
	public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
		User user = TestUtil.createValidUser();
		user.setUsername(null);
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
	}

	@Test
	public void postUser_whenUserHasNullPassword_receiveGenericMessageOfNullError() {
		User user = TestUtil.createValidUser();
		user.setPassword(null);
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
	}

	@Test
	public void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError() {
		User user = TestUtil.createValidUser();
		user.setUsername("abc");
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
	}

	@Test
	public void postUser_whenUserHasInvalidPasswordPattern_receiveGenericMessageOfNullError() {
		User user = TestUtil.createValidUser();
		user.setPassword("alllowercase");
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password"))
				.isEqualTo("Password must have at least one uppercase, one lowercase and one number");
	}

	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
		userRepository.save(TestUtil.createValidUser());

		User user = TestUtil.createValidUser();
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername() {
		userRepository.save(TestUtil.createValidUser());

		User user = TestUtil.createValidUser();
		ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("This name is in use");
	}

	@Test
	public void getUsers_whenThereAreNoUsersInDB_receiveOK() {
		ResponseEntity<Object> response = getUsers(new ParameterizedTypeReference<Object>() {
		});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

	}

	@Test
	public void getUsers_whenThereAreNoUsersInDB_receivePageWithZeroItems() {

		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}

	@Test
	public void getUsers_whenThereAreIsAUserInDB_receivePageWithUser() {

		userRepository.save(TestUtil.createValidUser());
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getNumberOfElements()).isEqualTo(1);
	}

	@Test
	public void getUsers_whenThereAreIsAUserInDB_receiveUserWithoutPassword() {

		userRepository.save(TestUtil.createValidUser());
		ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(
				new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {
				});
		Map<String, Object> entity = response.getBody().getContent().get(0);
		assertThat(entity.containsKey("password")).isFalse();
	}

	@Test
	public void getUsers_whenPageIsRequestedFor3ItemsPerPageWhereTheDatabaseHas20Users_receive3Users() {
		IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i).map(TestUtil::createValidUser)
				.forEach(userRepository::save);
		String path = API_V1_USERS + "?page=0&size=3";

		ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
		});

		assertThat(response.getBody().getContent().size()).isEqualTo(3);

	}

	@Test
	public void getUsers_whenPageSizeNotProvided_receivePageSizeAs10() {

		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getSize()).isEqualTo(10);
	}

	@Test
	public void getUsers_whenPageSizeIsGreaterThan100_receivePageSizeAs100() {
		String path = API_V1_USERS + "?size=500";
		ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getSize()).isEqualTo(100);
	}

	@Test
	public void getUsers_whenPageSizeIsNegative_receivePageSizeAs10() {
		String path = API_V1_USERS + "?size=-5";
		ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getSize()).isEqualTo(10);
	}

	@Test
	public void getUsers_whenPageNegative_receiveFirstPage() {
		String path = API_V1_USERS + "?size=-5";
		ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getNumber()).isEqualTo(0);
	}

	@Test
	public void getUsers_whenUserLoggedIn_receivePageWithoutLoggedInUser() {

		userService.save(TestUtil.createValidUser("user1"));
		userService.save(TestUtil.createValidUser("user2"));
		userService.save(TestUtil.createValidUser("user3"));
		authenticate("user1");
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
		});

		assertThat(response.getBody().getTotalElements()).isEqualTo(2);

	}

	@Test
	public void getUserByUsername_whenUserExist_receiveOK() {
		String username = "test-user";
		userService.save(TestUtil.createValidUser(username));
		ResponseEntity<Object> response = getUser(username, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getUserByUsername_whenUserExist_receiveUserWithoutPassword() {
		String username = "test-user";
		userService.save(TestUtil.createValidUser(username));
		ResponseEntity<String> response = getUser(username, String.class);
		assertThat(response.getBody().contains("password")).isFalse();
	}

	@Test
	public void getUserByUsername_whenDoesNotExist_receiveNotFound() {
		ResponseEntity<Object> response = getUser("unknown-user", Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void getUserByUsername_whenDoesNotExist_receiveApiError() {
		ResponseEntity<ApiError> response = getUser("unknown-user", ApiError.class);
		assertThat(response.getBody().getMessage().contains("unknown-user")).isTrue();
	}

	@Test
	public void putUser_whenAuthorisedUserSendsUpdateForAnotherUser_receiveForbidden() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		long anotherUserId = user.getId() + 123;
		ResponseEntity<Object> response = putUser(anotherUserId, null, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void putUser_whenUnauthorisedUserSendsTheRequest_receiveApiError() {
		ResponseEntity<ApiError> response = putUser(123, null, ApiError.class);
		assertThat(response.getBody().getUrl()).contains("users/123");
	}

	@Test
	public void putUser_whenAuthorisedUserSendsUpdateForAnotherUser_receiveApiError() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		long anotherUserId = user.getId() + 123;
		ResponseEntity<ApiError> response = putUser(anotherUserId, null, ApiError.class);
		assertThat(response.getBody().getUrl()).contains("users/" + anotherUserId);
	}

	@Test
	public void putUser_whenUnauthorisedUserSendsTheRequest_receiveUnauthorised() {
		ResponseEntity<Object> response = putUser(123, null, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveOk() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);

		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_displayameUpdated() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);

		putUser(user.getId(), requestEntity, Object.class);
		User userInDB = userRepository.findByUsername("user1");
		assertThat(userInDB.getDisplayName()).isEqualTo(updateUser.getDisplayName());
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveUserVMWithUpdatedDisplayName() {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);

		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

		assertThat(response.getBody().getDisplayName()).isEqualTo(updateUser.getDisplayName());
	}

	@Test
	public void putUser_withValidRequestBodyWithSupportedImageFromAuthorzedUser_receiveUserVMWithRandomImageName()
			throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("profile.png");
		updateUser.setImage(imageString);

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

		assertThat(response.getBody().getImage()).isNotEqualTo("profile-image.png");
	}

	@Test
	public void putUser_withValidRequestBodyWithSupportedImageFromAuthorzedUser_imageIsStoredUnderProfileFolder()
			throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("profile.png");
		updateUser.setImage(imageString);

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

		String storedImageName = response.getBody().getImage();

		String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
		File storedImage = new File(profilePicturePath);
		assertThat(storedImage.exists()).isTrue();

	}

	@Test
	public void putUser_withInvalidRequestBodyWithNullDisplayNameFromAuthorizedUser_receiveBadRequest()
			throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = new UserUpdateVM();

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void putUser_withInvalidRequestBodyWithLessThanMinSizeDisplayNameFromAuthorizedUser_receiveBadRequest()
			throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = new UserUpdateVM();
		String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		updateUser.setDisplayName(valueOf256Chars);

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void putUser_withInvalidRequestBodyWithMoreThanMaxSizeDisplayNameFromAuthorizedUser_receiveBadRequest()
			throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = new UserUpdateVM();
		updateUser.setDisplayName("abc");

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void putUser_withValidRequestBodyWithJPGImageFromAuthorzedUser_receiveOK() throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-jpg.jpg");
		updateUser.setImage(imageString);

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

	}

	@Test
	public void putUser_withValidRequestBodyWithGIFImageFromAuthorzedUser_receiveBadRequest() throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-gif.gif");
		updateUser.setImage(imageString);

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void putUser_withValidRequestBodyWithTxtForImageFromAuthorzedUser_receiveValidationErrorForProfileImage()
			throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-txt.txt");
		updateUser.setImage(imageString);

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<ApiError> response = putUser(user.getId(), requestEntity, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("image")).isEqualTo("Only PNG and JPG files are allowed");

	}

	@Test
	public void putUser_withValidRequestBodyWithJPGImage_removesOldImageFromStorage() throws IOException {
		User user = userService.save(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updateUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-jpg.jpg");
		updateUser.setImage(imageString);

		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updateUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

		putUser(user.getId(), requestEntity, UserVM.class);
		String storedImageName = response.getBody().getImage();
		String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
		File storeImage = new File(profilePicturePath);

		assertThat(storeImage.exists()).isFalse();

	}

	private String readFileToBase64(String fileName) throws IOException {

		ClassPathResource imageResource = new ClassPathResource(fileName);
		byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
		String imageString = Base64.getEncoder().encodeToString(imageArr);
		return imageString;

	}

	private UserUpdateVM createValidUserUpdateVM() {
		UserUpdateVM updateUser = new UserUpdateVM();
		updateUser.setDisplayName("newDisplayName");
		return updateUser;
	}

	private void authenticate(String username) {
		testRestTemplate.getRestTemplate().getInterceptors()
				.add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}

	public <T> ResponseEntity<T> getUser(String username, Class<T> responseType) {
		String path = API_V1_USERS + "/" + username;
		return testRestTemplate.getForEntity(path, responseType);
	}

	public <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	public <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(API_V1_USERS, HttpMethod.GET, null, responseType);
	}

	public <T> ResponseEntity<T> putUser(long id, HttpEntity<?> requestEntity, Class<T> responseType) {
		String path = API_V1_USERS + "/" + id;
		return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, responseType);
	}

	public <T> ResponseEntity<T> postSignup(Object request, Class<T> response) {
		return testRestTemplate.postForEntity(API_V1_USERS, request, response);
	}

	@After
	public void cleanDirectory() throws IOException {
		FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));

	}

}
