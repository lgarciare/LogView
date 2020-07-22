package com.bbva.logview.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.IOException;

/**
 * Clase encargada de establecer conexion y ejecutar comandos SFTP.
 */
public class SFTPConnector {

	/**
	 * Sesion SFTP establecida.
	 */
	private Session session;

	/**
	 * Establece una conexion SFTP.
	 *
	 * @param username Nombre de usuario.
	 * @param password Contrasena.
	 * @param host     Host a conectar.
	 * @param port     Puerto del Host.
	 *
	 * @throws JSchException          Cualquier error al establecer conexión SFTP.
	 * @throws IllegalAccessException Indica que ya existe una conexion SFTP
	 *                                establecida.
	 */
	public void connect(String username, String password, String host, int port)
			throws JSchException, IllegalAccessException {
		if (this.session == null || !this.session.isConnected()) {
			JSch jsch = new JSch();

			this.session = jsch.getSession(username, host, port);
			this.session.setPassword(password);

			// Parametro para no validar key de conexion.
			this.session.setConfig("StrictHostKeyChecking", "no");

			this.session.connect();
		} else {
			throw new IllegalAccessException("Sesion SFTP ya iniciada.");
		}
	}

	/**
	 * Añade un archivo al directorio FTP usando el protocolo SFTP.
	 *
	 * @param ftpPath  Path del FTP donde se agregará el archivo.
	 * @param filePath Directorio donde se encuentra el archivo a subir en disco.
	 * @param fileName Nombre que tendra el archivo en el destino.
	 *
	 * @throws IllegalAccessException Excepción lanzada cuando no hay conexión
	 *                                establecida.
	 * @throws JSchException          Excepción lanzada por algún error en la
	 *                                ejecución del comando SFTP.
	 * @throws SftpException          Error al utilizar comandos SFTP.
	 * @throws IOException            Excepción al leer el texto arrojado luego de
	 *                                la ejecución del comando SFTP.
	 */
	public final void addFile(String ftpPath, String filePath, String fileName)
			throws IllegalAccessException, IOException, SftpException, JSchException {
		if (this.session != null && this.session.isConnected()) {

			// Abrimos un canal SFTP. Es como abrir una consola.
			ChannelSftp channelSftp = (ChannelSftp) this.session.openChannel("sftp");

			// Nos ubicamos en el directorio del FTP.
			channelSftp.cd(ftpPath);
			channelSftp.connect();

			System.out.println(String.format("Creando archivo %s en el " + "directorio %s", fileName, ftpPath));
			channelSftp.put(filePath, fileName);

			System.out.println("Archivo subido exitosamente");

			channelSftp.exit();
			channelSftp.disconnect();
		} else {
			throw new IllegalAccessException("No existe sesion SFTP iniciada.");
		}
	}

	/**
	 * Cierra la sesion SFTP.
	 */
	public final void disconnect() {
		this.session.disconnect();
	}
}
