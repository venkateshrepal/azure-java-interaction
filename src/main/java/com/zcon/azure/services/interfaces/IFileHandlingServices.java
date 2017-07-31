package com.zcon.azure.services.interfaces;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.simple.JSONObject;

/**
 * @author Vyankatesh
 *
 */
public interface IFileHandlingServices {

	public String uploadFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException;

	public String deleteShare(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException;

	public JSONObject deleteFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException;

	public String uploadFileInExistingShare(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException;

	public String uploadDirectory(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException;

	public JSONObject createDirectory(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException;

	public JSONObject uploadFileInDirectory(JSONObject jsonInput) throws IOException, JSONException, InvalidKeyException, URISyntaxException;

	public JSONObject downloadFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException;

	public JSONObject copyFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException;

	public JSONObject checkDirectoryIfExist(JSONObject jsonInput) throws InvalidKeyException, URISyntaxException;

	public JSONObject syncFiles(JSONObject jsonInput) throws InvalidKeyException, URISyntaxException;

	public JSONObject uploadMultipleFilesInDirectory(JSONObject jsonInput) throws InvalidKeyException, URISyntaxException;


}
