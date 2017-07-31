package com.zcon.azure.services.implementation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zcon.azure.dao.interfaces.IFileHandlingDao;
import com.zcon.azure.services.interfaces.IFileHandlingServices;

/**
 * @author Vyankatesh
 *
 */
@Service("fileHandlingService")
public class FileHandlingServiceImpl implements IFileHandlingServices {

	@Autowired
	IFileHandlingDao fileHandlingDao;

	@Override
	public String uploadFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.uploadFile(jsonInput);
	}

	@Override
	public String deleteShare(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.deleteShare(jsonInput);
	}

	@Override
	public JSONObject deleteFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.deleteFile(jsonInput);
	}

	@Override
	public String uploadFileInExistingShare(JSONObject jsonInput)
			throws IOException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.uploadFileInExistingShare(jsonInput);
	}

	@Override
	public String uploadDirectory(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.uploadDirectory(jsonInput);
	}

	@Override
	public JSONObject createDirectory(JSONObject jsonInput)
			throws IOException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.createDirectory(jsonInput);
	}

	@Override
	public JSONObject uploadFileInDirectory(JSONObject jsonInput)
			throws IOException, JSONException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.uploadFileInDirectory(jsonInput);
	}

	@Override
	public JSONObject downloadFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.downloadFile(jsonInput);
	}

	@Override
	public JSONObject copyFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		return fileHandlingDao.copyFile(jsonInput);
	}

	@Override
	public JSONObject checkDirectoryIfExist(JSONObject jsonInput) throws InvalidKeyException, URISyntaxException {
		return fileHandlingDao.checkDirectoryIfExist(jsonInput);
	}

	@Override
	public JSONObject syncFiles(JSONObject jsonInput) throws InvalidKeyException, URISyntaxException {
		return fileHandlingDao.syncFiles(jsonInput);
	}

	@Override
	public JSONObject uploadMultipleFilesInDirectory(JSONObject jsonInput)
			throws InvalidKeyException, URISyntaxException {
		return fileHandlingDao.uploadMultipleFilesInDirectory(jsonInput);
	}


}
