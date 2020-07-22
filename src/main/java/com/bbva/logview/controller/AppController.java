package com.bbva.logview.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.bbva.logview.bnet.base.UserSession;
import com.bbva.logview.util.Constantes;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@Controller
public class AppController {

	@Autowired
	private Environment env;
	
	public Date fechaActual = new Date();
	
	SimpleDateFormat datePickerYYYYmmDD = new SimpleDateFormat("yyyy-MM-dd"); 

	@GetMapping({ "/", "/login" })
	public String index() {
		System.out.println("login");
		return "index";
	}

	@GetMapping("/logView")
	public String menu(HttpServletRequest request, ServletResponse response, HttpSession session2) throws IOException {

		String ambientePrevio = request.getParameter("ambientePrevio") != null ? request.getParameter("ambientePrevio"): "-1";
		String log = request.getParameter("log") != null ? request.getParameter("log") : "-1";
		String datepicker =  request.getParameter("datepicker") != "" ? request.getParameter("datepicker") : "-1";
				
		evaluacionCreaCarpetaLogTestCalidad(ambientePrevio,log);
		
		
		try {
			
			String filePath = new String("");
			
			if (ambientePrevio.equals("test")) {
				
				if (Constantes.LOG_OTP.equals(log)) {
					if(!"-1".equals(datepicker)) {
						filePath = obtenerFilePath(ambientePrevio,datepicker);	
						if(filePath!=null) {
							Session session = establecerSesion(ambientePrevio,filePath);
							String archivo = archivoBuscado(String.valueOf(datePickerYYYYmmDD.format(fechaActual)),datepicker.replace("/", "-"));
							obtenerArchivoLogOTP(ambientePrevio,session,filePath, archivo);
						}
					}
				} else if (Constantes.LOG_HOST.equals(log)) {			
					
				} else if (Constantes.LOG_DSE.equals(log)) {	
					
				}

			}else if (ambientePrevio.equals("calidad")) {
				if (Constantes.LOG_OTP.equals(log)) {
					
				} else if (Constantes.LOG_HOST.equals(log)) {			
					
				} else if (Constantes.LOG_DSE.equals(log)) {	
					
				}

			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}

		return "index";
	}

	private void evaluacionCreaCarpetaLogTestCalidad(String ambientePrevio, String log) {
		File carpetaTestLog = new File(env.getProperty("carpeta."+ambientePrevio+".log"));
		
		if (!carpetaTestLog.exists()) {
			carpetaTestLog.mkdirs();
		} 		
		
		if (Constantes.LOG_OTP.equals(log)) {
			File carpetaTestOtp = new File(env.getProperty("carpeta."+ambientePrevio+".log.otp"));
			if (!carpetaTestOtp.exists()) {
				carpetaTestOtp.mkdirs();
			}							
		} else if (Constantes.LOG_HOST.equals(log)) {			
			File carpetaTestHost = new File(env.getProperty("carpeta."+ambientePrevio+".log.host"));
			if (!carpetaTestHost.exists()) {
				carpetaTestHost.mkdirs();
			}
		} else if (Constantes.LOG_DSE.equals(log)) {	
			File carpetaTestDse = new File(env.getProperty("carpeta."+ambientePrevio+".log.dse"));
			if (!carpetaTestDse.exists()) {
				carpetaTestDse.mkdirs();
			}
		}
	}
	
	private String obtenerFilePath(String ambientePrevio,String datepicker) {
		
		String filePath = new String("");
		String archivo = new String("");
		
		
		System.out.println(String.valueOf(datePickerYYYYmmDD.format(fechaActual)));
		System.out.println(datepicker.replace("/", "-"));
		
		archivo = archivoBuscado(String.valueOf(datePickerYYYYmmDD.format(fechaActual)),datepicker.replace("/", "-"));
		
		filePath = env.getProperty("ruta."+ambientePrevio+".log.otp")+archivo;
		
		return filePath;
	}
	
	private String archivoBuscado(String fechaActual, String datepicker) {
		String archivo = new String("PAUServer.log");
		if(!fechaActual.equals(datepicker)) {
			archivo = archivo+"."+datepicker.replace("/", "-");
		}				
		return archivo;
	}

	private Session establecerSesion(String ambientePrevio, String filePath) {
		
		String usuarioSesion = env.getProperty("usuario.session");
		String usuario = env.getProperty("usuario."+ambientePrevio);
		String contrasena = env.getProperty("contrasena."+ambientePrevio);
		String servidor = env.getProperty("servidor."+ambientePrevio);
		int puerto = Integer.valueOf(env.getProperty("puerto."+ambientePrevio));
		Session session = null;
		
		try {
			JSch jSSH = new JSch();
			session = jSSH.getSession(usuario,servidor, puerto);
			UserSession userSession = new UserSession(usuarioSesion, null);
			session.setUserInfo(userSession);
			session.setPassword(contrasena);
			session.connect();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		return session;		
	}

	private void obtenerArchivoLogOTP(String ambientePrevio, Session session, String filePath, String archivo) {
		
		try {
		
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			  
			InputStream in = channelExec.getInputStream();
			channelExec.setCommand("tail -10f " + filePath);
			  
			channelExec.connect();
			  
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String linea = null;
			int index = 0;
			
			File file = new File(env.getProperty("carpeta."+ambientePrevio+".log.otp")+"\\"+archivo+".txt");
			FileWriter flwriter = null;				
			
			flwriter = new FileWriter(env.getProperty("carpeta."+ambientePrevio+".log.otp")+"\\"+archivo+".txt", true);
			BufferedWriter bfwriter = new BufferedWriter(flwriter);
			
			while ((linea = reader.readLine()) != null) {
				String traza = String.valueOf(String.valueOf(++index)) + " : " + linea;
				System.out.println(traza);	
				bfwriter.write(traza+"\n");
				bfwriter.close();
				System.out.println("Archivo creado satisfactoriamente..");
				
				channelExec.disconnect();
				session.disconnect();
				  
				System.out.println("FIN");
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
}
