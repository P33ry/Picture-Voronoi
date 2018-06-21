package peery.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import peery.file.FileHandler;

public class Log {

	public static Log log;
	public static final boolean silenceDebug = false,
	appendEvents = false, appendErrors = false;
	
	private final static String readmeText = 
			"This is the ReadMe for Mosaic.\n"
			+ "The folders that were created serve following purpose: \n"
			+ "resources				- Is the main folder where all images (Input AND Output are stored). \n"
			+ "resources%sImages		- Is the Input folder for all images you want to use as \"paint\" in the Mosaic. Put all your images in there!\n"
			+ "resources%sOutput		- Stores all resulting Mosaic pictures. There are all named Output-{number}.png \n"
			+ "resources%sTarget		- Is an Image (or symbolic Link to one) of your choice. Mosaic will try to create the mosaic after this inspiration. The name MUST be \"Target\" without any file extension or it will not be recognized."
			+ "resources%sERROR.log		- Is a log file where all non-fatal erros are stored. Take a peek if problems occur. \n"
			+ "resources%seventLog.log	- Is a log for more genereal events. Like progress, events and such with time stamps. Most useful for debugging problems.";	
	
	private String location;
	public final File eventFile, errorFile;
	private File perfFile;
	private BufferedWriter eventWriter, errorWriter, perfWriter;
	
	private ArrayList<Long> nanoTimes;
	
	public Log(String location, FileHandler fh){
		this.location = location;
		this.eventFile = new File(location+fh.fs+"eventLog.log");
		this.errorFile = new File(location+fh.fs+"ERROR.log");
		this.perfFile = new File(location+fh.fs+"perf.log");
		this.nanoTimes = new ArrayList<Long>();
		
		try {
			if(!this.eventFile.exists()){
				this.eventFile.createNewFile();
			}
			if(!this.errorFile.exists()){
				this.errorFile.createNewFile();
			}
			if(!this.perfFile.exists()){
				this.perfFile.createNewFile();
			}
			this.eventWriter = new BufferedWriter(new FileWriter(eventFile, appendEvents));
			this.errorWriter = new BufferedWriter(new FileWriter(errorFile, appendErrors));
			this.perfWriter = new BufferedWriter(new FileWriter(perfFile, true));
			perfWriter.write("\n\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static synchronized void initLog(String location, FileHandler fh){
		if(Log.log != null){
			return;
		}
		
		Log.log = new Log(location, fh);
	}
	
	public static void shutdownLog(){
		if(Log.log == null){
			return;
		}
		try {
			Log.log.errorWriter.close();
			Log.log.eventWriter.close();
			Log.log.perfWriter.close();
			Log.log = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log(int logLvl, String message){
		Log.log(LogLevel.values()[logLvl], message);
	}
	
	public static void log(LogLevel lv, String message){
		if(silenceDebug && LogLevel.Debug == lv){
			return;
		}
		Log.log.logs(lv.ordinal(), message);
	}
	
	@SuppressWarnings("unused")
	public synchronized void logs(int logLvl, String message){
		String prefix = LogLevel.values()[logLvl].toString();
		prefix = "["+prefix+"]";
		BufferedWriter logWriter;
		if(silenceDebug && logLvl == LogLevel.Debug.ordinal()){
			return;
		}
		
		if(logLvl == LogLevel.Error.ordinal()){
			logWriter = this.errorWriter;
		}
		else{
			logWriter = this.eventWriter;
		}
		String timeStamp = new java.util.Date().toString();
		String msg = "["+timeStamp+"]"+prefix+" "+message;
		System.out.println(msg);
		try {
			logWriter.write(msg+"\n");
			if(LogLevel.Info.ordinal() < logLvl){ //Saves perfomance?
				logWriter.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void perfLog(String message){
		long currentTime = System.nanoTime();
		String timeStamp = new java.util.Date().toString()+"|"+currentTime;
		this.nanoTimes.add(currentTime);
		try {
			perfWriter.write(message+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void finishPerfLog(){
		String[] stages = {
				"Indexing & Rasterization", // 2 - 1  1
				"Matching",					// 4 - 3  2
				"Placement",				// 6 - 5  3
				"Saving"					// 8 - 7  4
		};
		for(int i = 1; i <= stages.length; i++){
			long duration = nanoTimes.get(i*2) - nanoTimes.get(i*2-1);
			try {
				perfWriter.write(stages[i-1]+": "+duration+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			perfWriter.flush();
			perfWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void spawnReadMe(FileHandler fh){
		File readme = new File(Log.log.location+fh.fs+"README.txt");
		Log.log(LogLevel.Info, "Spawning README file at "+readme.getAbsolutePath());
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String rdme = readmeText.replaceAll("%s", fh.fs);
			bw.write(rdme);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	public static void main(String[] args){
		Log.initLog("/home/mosaic/Software_Projects/EclipseWorkspace/Picture Mosaic/resources/");
		Log.log(LogLevel.Debug, "Test!");
		Log.log(LogLevel.Error, "TEST ERROR");
	}*/
}
