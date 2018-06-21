package peery.file;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

import peery.log.Log;
import peery.log.LogLevel;

public class FileHandler {
	
	public File sourceFolder, InputImagesFolder, TargetImageFile, OutputFolder, indexFile;
	/*
	 * sourcePath/ -> all ressources
	 * sourcePath/Images/ -> Input pictures folder
	 * sourcePath/Target -> Target picture file
	 * sourcePath/Output/ -> Output picture folder
	 */
	public final String fs;
	
	public FileHandler(String sourcePath){
		if(!System.getProperty("os.name").startsWith("Windows")){
			fs = "/";
		}else{
			fs = "\\";
		}
		this.sourceFolder = new File(sourcePath);
		this.InputImagesFolder = new File(sourceFolder.getAbsolutePath()+fs+"Images");
		this.TargetImageFile = new File(sourceFolder.getAbsolutePath()+fs+"Target.png");
		this.OutputFolder = new File(sourceFolder.getAbsolutePath()+fs+"Output");
		this.indexFile = new File(this.InputImagesFolder.getAbsolutePath()+"Index.txt");
		if(!this.sourceFolder.exists()){
			this.sourceFolder.mkdirs();
		}
		Log.initLog(this.sourceFolder.getAbsolutePath(), this);
		if(fs == "\\"){
			Log.log(LogLevel.Debug, "Assumed Windows like Folder declaration. Therefore using "+fs+" as a separator.");
		}else{
			Log.log(LogLevel.Debug, "Detected Linux or OSX.");
		}
		if(!this.validateFolderStructure()){
			Log.log(LogLevel.Error, "Could not validate folder structure! Things are missing!");
			Log.spawnReadMe(this);
			System.exit(1);
		}
	}
	
	public boolean validateFolderStructure(){
		if(this.sourceFolder.isDirectory()){
			Log.log(LogLevel.Debug, "Detected source folder at "+this.sourceFolder.getAbsolutePath());
			if(this.InputImagesFolder.isDirectory()){
				Log.log(LogLevel.Debug, "Detected Input folder at "+this.InputImagesFolder.getAbsolutePath());
				if(this.OutputFolder.isDirectory()){
					Log.log(LogLevel.Debug, "Detected Output folder at "+this.OutputFolder.getAbsolutePath());
				}else{
					Log.log(LogLevel.Info, "No Output folder found.");
					Log.log(LogLevel.Info, "Creating one at "+this.OutputFolder.getAbsolutePath());
					this.OutputFolder.mkdirs();
				}
				if(this.TargetImageFile.isFile()){
					Log.log(LogLevel.Debug, "Detected Target Image at "+this.TargetImageFile.getAbsolutePath());
					if(!this.indexFile.isDirectory()){
						Log.log(LogLevel.Debug, "Found no directory blocking the index file.");
						return true;
					}
					else{
						Log.log(LogLevel.Error, "Following folder collides with the index file name: "+this.indexFile.getAbsolutePath());
						return false;
					}
				}else{
					Log.log(LogLevel.Critical, "No Target Image found! Exiting...");
					return false;
				}
			}else{
				Log.log(LogLevel.Critical, "No Input folder found.");
				Log.log(LogLevel.Critical, "Creating one at "+this.InputImagesFolder.getAbsolutePath());
				this.InputImagesFolder.mkdirs();
			}
		}else{
			Log.log(LogLevel.Critical, "No source folder found (redundant check).");
			Log.log(LogLevel.Critical, "Creating one at "+this.sourceFolder.getAbsolutePath());
			this.sourceFolder.mkdirs();
		}
		Log.log(LogLevel.Critical, "Folder validation failed. There could be a permission problem "
				+ "or a folder needed to be created! Please look for earlier errors.");
		return false;
	}
	
	public ArrayList<String> listInputFiles(){
		Log.log(LogLevel.Info, "Listing files inside "+this.InputImagesFolder.getName()+" ...");
		ArrayList<String> fileList = new ArrayList<String>();
		for(File f: this.InputImagesFolder.listFiles()){
			if(f.isFile()){
				fileList.add(f.getAbsolutePath());
			}
		}
		return fileList;
	}
	
	public BufferedImage loadImage(File file){
		Log.log(LogLevel.Info, "Loading image "+file.getName()+" ...");
		if(file.isFile() && file.canRead()){
			BufferedImage img;
			try {
				img = ImageIO.read(file);
				Log.log(LogLevel.Debug, "Loaded image "+file.getName()+" !");
				return img;
			} catch (IOException e) {
				Log.log(LogLevel.Debug, "File "+file.getPath()+" failed to load as an Image. What did I just read?");
				e.printStackTrace();
				return null;
			}
		}
		else{
			Log.log(LogLevel.Info, "Can't read file "+file.getPath()+" ! It could be a directory or no read permissions.");
			return null;
		}
	}
	
	public void saveImage(BufferedImage img, File file){
		Log.log(LogLevel.Info, "Saving image as file "+file.getAbsolutePath());
		Log.log(LogLevel.Info, "This could take a moment ...");
		try {
			ImageIO.write(img, "png", file);
		} catch (IOException e) {
			Log.log(LogLevel.Critical, "Couldn't write image "+file.getName()+" to file! Are write permissions missing?"
					+ " Attempted to write at "+file.getAbsolutePath());
			e.printStackTrace();
		}
		Log.log(LogLevel.Info, "Saved "+file.getName()+" !");
	}
	
