package com.ebizon.appify.builder;

import java.io.File;

public class BuilderConfig {
	private String baseDir = "/AppifyCart";
	private String iosProjName = "Mofluid";
	private String iosBuildScript = "webapps/AppifyCartAdmin/scripts/IOSBuild.sh";
	private String androidBuildScript = "webapps/AppifyCartAdmin/scripts/AndroidBuild.sh";
	private String generateAppIconSplash = "webapps/AppifyCartAdmin/scripts/GenerateAppIconSplash.sh";
	private String deleteFileScript = "webapps/AppifyCartAdmin/scripts/DeleteFiles.sh";

	private static BuilderConfig instance = null;
	private String copyFilesScript = "webapps/AppifyCartAdmin/scripts/CopyIOSBuildFiles.sh";
	private String copyAndroidConfigScript = "webapps/AppifyCartAdmin/scripts/CopyBaseBuilderFile.sh";

	private BuilderConfig() {
	}

	public static BuilderConfig getInstance() {
		if(instance == null) {
			instance = new BuilderConfig();
		}
		return instance;
	}
	
	public String  getBaseDir(){
		return this.baseDir;
	}
	
	public String getIOSProjName(){
		return this.iosProjName;
	}


	public String getIOSBuildScript(){
		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File scriptFile = new File( catalinaBase, this.iosBuildScript);
		scriptFile.setExecutable(true, false);
		
		return scriptFile.getAbsolutePath();
	}
	
	public String getAndroidBuildScript(){
		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File scriptFile = new File( catalinaBase, this.androidBuildScript);
		scriptFile.setExecutable(true, false);
		
		return scriptFile.getAbsolutePath();
	}

	public String getGenerateAppIconSplash(){
		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File scriptFile = new File( catalinaBase, this.generateAppIconSplash);
		scriptFile.setExecutable(true, false);

		return scriptFile.getAbsolutePath();
	}

	public String getDeleteScript(){
		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File scriptFile = new File( catalinaBase, this.deleteFileScript);
		scriptFile.setExecutable(true, false);

		return scriptFile.getAbsolutePath();
	}


	public String getCopyAndroiConfigScript() {
		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File scriptFile = new File( catalinaBase, this.copyAndroidConfigScript);
		scriptFile.setExecutable(true, false);

		return scriptFile.getAbsolutePath();    }

    public String getCopyFilesScript() {
		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File scriptFile = new File( catalinaBase, this.copyFilesScript);
		scriptFile.setExecutable(true, false);

		return scriptFile.getAbsolutePath();    }
}
