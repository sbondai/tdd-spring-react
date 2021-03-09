package com.hoaxify.hoaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.hoaxify.hoaxify.configuration.AppConfiguration;
import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentRepository;
import com.hoaxify.hoaxify.repos.UserRepository;
import com.hoaxify.hoaxify.services.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileUploadControllerTest {

	private static final String API_1_HOAXES_UPLOAD = "/api/v1/hoaxes/upload";

	@Autowired
	TestRestTemplate testRestTemplate;

	@Autowired
	UserRepository userRepository;

	@Autowired
	FileAttachmentRepository fileAttachmentRepository;

	@Autowired
	UserService userService;

	@Autowired
	AppConfiguration appConfiguration;

	@Before
	public void init() throws IOException {
		userRepository.deleteAll();
		fileAttachmentRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
	}

	@Test
	public void uploadFile_withImageFromAuthorisedUser_receiveOk() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");

		ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void uploadFile_withImageFromUnAuthorisedUser_receiveUnauthorized() {

		ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void uploadFile_withImageFromAuthorisedUser_receiveFileAttachmentWithDate() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");

		ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);

		assertThat(response.getBody().getDate()).isNotNull();
	}

	@Test
	public void uploadFile_withImageFromAuthorisedUser_imageSavedToFolder() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");

		ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
		String imagePath = appConfiguration.getFullAttachmentsPath() + "/" + response.getBody().getName();
		File storedImage = new File(imagePath);
		assertThat(storedImage.exists()).isTrue();
	}

	@Test
	public void uploadFile_withImageFromAuthorisedUser_receiveFileAttachmentWithRandomName() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");

		ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);

		assertThat(response.getBody().getName()).isNotNull();
		assertThat(response.getBody().getName()).isNotEqualTo("profile.png");
	}

	@Test
	public void uploadFile_withImageFromAuthorisedUser_fileAttachmentSavedtoDatabase() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		uploadFile(getRequestEntity(), FileAttachment.class);

		assertThat(fileAttachmentRepository.count()).isEqualTo(1);
	}

	@Test
	public void uploadFile_withImageFromAuthorisedUser_fileAttachmentStoredWithFileType() {
		userService.save(TestUtil.createValidUser("user1"));
		authenticate("user1");
		uploadFile(getRequestEntity(), FileAttachment.class);
		FileAttachment storedFile = fileAttachmentRepository.findAll().get(0);
		assertThat(storedFile.getFileType()).isEqualTo("image/png");
	}

	public <T> ResponseEntity<T> uploadFile(HttpEntity<?> requestEntity, Class<T> responseType) {
		return testRestTemplate.exchange(API_1_HOAXES_UPLOAD, HttpMethod.POST, requestEntity, responseType);
	}

	private HttpEntity<MultiValueMap<String, Object>> getRequestEntity() {
		ClassPathResource imageResource = new ClassPathResource("profile.png");
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", imageResource);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
		return requestEntity;
	}

	private void authenticate(String username) {
		testRestTemplate.getRestTemplate().getInterceptors()
				.add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}

}
