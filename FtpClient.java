import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * 
 * @author Abdelaziz Khabthani
 *
 */

public class FtpClient {

	private FTPClient ftpClient;

	public FtpClient() {
		ftpClient = new FTPClient();
	}

	private void showServerReply(FTPClient ftpClient) {
		String[] replies = ftpClient.getReplyStrings();
		if (replies != null && replies.length > 0) {
			for (String aReply : replies) {
				System.out.println("SERVER: " + aReply);
			}
		}
	}

	public boolean ftpConnect(String server, String username, String password,
			int port) {
		ftpDisconnect();
		try {
			ftpClient.connect(server, port);
			showServerReply(ftpClient);
			int replyCode = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				System.out.println("Operation failed. Server reply code: "
						+ replyCode);
				return false;
			}
			boolean success = ftpClient.login(username, password);
			showServerReply(ftpClient);
			if (!success) {
				System.out.println("Could not login to the server");
				return false;
			} else {
				ftpClient.enterLocalPassiveMode();
				ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
				showServerReply(ftpClient);
				System.out.println("Logged in server");
				return true;
			}
		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	private void ftpDisconnect() {
		try {
			if (ftpClient.isConnected()) {
				ftpClient.logout();
				showServerReply(ftpClient);
				ftpClient.disconnect();
				System.out.println("Disconnected from server");
			}
		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private File inputStreamToTempFile(InputStream in, String prefix,
			String Suffix) throws IOException {
		final File temporaryFile = File.createTempFile(prefix, Suffix);
		temporaryFile.deleteOnExit();
		try (FileOutputStream out = new FileOutputStream(temporaryFile)) {
			IOUtils.copy(in, out);
		}
		return temporaryFile;
	}

	public File ftpGetFile(String server, String username, String password,
			int port, String remoteFilePath, String temporaryPrefix,
			String temporarySuffix) {
		File temporaryFile = null;
		try {
			if (ftpConnect(server, username, password, port)) {
				InputStream inputStream = ftpClient
						.retrieveFileStream(remoteFilePath);
				temporaryFile = inputStreamToTempFile(inputStream,
						temporaryPrefix, temporarySuffix);
				boolean success = ftpClient.completePendingCommand();
				if (success) {
					System.out
							.println("File has been downloaded successfully.");
					inputStream.close();
				} else {
					System.out.println("File has not been downloaded.");
					inputStream.close();
				}
			} else {
				System.out.println("File has not been downloaded.");
				return temporaryFile;
			}
		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			ftpDisconnect();
		}
		return temporaryFile;
	}

	public boolean ftpDeleteFile(String server, String username,
			String password, int port, String remoteFilePath) {
		try {
			if (ftpConnect(server, username, password, port)) {
				boolean deleted = ftpClient.deleteFile(remoteFilePath);
				showServerReply(ftpClient);
				if (deleted) {
					System.out.println("The file was deleted successfully.");
				} else {
					System.out.println("Could not delete the  file.");
				}
				return deleted;
			} else {
				System.out.println("Could not delete the  file.");
				return false;
			}
		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		} finally {
			ftpDisconnect();
		}
	}

	public boolean ftpUploadFile(String server, String username,
			String password, int port, String remoteFilePath,
			String localFilePath) {
		try {
			if (ftpConnect(server, username, password, port)) {
				File localFile = new File(localFilePath);
				InputStream inputStream = new FileInputStream(localFile);

				boolean done = ftpClient.storeFile(remoteFilePath, inputStream);
				showServerReply(ftpClient);
				inputStream.close();
				if (done) {
					System.out.println("File was uploaded successfully.");
				} else {
					System.out.println("File was not uploaded.");
				}
				return done;
			} else {
				System.out.println("File was not uploaded.");
				return false;
			}
		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		} finally {
			ftpDisconnect();
		}
	}
}
