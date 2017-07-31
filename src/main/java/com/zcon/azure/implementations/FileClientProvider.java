package com.zcon.azure.implementations;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.SharedAccessAccountPermissions;
import com.microsoft.azure.storage.SharedAccessAccountPolicy;
import com.microsoft.azure.storage.SharedAccessAccountResourceType;
import com.microsoft.azure.storage.SharedAccessAccountService;
import com.microsoft.azure.storage.SharedAccessProtocols;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.zcon.azure.implementations.FileHandlingDaoImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.EnumSet;
import java.util.Properties;
/**
 * Manages the storage file client
 */

/**
 * @author Vyankatesh
 *
 */
class FileClientProvider {
	/**
     * Validates the connection string and returns the storage file client.
     * The connection string must be in the Azure connection string format.
     *
     * @return The newly created CloudFileClient object
     *
     */
    static CloudFileClient getFileClientReference() throws RuntimeException, IOException, URISyntaxException, InvalidKeyException {
        // Retrieve the connection string
        Properties prop = new Properties();
        try {
            InputStream propertyStream = FileHandlingDaoImpl.class.getClassLoader().getResourceAsStream("application.properties");
            if (propertyStream != null) {
                prop.load(propertyStream);
            }
            else {
                throw new RuntimeException();
            }
        } catch (RuntimeException|IOException e) {
            System.out.println("\nFailed to load application.properties file.");
            throw e;
        }

        CloudStorageAccount storageAccount;
        try {
            storageAccount = CloudStorageAccount.parse(prop.getProperty("StorageConnectionString"));
        }
        catch (IllegalArgumentException|URISyntaxException e) {
            System.out.println("\nConnection string specifies an invalid URI.");
            System.out.println("Please confirm the connection string is in the Azure connection string format.");
            throw e;
        }
        catch (InvalidKeyException e) {
            System.out.println("\nConnection string specifies an invalid key.");
            System.out.println("Please confirm the AccountName and AccountKey in the connection string are valid.");
            throw e;
        }

        return storageAccount.createCloudFileClient();
    }
    public static final String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=zconazure;AccountKey=RxTHtwGtNCja489SrcfSBX9no3N3a/ug5Q2Vg36/dIRMYwI+hCc8H0yajoWAQoNc/xh31PYbAGAs9FqAkemrmQ==;EndpointSuffix=core.windows.net";
    public String getAccountSASToken() throws InvalidKeyException, URISyntaxException, StorageException {
        CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
        SharedAccessAccountPolicy policy = new SharedAccessAccountPolicy();
        policy.setPermissions(EnumSet.of(SharedAccessAccountPermissions.READ, SharedAccessAccountPermissions.WRITE, SharedAccessAccountPermissions.LIST));
        policy.setServices(EnumSet.of(SharedAccessAccountService.BLOB, SharedAccessAccountService.FILE) );
        policy.setResourceTypes(EnumSet.of(SharedAccessAccountResourceType.SERVICE));
        policy.setSharedAccessExpiryTime(Date.from(ZonedDateTime.now(ZoneOffset.UTC).plusHours(24L).toInstant()));
        policy.setProtocols(SharedAccessProtocols.HTTPS_ONLY);
        System.out.println("Account SAS Token is : "+account.generateSharedAccessSignature(policy));
        return account.generateSharedAccessSignature(policy);
    }

}
