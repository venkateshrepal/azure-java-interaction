package com.zcon.azure.implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.springframework.stereotype.Repository;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileSharePermissions;
import com.microsoft.azure.storage.file.ListFileItem;
import com.microsoft.azure.storage.file.SharedAccessFilePermissions;
import com.microsoft.azure.storage.file.SharedAccessFilePolicy;
import com.zcon.azure.dao.interfaces.IFileHandlingDao;

/**
 * @author Vyankatesh
 *
 */
@Repository("fileHandlingDao")
public class FileHandlingDaoImpl implements IFileHandlingDao {
	protected CloudBlobContainer container;
	protected CloudBlockBlob blob;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zcon.azure.DAO.IFileHandlingDao#uploadFile(org.json.simple.JSONObject)
	 * This method can upload file and also creates a share with respect to name
	 * of corresponding file.
	 */
	@Override
	public String uploadFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		CloudFileClient fileClient = null;
		String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + jsonInput.get("accountName")
				+ ";" + "AccountKey=" + jsonInput.get("accountKey");
		System.out.println(storageConnectionString);
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		try {
			fileClient = storageAccount.createCloudFileClient();
			String filePath = jsonInput.get("fileLocation").toString();
			File f = new File(filePath);
			System.out.println(f.getName());
			CloudFileShare share = fileClient
					.getShareReference(f.getName().toLowerCase().replaceAll("[-+.^:,!@#$%&*()_~`]", ""));
			if (share.createIfNotExists()) {
				System.out.println("New share created");
			}
			CloudFileDirectory rootDir = share.getRootDirectoryReference();
			CloudFile cloudFile = rootDir.getFileReference(f.getName().toLowerCase());
			cloudFile.uploadFromFile(filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "File Uploaded Succesfully";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zcon.azure.DAO.IFileHandlingDao#deleteShare(org.json.simple.JSONObject)
	 * This method can delete particular share.
	 */
	@SuppressWarnings("unused")
	@Override
	public String deleteShare(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		CloudFileClient fileClient = null;
		String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + jsonInput.get("accountName")
				+ ";" + "AccountKey=" + jsonInput.get("accountKey");
		System.out.println(storageConnectionString);
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		JSONObject jsonOutput = new JSONObject();
		try {
			fileClient = storageAccount.createCloudFileClient();
			String shareName = jsonInput.get("shareName").toString();
			CloudFileShare share = fileClient.getShareReference(shareName);
			share.delete();
			System.out.println("Share " + shareName + " deleted succesfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Share Deleted";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zcon.azure.DAO.IFileHandlingDao#deleteFile(org.json.simple.JSONObject)
	 * This method can delete file under particular directory.
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public JSONObject deleteFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		if (jsonInput.containsKey("accountName")) {
			CloudFileClient fileClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("accountName") + ";" + "AccountKey=" + jsonInput.get("accountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			try {
				fileClient = storageAccount.createCloudFileClient();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				System.out.println(directoryNameArray.length);
				String fileName = jsonInput.get("fileName").toString();
				CloudFileShare share = fileClient.getShareReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				CloudFileDirectory rootDir = share.getRootDirectoryReference();
				for (int i = 0; i < directoryNameArray.length; i++) {
					String directoryToCreate = directoryNameArray[i];
					CloudFileDirectory directory = rootDir.getDirectoryReference(directoryToCreate);
					if (i == directoryNameArray.length - 1) {
						CloudFile cloudFile = directory.getFileReference(fileName);
						String tokenKey = testFileSAS(share, cloudFile);
						jsonOutput.put("status", "successful");
						jsonOutput.put("token", tokenKey);
						cloudFile.delete();
						System.out
								.println("file " + fileName + " under share " + directoryName + " succesfully deleted");
					}
					rootDir = directory;
				}
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		} else {
			CloudBlobClient blobClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("blobAccountName") + ";" + "AccountKey=" + jsonInput.get("blobAccountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			try {
				blobClient = storageAccount.createCloudBlobClient();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				CloudBlobContainer container = blobClient.getContainerReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (container.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				CloudBlockBlob blob = container
						.getBlockBlobReference(jsonInput.get("directoryStructure") + "/" + jsonInput.get("fileName"));
				CloudBlobDirectory directoryOfFile = container
						.getDirectoryReference(jsonInput.get("directoryStructure").toString());
				CloudBlob blobToDelete = directoryOfFile.getBlockBlobReference(jsonInput.get("fileName").toString());
				String tokenKey = testBlobSaS(blob, container);
				jsonOutput.put("token", tokenKey);
				blobToDelete.deleteIfExists();
				final String filePath = "/home/zcon/Temp.txt";
				CloudBlockBlob blobToCreate = container
						.getBlockBlobReference(jsonInput.get("directoryStructure") + "/Temp.txt");
				File source = new File(filePath);
				blobToCreate.upload(new FileInputStream(source), source.length());
				jsonOutput.put("status", "successful");
				return jsonOutput;
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zcon.azure.DAO.IFileHandlingDao#uploadFileInExistingShare(org.json.
	 * simple.JSONObject) This method can upload file in particular share.
	 */
	@SuppressWarnings("unused")
	@Override
	public String uploadFileInExistingShare(JSONObject jsonInput)
			throws IOException, InvalidKeyException, URISyntaxException {
		CloudFileClient fileClient = null;
		String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + jsonInput.get("accountName")
				+ ";" + "AccountKey=" + jsonInput.get("accountKey");
		System.out.println(storageConnectionString);
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		JSONObject jsonOutput = new JSONObject();
		try {
			fileClient = storageAccount.createCloudFileClient();
			String shareName = jsonInput.get("shareName").toString();
			String filePath = jsonInput.get("fileLocation").toString();
			File f = new File(filePath);
			CloudFileShare share = fileClient.getShareReference(shareName);
			CloudFileDirectory rootDir = share.getRootDirectoryReference();
			CloudFile cloudFile = rootDir.getFileReference(f.getName().toLowerCase());
			cloudFile.uploadFromFile(filePath);
			System.out.println("file " + f.getName() + " under share " + shareName + " succesfully created");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "File succesfully created";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.zcon.azure.DAO.IFileHandlingDao#uploadDirectory(org.json.simple.
	 * JSONObject) This method can uploadDirectory from local to Azure storage
	 * account.
	 */
	@SuppressWarnings("unused")
	@Override
	public String uploadDirectory(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		CloudFileClient fileClient = null;
		String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + jsonInput.get("accountName")
				+ ";" + "AccountKey=" + jsonInput.get("accountKey");
		System.out.println(storageConnectionString);
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		JSONObject jsonOutput = new JSONObject();
		try {
			fileClient = storageAccount.createCloudFileClient();
			String directoryLocation = jsonInput.get("directoryLocation").toString();
			File f = new File(directoryLocation);
			listFilesForFolder(f);
			File f1 = new File("/home/zcon/Documents/TestingDocument2");
			CloudFileShare share = fileClient
					.getShareReference(f.getName().toLowerCase().replaceAll("[-+.^:,!@#$%&*()_~`]", ""));
			if (share.createIfNotExists()) {
				System.out.println("New share created");
			}
			CloudFileDirectory rootDir = share.getRootDirectoryReference();
			CloudFileDirectory sampleDir = rootDir.getDirectoryReference(f.getName());
			if (sampleDir.createIfNotExists()) {
				System.out.println("new directory created");
			}
			for (ListFileItem fileItem : rootDir.listFilesAndDirectories()) {
				System.out.println(fileItem.getUri());
			}
		} catch (Exception e) {
			System.out.println("Exception " + e.toString());
		}
		return "Directory uploaded succesfully";
	}

	public void listFilesForFolder(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				System.out.println(fileEntry.getName());
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JSONObject uploadMultipleFilesInDirectory(JSONObject jsonInput)
			throws InvalidKeyException, URISyntaxException {
		if (jsonInput.containsKey("accountName")) {
			CloudFileClient fileClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("accountName") + ";" + "AccountKey=" + jsonInput.get("accountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			ArrayList tokenList = new ArrayList();
			ArrayList filePathList = new ArrayList();
			try {
				org.json.JSONObject jSONObject = new org.json.JSONObject(jsonInput.toString());
				JSONArray fileLocation = jSONObject.getJSONArray("fileLocation");
				org.json.JSONObject jSONObjectName = new org.json.JSONObject(jsonInput.toString());
				JSONArray nameForFile = jSONObjectName.getJSONArray("nameForFile");
				fileClient = storageAccount.createCloudFileClient();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				System.out.println(directoryNameArray.length);
				CloudFileShare share = fileClient.getShareReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (share.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				CloudFileDirectory rootDir = share.getRootDirectoryReference();
				for (int i = 0; i < directoryNameArray.length; i++) {
					String directoryToCreate = directoryNameArray[i];
					CloudFileDirectory directory = rootDir.getDirectoryReference(directoryToCreate);
					if (directory.createIfNotExists()) {
						String token = testDirectorySas(share, rootDir);
						System.out.println("new directory created named as " + directoryName);
						jsonOutput.put("token", token);
						jsonOutput.put("status", "successful");
					}
					if (i == directoryNameArray.length - 1) {
						for (int d = 0; d < fileLocation.length(); d++) {
							CloudFile cloudFile = directory.getFileReference(nameForFile.getString(d));
							System.out.println("uploading file===================" + nameForFile.getString(d));
							if (cloudFile.exists()) {
								cloudFile.create(0);
								System.out.println("cloud file created with name " + cloudFile.getName());
								String tokenKey = testFileSAS(share, cloudFile);
								cloudFile.uploadFromFile(fileLocation.getString(d));
								tokenList.add(tokenKey);
								jsonOutput.put("token", tokenList);
								jsonOutput.put("status", "successful");
								filePathList.add(cloudFile.getStorageUri().getPrimaryUri() + "?" + tokenKey);
								jsonOutput.put("filePath", filePathList);
								jsonOutput.put("note", "file updated successfully");
							} else {
								cloudFile.create(0);
								System.out.println("cloud file created with name " + cloudFile.getName());
								String tokenKey = testFileSAS(share, cloudFile);
								cloudFile.uploadFromFile(fileLocation.getString(d));
								tokenList.add(tokenKey);
								jsonOutput.put("token", tokenList);
								jsonOutput.put("status", "successful");
								filePathList.add(cloudFile.getStorageUri().getPrimaryUri() + "?" + tokenKey);
								jsonOutput.put("filePath", filePathList);
							}
						}
					}
					rootDir = directory;
				}
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		} else {
			CloudBlobClient blobClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("blobAccountName") + ";" + "AccountKey=" + jsonInput.get("blobAccountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			ArrayList filePathList = new ArrayList();
			try {
				org.json.JSONObject jSONObject = new org.json.JSONObject(jsonInput.toString());
				JSONArray fileLocation = jSONObject.getJSONArray("fileLocation");

				org.json.JSONObject jSONObjectName = new org.json.JSONObject(jsonInput.toString());
				JSONArray nameForFile = jSONObjectName.getJSONArray("nameForFile");

				blobClient = storageAccount.createCloudBlobClient();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				CloudBlobContainer container = blobClient.getContainerReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (container.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				for (int i = 0; i < fileLocation.length(); i++) {
					final String filePath = fileLocation.getString(i).toString();
					CloudBlockBlob blob = container.getBlockBlobReference(
							jsonInput.get("directoryStructure") + "/" + nameForFile.getString(i).toString());
					File source = new File(filePath);
					if (blob.exists()) {
						blob.upload(new FileInputStream(source), source.length());
						jsonOutput.put("status", "successful");
						String tokenKey = testBlobSaS(blob, container);
						jsonOutput.put("token", tokenKey);
						filePathList.add(blob.getStorageUri().getPrimaryUri() + "?" + tokenKey);
						jsonOutput.put("note", "file(s) updated successfully");
					} else {
						blob.upload(new FileInputStream(source), source.length());
						jsonOutput.put("status", "successful");
						String tokenKey = testBlobSaS(blob, container);
						jsonOutput.put("token", tokenKey);
						filePathList.add(blob.getStorageUri().getPrimaryUri() + "?" + tokenKey);
					}
					jsonOutput.put("filePath", filePathList);
				}
				return jsonOutput;
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.zcon.azure.DAO.IFileHandlingDao#createDirectory(org.json.simple.
	 * JSONObject) This method can create directories as per required structure.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject createDirectory(JSONObject jsonInput)
			throws IOException, InvalidKeyException, URISyntaxException {
		if (jsonInput.containsKey("accountName")) {
			CloudFileClient fileClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("accountName") + ";" + "AccountKey=" + jsonInput.get("accountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			try {
				fileClient = storageAccount.createCloudFileClient();
				String directoryName = jsonInput.get("directoryStructure").toString();

				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				System.out.println(directoryNameArray.length);

				CloudFileShare share = fileClient.getShareReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (share.createIfNotExists()) {
					System.out.println("New container created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				CloudFileDirectory rootDir = share.getRootDirectoryReference();
				for (int i = 0; i < directoryNameArray.length; i++) {
					String directoryToCreate = directoryNameArray[i];
					CloudFileDirectory directory = rootDir.getDirectoryReference(directoryToCreate);
					if (directory.createIfNotExists()) {
						String token = testDirectorySas(share, rootDir);
						System.out.println("new directory created named as " + directoryName);
						jsonOutput.put("token", token);
						jsonOutput.put("status", "successful");
					}
					rootDir = directory;
				}
				if (jsonOutput.containsKey("token")) {
					return jsonOutput;
				} else {
					jsonOutput.put("note", "same directory structure already exist");
				}
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		} else {
			CloudBlobClient blobClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("blobAccountName") + ";" + "AccountKey=" + jsonInput.get("blobAccountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			try {
				blobClient = storageAccount.createCloudBlobClient();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				CloudBlobContainer container = blobClient.getContainerReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (container.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				final String filePath = "/home/zcon/Temp.txt";
				CloudBlockBlob blob = container
						.getBlockBlobReference(jsonInput.get("directoryStructure") + "/Temp.txt");
				File source = new File(filePath);
				blob.upload(new FileInputStream(source), source.length());
				String tokenKey = testBlobSaS(blob, container);
				jsonOutput.put("token", tokenKey);
				jsonOutput.put("status", "successful");
				return jsonOutput;
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zcon.azure.DAO.IFileHandlingDao#uploadFileInDirectory(org.json.simple.
	 * JSONObject) This method can upload file in directory.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject uploadFileInDirectory(JSONObject jsonInput)
			throws IOException, InvalidKeyException, URISyntaxException {
		if (jsonInput.containsKey("accountName")) {
			CloudFileClient fileClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("accountName") + ";" + "AccountKey=" + jsonInput.get("accountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			String fileLocation = jsonInput.get("fileLocation").toString();
			String nameForFile = jsonInput.get("nameForFile").toString();
			File f = new File(fileLocation);
			try {
				fileClient = storageAccount.createCloudFileClient();
				String directoryName = jsonInput.get("directoryStructure").toString();

				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				System.out.println(directoryNameArray.length);

				CloudFileShare share = fileClient.getShareReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (share.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				CloudFileDirectory rootDir = share.getRootDirectoryReference();
				for (int i = 0; i < directoryNameArray.length; i++) {
					String directoryToCreate = directoryNameArray[i];
					CloudFileDirectory directory = rootDir.getDirectoryReference(directoryToCreate);
					if (directory.createIfNotExists()) {
						String token = testDirectorySas(share, rootDir);
						System.out.println("new directory created named as " + directoryName);
						jsonOutput.put("token", token);
						jsonOutput.put("status", "successful");
					}
					if (i == directoryNameArray.length - 1 && f.exists() && !f.isDirectory()) {
						CloudFile cloudFile = directory.getFileReference(nameForFile);
						if (cloudFile.exists()) {
							cloudFile.create(0);
							System.out.println("cloud file created with name " + cloudFile.getName());
							String tokenKey = testFileSAS(share, cloudFile);
							cloudFile.uploadFromFile(fileLocation);
							jsonOutput.put("token", tokenKey);
							jsonOutput.put("status", "successful");
							cloudFile.getStorageUri().getPrimaryUri();
							jsonOutput.put("filePath", cloudFile.getStorageUri().getPrimaryUri() + "?" + tokenKey);
							jsonOutput.put("note", "file updated successfully");
						} else {
							cloudFile.create(0);
							System.out.println("cloud file created with name " + cloudFile.getName());
							String tokenKey = testFileSAS(share, cloudFile);
							cloudFile.uploadFromFile(fileLocation);
							jsonOutput.put("token", tokenKey);
							jsonOutput.put("status", "successful");
							cloudFile.getStorageUri().getPrimaryUri();
							jsonOutput.put("filePath", cloudFile.getStorageUri().getPrimaryUri() + "?" + tokenKey);
						}
					}
					rootDir = directory;
				}
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		} else {
			CloudBlobClient blobClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("blobAccountName") + ";" + "AccountKey=" + jsonInput.get("blobAccountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			try {
				blobClient = storageAccount.createCloudBlobClient();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				CloudBlobContainer container = blobClient.getContainerReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (container.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				final String filePath = jsonInput.get("fileLocation").toString();
				CloudBlockBlob blob = container.getBlockBlobReference(
						jsonInput.get("directoryStructure") + "/" + jsonInput.get("nameForFile"));
				File source = new File(filePath);
				if (blob.exists()) {
					blob.upload(new FileInputStream(source), source.length());
					jsonOutput.put("status", "successful");
					String tokenKey = testBlobSaS(blob, container);
					jsonOutput.put("token", tokenKey);
					jsonOutput.put("filePath", blob.getStorageUri().getPrimaryUri() + "?" + tokenKey);
					jsonOutput.put("note", "file updated successfully");
				} else {
					blob.upload(new FileInputStream(source), source.length());
					jsonOutput.put("status", "successful");
					String tokenKey = testBlobSaS(blob, container);
					jsonOutput.put("token", tokenKey);
					jsonOutput.put("filePath", blob.getStorageUri().getPrimaryUri() + "?" + tokenKey);
				}
				return jsonOutput;
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject downloadFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		if (jsonInput.containsKey("accountName")) {
			CloudFileClient fileClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("accountName") + ";" + "AccountKey=" + jsonInput.get("accountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			try {
				fileClient = storageAccount.createCloudFileClient();
				String fileToDownload = jsonInput.get("fileName").toString();
				String downloadLocation = jsonInput.get("downloadLocation").toString();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				System.out.println(directoryNameArray.length);
				/*
				 * Note 1: create a temporary file with same name of
				 * "fileToDownload" because java needs file at the location to
				 * rewrite contents in it.
				 */
				File f = new File(downloadLocation + "/" + fileToDownload);
				String DownloadTo = f.toString();
				f.createNewFile();

				CloudFileShare share = fileClient.getShareReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				CloudFileDirectory rootDir = share.getRootDirectoryReference();
				for (int i = 0; i < directoryNameArray.length; i++) {
					String directoryToCreate = directoryNameArray[i];
					CloudFileDirectory directory = rootDir.getDirectoryReference(directoryToCreate);
					if (directoryToCreate.equalsIgnoreCase(directoryNameArray[directoryNameArray.length - 1])) {
						CloudFile cloudFile = directory.getFileReference(fileToDownload);
						String tokenKey = testFileSAS(share, cloudFile);
						cloudFile.downloadToFile(DownloadTo);
						jsonOutput.put("status", "successful");
						jsonOutput.put("token", tokenKey);
					}
					rootDir = directory;
				}
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		} else {
			CloudBlobClient blobClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("blobAccountName") + ";" + "AccountKey=" + jsonInput.get("blobAccountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			String fileToDownload = jsonInput.get("fileName").toString();
			String downloadLocation = jsonInput.get("downloadLocation").toString();
			File f = new File(downloadLocation + "/" + fileToDownload);
			String DownloadTo = f.toString();
			f.createNewFile();

			try {
				blobClient = storageAccount.createCloudBlobClient();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				CloudBlobContainer container = blobClient.getContainerReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (container.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				CloudBlockBlob blob = container
						.getBlockBlobReference(jsonInput.get("directoryStructure") + "/" + jsonInput.get("fileName"));
				CloudBlobDirectory directoryOfFile = container
						.getDirectoryReference(jsonInput.get("directoryStructure").toString());
				CloudBlob blobToDownload = directoryOfFile.getBlockBlobReference(jsonInput.get("fileName").toString());
				if (blobToDownload.exists()) {
					blobToDownload.downloadToFile(DownloadTo);
					jsonOutput.put("status", "successful");
					String tokenKey = testBlobSaS(blob, container);
					jsonOutput.put("token", tokenKey);
				}
				return jsonOutput;
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zcon.azure.DAO.IFileHandlingDao#copyFile(org.json.simple.JSONObject)
	 * This method can copy file from one directory to another.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject copyFile(JSONObject jsonInput) throws IOException, InvalidKeyException, URISyntaxException {
		if (jsonInput.containsKey("accountName")) {
			CloudFileClient fileClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("accountName") + ";" + "AccountKey=" + jsonInput.get("accountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			try {
				fileClient = storageAccount.createCloudFileClient();
				String source = jsonInput.get("source").toString();
				String destination = jsonInput.get("destination").toString();
				String fileToCopy = jsonInput.get("fileToCopy").toString();
				String[] sourceNameArray = source.split("\\s*/\\s*");
				System.out.println(sourceNameArray.length);
				String[] destinationNameArray = destination.split("\\s*/\\s*");
				System.out.println(destinationNameArray.length);
				CloudFileShare share = fileClient.getShareReference(
						sourceNameArray[0].toLowerCase().replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				CloudFileDirectory rootDir = share.getRootDirectoryReference();
				CloudFileDirectory kidDir = rootDir;
				for (String name : destinationNameArray) {
					kidDir = kidDir.getDirectoryReference(name);
					kidDir.createIfNotExists();
				}
				CloudFileDirectory sourceDir = rootDir.getDirectoryReference(source);
				CloudFileDirectory destinationDir = rootDir.getDirectoryReference(destination);
				CloudFile sourceFile = sourceDir.getFileReference(fileToCopy);
				CloudFile destinationFile = destinationDir.getFileReference(fileToCopy);
				String tokenKey = testFileSAS(share, sourceFile);
				destinationFile.startCopy(sourceFile);
				jsonOutput.put("status", "successful");
				jsonOutput.put("token", tokenKey);
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		} else {
			CloudBlobClient blobClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("blobAccountName") + ";" + "AccountKey=" + jsonInput.get("blobAccountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			String fileToDownload = jsonInput.get("fileToCopy").toString();
			File f = new File("/home/zcon/AzureDownloadedFiles" + "/" + fileToDownload);
			String DownloadTo = f.toString();
			f.createNewFile();

			try {
				blobClient = storageAccount.createCloudBlobClient();
				String directoryName = jsonInput.get("source").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				CloudBlobContainer container = blobClient.getContainerReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (container.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				CloudBlockBlob blob = container
						.getBlockBlobReference(jsonInput.get("source") + "/" + jsonInput.get("fileToCopy"));
				CloudBlobDirectory directoryOfFile = container
						.getDirectoryReference(jsonInput.get("source").toString());

				CloudBlob blobToDownload = directoryOfFile
						.getBlockBlobReference(jsonInput.get("fileToCopy").toString());
				if (blobToDownload.exists()) {
					blobToDownload.downloadToFile(DownloadTo);
				}
				final String filePath = "/home/zcon/Test.txt";
				CloudBlockBlob blobToGet = container
						.getBlockBlobReference(jsonInput.get("destination") + "/" + jsonInput.get("fileToCopy"));
				File source = new File(filePath);
				if (blobToGet.exists()) {
					blobToGet.upload(new FileInputStream(source), source.length());
					jsonOutput.put("status", "successful");
					String tokenKey = testBlobSaS(blob, container);
					jsonOutput.put("token", tokenKey);
				} else {
					blobToGet.upload(new FileInputStream(source), source.length());
					jsonOutput.put("status", "successful");
					String tokenKey = testBlobSaS(blob, container);
					jsonOutput.put("token", tokenKey);
				}
				f.delete();
			} catch (Exception e) {
				e.printStackTrace();
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
			}
			return jsonOutput;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zcon.azure.DAO.IFileHandlingDao#checkDirectoryIfExist(org.json.simple.
	 * JSONObject) This method checks if particular directory exists.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject checkDirectoryIfExist(JSONObject jsonInput) throws InvalidKeyException, URISyntaxException {

		CloudFileClient fileClient = null;
		String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + jsonInput.get("accountName")
				+ ";" + "AccountKey=" + jsonInput.get("accountKey");
		System.out.println(storageConnectionString);
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		JSONObject jsonOutput = new JSONObject();
		try {
			fileClient = storageAccount.createCloudFileClient();
			String directoryName = jsonInput.get("directoryStructure").toString();
			String[] directoryNameArray = directoryName.split("\\s*/\\s*");
			System.out.println(directoryNameArray.length);

			CloudFileShare share = fileClient.getShareReference(
					directoryNameArray[0].toLowerCase().replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
			if (share.createIfNotExists()) {
				System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
			}
			CloudFileDirectory rootDir = share.getRootDirectoryReference();
			for (int i = 0; i <= directoryNameArray.length - 1; i++) {
				String directoryToCreate = directoryNameArray[i];
				CloudFileDirectory directory = rootDir.getDirectoryReference(directoryToCreate);
				if (directory.exists()) {
					jsonOutput.put("status", "directory already exists");
					jsonOutput.put("nameOfDirectoryToCheck", directory.getName());
				} else {
					jsonOutput.put("status", "directory does not exists");
					jsonOutput.put("nameOfDirectoryToCheck", directory.getName());
				}
				rootDir = directory;
			}
		} catch (Exception e) {
			e.printStackTrace();
			jsonOutput.put("status", "unsuccessful");
			jsonOutput.put("exception", e.toString());
		}
		return jsonOutput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zcon.azure.DAO.IFileHandlingDao#syncFiles(org.json.simple.JSONObject)
	 * This method will give all the file names, their path belonging to a
	 * particular directory.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@Override
	public JSONObject syncFiles(JSONObject jsonInput) throws InvalidKeyException, URISyntaxException {
		if (jsonInput.containsKey("accountName")) {
			CloudFileClient fileClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("accountName") + ";" + "AccountKey=" + jsonInput.get("accountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			ArrayList fileList = new ArrayList();
			try {
				fileClient = storageAccount.createCloudFileClient();
				String directoryName = jsonInput.get("directoryStructure").toString();

				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				System.out.println(directoryNameArray.length);

				CloudFileShare share = fileClient.getShareReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (share.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				CloudFileDirectory rootDir = share.getRootDirectoryReference();
				for (int i = 0; i < directoryNameArray.length; i++) {
					String directoryToCreate = directoryNameArray[i];
					CloudFileDirectory directory = rootDir.getDirectoryReference(directoryToCreate);
					if (i == directoryNameArray.length - 1) {
						for (ListFileItem fileItem : directory.listFilesAndDirectories()) {
							boolean isDirectory;
							if (isDirectory = fileItem.getClass() == CloudFileDirectory.class) {
								System.out.println("Directory Exists Here");
							} else {
								System.out.println("Name with files :" + fileItem.getUri().toString());
								String downloadLocation = "/home/zcon/AzureDownloadedFiles";
								String fileName[] = fileItem.getUri().toString().split("\\s*/\\s*");
								for (int j = 0; j < fileName.length; j++) {
									if (j == fileName.length - 1) {
										String fileNameWithExtension = fileName[j];
										File f = new File(downloadLocation + "/" + fileNameWithExtension);
										String DownloadTo = f.toString();
										f.createNewFile();
										CloudFile cloudFile = directory
												.getFileReference(fileNameWithExtension.replaceAll("%20", " "));
										System.out.println("fileName===========" + fileNameWithExtension);
										String tokenKey = testFileSAS(share, cloudFile);
										cloudFile.downloadToFile(DownloadTo);
										fileList.add(fileItem.getUri().toString() + "?" + tokenKey);
										f.delete();
									}
								}
							}
						}
					}
					rootDir = directory;
				}
				ArrayList fileNamesList = new ArrayList<>();
				for (int i = 0; i < fileList.size(); i++) {
					String fileName[] = fileList.get(i).toString().split("\\s*/\\s*");
					for (int j = 0; j < fileName.length; j++) {
						if (j == fileName.length - 1) {
							String fileNameReturn = fileName[j];
							String[] fileNameReturnArray = fileNameReturn.split("\\.");
							fileNamesList.add(fileNameReturnArray[0].replace("%20", " "));
						}
					}
				}
				jsonOutput.put("fileNamesList", fileNamesList);
				jsonOutput.put("fileList", fileList);
				jsonOutput.put("status", "successful");
			} catch (Exception e) {
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
				e.printStackTrace();
			}
			return jsonOutput;
		} else {
			
			CloudBlobClient blobClient = null;
			String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="
					+ jsonInput.get("blobAccountName") + ";" + "AccountKey=" + jsonInput.get("blobAccountKey");
			System.out.println(storageConnectionString);
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			JSONObject jsonOutput = new JSONObject();
			ArrayList fileList = new ArrayList<>();
			ArrayList fileNamesList = new ArrayList<>();
			ArrayList blobItemList = new ArrayList<>();

			try {
				blobClient = storageAccount.createCloudBlobClient();
				String directoryName = jsonInput.get("directoryStructure").toString();
				String[] directoryNameArray = directoryName.split("\\s*/\\s*");
				CloudBlobContainer container = blobClient.getContainerReference(directoryNameArray[0].toLowerCase()
						.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				if (container.createIfNotExists()) {
					System.out.println("New share created named as " + directoryNameArray[0].toLowerCase()
							.replaceAll("[-+.^:,!@#$%&*()_~`]", "").replaceAll("\\s+", ""));
				}
				
				//SetPublicContainerPermissions(container);
				CloudBlobDirectory directoryOfFile = container
						.getDirectoryReference(jsonInput.get("directoryStructure").toString());
				for (ListBlobItem blobItem : directoryOfFile.listBlobs()) {
					blobItemList.add(blobItem);
				}
				for (int q = 0; q < blobItemList.size(); q++) {
					if (blobItemList.get(q).getClass() == CloudBlobDirectory.class) {
						blobItemList.remove(q);
					}
				}
				System.out.println(blobItemList);
				for (int l = 0; l < blobItemList.size(); l++) {
					CloudBlob blob = (CloudBlob) blobItemList.get(l);
					if (blob.getUri().toString().contains("Temp.txt")) {
						System.out.println("Temp file was skipped");
					} else {
						String tokenKey = testBlobSaS(blob, container);
						fileList.add(blob.getUri().toString() + "?" + tokenKey);
						
						//fileList.add(blob.getUri().toString());
						
					}
				}
				System.out.println("size of blobItemList is=============" + blobItemList.size());
				for (int k = 0; k < fileList.size(); k++) {
					String fileItem = fileList.get(k).toString();
					String fileName[] = fileItem.split("\\s*/\\s*");
					for (int j = 0; j < fileName.length; j++) {
						if (j == fileName.length - 1) {
							String fileNameWithExtension = fileName[j];
							String[] parts = fileNameWithExtension.split("\\?");
							System.out.println("fileName===========" + fileNameWithExtension);
							fileNamesList.add(parts[0].replace("%20", " "));
						}
					}
				}
				jsonOutput.put("fileList", fileList);
				jsonOutput.put("fileNamesList", fileNamesList);
				jsonOutput.put("status", "successful");
				System.out.println(fileList);
			} catch (Exception e) {
				jsonOutput.put("status", "unsuccessful");
				jsonOutput.put("exception", e.toString());
				e.printStackTrace();
			}
			return jsonOutput;
		}
	}

	/**
	 * @param share
	 * @param file
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalArgumentException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 *             This method creates token when there will be certain
	 *             operation on file.
	 */
	@Test
	public String testFileSAS(CloudFileShare share, CloudFile file) throws InvalidKeyException,
			IllegalArgumentException, StorageException, URISyntaxException, InterruptedException {
		SharedAccessFilePolicy policy = createSharedAccessPolicy(EnumSet.of(SharedAccessFilePermissions.READ,
				SharedAccessFilePermissions.LIST, SharedAccessFilePermissions.WRITE), 100);
		FileSharePermissions perms = new FileSharePermissions();
		perms.getSharedAccessPolicies().put("readperm", policy);
		share.uploadPermissions(perms);
		CloudFile sasFile = new CloudFile(
				new URI(file.getUri().toString() + "?" + file.generateSharedAccessSignature(null, "readperm")));
		sasFile.download(new ByteArrayOutputStream());
		CloudFile fileFromUri = new CloudFile(
				PathUtility.addToQuery(file.getStorageUri(), file.generateSharedAccessSignature(null, "readperm")));
		assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
				fileFromUri.getServiceClient().getCredentials().getClass().toString());
		/* Note 1 : create credentials from sas */
		StorageCredentials creds = new StorageCredentialsSharedAccessSignature(
				file.generateSharedAccessSignature(policy, null, null));
		System.out.println("Generated SAS token is : " + file.generateSharedAccessSignature(policy, null, null));
		String token = file.generateSharedAccessSignature(policy, null, null);
		CloudFileClient client = new CloudFileClient(sasFile.getServiceClient().getStorageUri(), creds);
		CloudFile fileFromClient = client.getShareReference(file.getShare().getName()).getRootDirectoryReference()
				.getFileReference(file.getName());
		assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
				fileFromClient.getServiceClient().getCredentials().getClass().toString());
		assertEquals(client, fileFromClient.getServiceClient());
		return token;
	}

	/**
	 * @param sap
	 * @param expireTimeInSeconds
	 * @return SharedAccessFilePolicy
	 */
	private final static SharedAccessFilePolicy createSharedAccessPolicy(EnumSet<SharedAccessFilePermissions> sap,
			int expireTimeInSeconds) {

		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, expireTimeInSeconds);
		SharedAccessFilePolicy policy = new SharedAccessFilePolicy();
		policy.setPermissions(sap);
		policy.setSharedAccessExpiryTime(calendar.getTime());
		return policy;
	}

	/**
	 * @param share
	 * @param directory
	 * @return sas
	 * @throws InvalidKeyException
	 * @throws IllegalArgumentException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 *             This method will return token when there will be certain
	 *             operation on directory.
	 */
	@Test
	public String testDirectorySas(CloudFileShare share, CloudFileDirectory directory) throws InvalidKeyException,
			IllegalArgumentException, StorageException, URISyntaxException, InterruptedException {
		CloudFile file = directory.getFileReference("dirFile");
		file.create(512);
		SharedAccessFilePolicy policy = createSharedAccessPolicy(
				EnumSet.of(SharedAccessFilePermissions.READ, SharedAccessFilePermissions.LIST), 100);
		String sas = file.generateSharedAccessSignature(policy, null);
		CloudFileDirectory sasDir = new CloudFileDirectory(new URI(directory.getUri().toString() + "?" + sas));
		try {
			sasDir.downloadAttributes();
			fail("This should result in an authentication error.");
		} catch (StorageException ex) {
			assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ex.getHttpStatusCode());
		}
		sas = share.generateSharedAccessSignature(policy, null);
		sasDir = new CloudFileDirectory(new URI(directory.getUri().toString() + "?" + sas));
		sasDir.downloadAttributes();
		System.out.println("Generated SAS for directory is : " + sas);
		file.delete();
		return sas;
	}

	/**
	 * @param sap
	 * @param expireTimeInSeconds
	 * @return policy This method will return Shared Access Blob Policy when
	 *         there will be certain operation on directory.
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final static SharedAccessBlobPolicy createSharedAccessBlobPolicy(EnumSet<SharedAccessBlobPermissions> sap,
			int expireTimeInSeconds) {
		Calendar now = Calendar.getInstance();
		TimeZone timeZone = now.getTimeZone();
		System.out.println("Current TimeZone is : " + timeZone.getDisplayName());
		String x = timeZone.getDisplayName();
		String[] character = x.split(" ");
		String s = "";
		ArrayList zoneArray = new ArrayList<>();
		char zone = 0;
		for (int i = 0; i < character.length; i++) {
			s = character[i];
			zone = s.charAt(0);
			zoneArray.add(zone);
		}
		String timeZoneDynamic = zoneArray.toString().replace(",", "").replace(" ", "").replace("[", "").replace("]",
				"");
		System.out.println("Value of timezone==========" + timeZoneDynamic);
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone(timeZoneDynamic));
		cal.setTime(new Date());
		cal.add(Calendar.YEAR, expireTimeInSeconds);
		SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
		policy.setPermissions(sap);
		policy.setSharedAccessExpiryTime(cal.getTime());
		return policy;
	}

	/**
	 * @param blob
	 * @param container
	 * @return sas
	 * @throws InvalidKeyException
	 * @throws IllegalArgumentException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 *             This method will return token when there will be certain
	 *             operation on any blob.
	 */
	@Test
	public String testBlobSaS(CloudBlob blob, CloudBlobContainer container) throws InvalidKeyException,
			IllegalArgumentException, StorageException, URISyntaxException, InterruptedException {
		SharedAccessBlobPolicy sp = createSharedAccessBlobPolicy(
				EnumSet.of(SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.LIST), 100);
		BlobContainerPermissions perms = new BlobContainerPermissions();
		perms.getSharedAccessPolicies().put("readperm", sp);
		container.uploadPermissions(perms);
		String sas = blob.generateSharedAccessSignature(sp, null);
		CloudBlockBlob sasBlob = new CloudBlockBlob(
				new URI(blob.getUri().toString() + "?" + blob.generateSharedAccessSignature(null, "readperm")));
		sasBlob.download(new ByteArrayOutputStream());
		CloudBlob blobFromUri = new CloudBlockBlob(
				PathUtility.addToQuery(blob.getStorageUri(), blob.generateSharedAccessSignature(null, "readperm")));
		assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
				blobFromUri.getServiceClient().getCredentials().getClass().toString());
		StorageCredentials creds = new StorageCredentialsSharedAccessSignature(
				blob.generateSharedAccessSignature(null, "readperm"));
		CloudBlobClient bClient = new CloudBlobClient(sasBlob.getServiceClient().getStorageUri(), creds);
		CloudBlockBlob blobFromClient = bClient.getContainerReference(blob.getContainer().getName())
				.getBlockBlobReference(blob.getName());
		assertEquals(StorageCredentialsSharedAccessSignature.class.toString(),
				blobFromClient.getServiceClient().getCredentials().getClass().toString());
		assertEquals(bClient, blobFromClient.getServiceClient());
		return sas;
	}

	
}