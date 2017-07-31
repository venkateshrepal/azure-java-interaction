package com.zcon.azure.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zcon.azure.services.interfaces.IFileHandlingServices;
/**
 * @author Vyankatesh
 *
 */
@RestController
@RequestMapping("/fileHandling")
public class FileHandlingController {
	@Autowired
	IFileHandlingServices fileHandlingService;
	DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	Calendar calobj = Calendar.getInstance();
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<String> uploadFile(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		String result = fileHandlingService.uploadFile(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/deleteShare", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<String> deleteShare(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		String result = fileHandlingService.deleteShare(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/deleteFile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> deleteFile(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		JSONObject result = fileHandlingService.deleteFile(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/uploadFileInExistingShare", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<String> uploadFileInExistingShare(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		String result = fileHandlingService.uploadFileInExistingShare(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/uploadDirectory", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<String> uploadDirectory(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		System.out.println("Service uploadDirectory started at" + df.format(calobj.getTime()));
		String result = fileHandlingService.uploadDirectory(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	@RequestMapping(value = "/createDirectory", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> createDirectory(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		JSONObject result = fileHandlingService.createDirectory(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/uploadFileInDirectory", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> uploadFileInDirectory(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		JSONObject result = fileHandlingService.uploadFileInDirectory(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/downloadFile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> downloadFile(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		JSONObject result = fileHandlingService.downloadFile(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/copyFile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> copyFile(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		JSONObject result = fileHandlingService.copyFile(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/checkDirectoryIfExist", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> checkDirectoryIfExist(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		JSONObject result = fileHandlingService.checkDirectoryIfExist(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/syncFiles", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> syncFiles(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		JSONObject result = fileHandlingService.syncFiles(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/uploadMultipleFilesInDirectory", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> uploadMultipleFilesInDirectory(@RequestBody JSONObject jsonInput)
			throws NumberFormatException, Exception {
		JSONObject result = fileHandlingService.uploadMultipleFilesInDirectory(jsonInput);
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