	/**
	 * Plainly appends the given file and rgb value to the end of the index.
	 * 
	 * CHECK FOR DUPLICATES BEFOREHAND.
	 * @param file
	 * @param rgb
	 */
	public synchronized void appendToIndex(File file, int rgb){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile, true));
			bw.write(rgb+";"+file.getName()+"\n");
			bw.flush();
			bw.close();
			Log.log(LogLevel.Info, "Wrote index entry for "+file.getName()+" !");
		} catch (IOException e) {
			Log.log(LogLevel.Critical, "Couldn't create or write index file "+indexFile.getAbsolutePath()+" ."
					+ "Are write permissions missing?");
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Loads an index entry for a single file.
	 * 
	 * Loads and parses the whole index in the background if not given the indexData.
	 * @param indexData
	 * @param file
	 * @return
	 */
	public Integer loadIndexEntry(HashMap<String, Integer> indexData, File file){
		Log.log(LogLevel.Debug, "Searching for index data of "+file.getName()+" ...");
		if(indexData == null){
			indexData = loadIndex();
		}
		return indexData.get(file.getName());
	}
	
	/**
	 * Loads the whole index file into a Hashmap.
	 * The second value (file name) is used as a String-key, the rgb value is the int-value.
	 * @param file
	 * @return
	 */
	public HashMap<String, Integer> loadIndex(){
		if(!this.indexFile.exists()){
			Log.log(LogLevel.Info, "No Index file found. Nothing to load then. Creating empty one ...");
			try {
				indexFile.createNewFile();
			} catch (IOException e) {
				Log.log(LogLevel.Error, "Couldn't create Index file! Are write permissions missing?");
				e.printStackTrace();
			}
			return null;
		}
		HashMap<String, Integer> indexData = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(new BufferedReader(new FileReader(this.indexFile)));
			while(sc.hasNext()){
				@SuppressWarnings("rawtypes")
				ArrayList data = parseIndexData(sc.nextLine());
				indexData.put((String)data.get(1), (int)data.get(0));
			}
			sc.close();
		} catch (FileNotFoundException e) {
			Log.log(LogLevel.Critical, "Could not open index file! Do I have read permissions?");
			e.printStackTrace();
		}
		Log.log(LogLevel.Debug, "Sucessfully loaded index!");
		return indexData;
	}
	
	public void overwriteIndex(HashMap<String, Integer> index){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile, false));
			for(String key: index.keySet()){
				bw.write(index.get(key)+";"+key+"\n");
			}
			bw.flush();
			bw.close();
			Log.log(LogLevel.Info, "Overwrote index file with new index!");
		} catch (IOException e) {
			Log.log(LogLevel.Critical, "Couldn't create or write index file "+indexFile.getAbsolutePath()+" ."
					+ "Are write permissions missing?");
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Parses a line of indexData into an ArrayList containing the rgb int and the name
	 * @param indexData
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList parseIndexData(String indexData){
		String[] data = indexData.split(";");
		ArrayList result = new ArrayList();
		result.add(Integer.parseInt(data[0]));
		result.add(data[1]);
		
		return result;
	}
	
	// could use a single image variant to check synchronized to the classification (saves IO)
	public Dimension loadBiggestDimension(){
		File[] files = this.InputImagesFolder.listFiles();
		int width = 0, height = 0, img_count = 0;
		for(File f: files){
			if(f.isFile() && f.canRead()){
				BufferedImage img = loadImage(f);
				if(img == null){
					continue;
				}
				img_count++;
				if(width < img.getWidth()){
					width = img.getWidth();
				}
				if(height < img.getHeight()){
					height = img.getHeight();
				}
			}
			else{
				Log.log(LogLevel.Info, "Can't read file"+f.toString()+"! It could be a directory or no read permissions.");
			}
		}
		Log.log(LogLevel.Info, img_count+" image(s) were loaded...");
		if(width == 0 || height == 0){
			Log.log(LogLevel.Critical, "Incomplete or no dimension values! Could I load any Image?");
		}
		Log.log(LogLevel.Debug, "Biggest dimension is "+width+"x"+height);
		return new Dimension(width, height);
	}
	
	// Would probably kill memory (and performance).
	@Deprecated
	public BufferedImage[] loadAllImages(){
		File[] files = this.InputImagesFolder.listFiles();
		ArrayList<BufferedImage> imgs = new ArrayList<BufferedImage>();
		int img_count = 0;
		for(File f: files){
			BufferedImage img = loadImage(f);
			if(img == null){
				continue;
			}
			imgs.add(img);
			img_count++;
		}
		if(imgs.size() == 0){
			Log.log(LogLevel.Critical, "No Images found in "+this.InputImagesFolder.getAbsolutePath());
			return null;
		}
		Log.log(LogLevel.Info, img_count+" image(s) were loaded...");
		BufferedImage[] bfs = new BufferedImage[imgs.size()];
		for(int i = 0; i < imgs.size(); i++){
			bfs[i] = imgs.get(i);
		}
		return bfs;
	}

}
