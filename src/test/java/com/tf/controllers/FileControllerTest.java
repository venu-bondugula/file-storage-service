package com.tf.controllers;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.gson.Gson;
import com.tf.repositories.FileRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.tf.services.FileService;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(FileController.class)
public class FileControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FileService fileService;

	@MockBean
	private FileRepository fileRepository;

	private static final Gson gson = new Gson();

	@Test
	public void helloWorld() throws Exception {
		this.mockMvc.perform(get("http://localhost:8080/api/v1/file"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("hello").value("world"));
	}

	@Test
	public void addFile() throws Exception {
		HashMap<String, String> stringStringHashMap = new HashMap<>();
		stringStringHashMap.put("payload", "test_payload");
		System.out.print(gson.toJson(stringStringHashMap));
		this.mockMvc.perform(post("http://localhost:8080/api/v1/file").content(gson.toJson(stringStringHashMap)).contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(status().isOk())
			.//			.andExpect(model().attributeExists("id"))
		//			.andExpect(model().attribute("<name>", "<value>"))
		//			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
		//			.andExpect(jsonPath("$.id").value("<value>"))
		  andExpect(jsonPath("payload").value("test_payload"));
	}

	@Test
	public void addFileTODO() throws Exception {
		HashMap<String, String> stringStringHashMap = new HashMap<>();
		stringStringHashMap.put("payload", "test_payload");
		System.out.print(gson.toJson(stringStringHashMap));
		this.mockMvc.perform(post("http://localhost:8080/api/v1/file/testa").content(gson.toJson(stringStringHashMap)).contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("<name>"))
			.andExpect(model().attribute("<name>", "<value>"))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
			.andExpect(jsonPath("$.id").value("<value>"))
			.andExpect(jsonPath("$.payload").value("<value>"));
	}
}
