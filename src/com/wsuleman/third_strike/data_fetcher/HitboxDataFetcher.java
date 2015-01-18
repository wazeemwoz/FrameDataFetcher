/**
 * 
 */
package com.wsuleman.third_strike.data_fetcher;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.wsuleman.third_strike.data_fetcher.pojos.CharSDO;

/**
 * @author Waseem Suleman
 * Using the jsoup library this class will collect data from
 * HTML files to acquire third strike hit box data.
 * 
 * Currently set up to translate ESN's website data.
 */
public class HitboxDataFetcher {
	public static final char seperator = '\n';
	private static String output_destination = "/home/vhd/git/FrameDataFetcher/Resources/extracted/hitBox/";
	String baseUrl = "http://ensabahnur.free.fr/BastonNew/hitboxesDisplay.php?sMode=f&";
	private static String[] moveTypeList = {
		"fd_normals","fd_specials","fd_supers","fd_gj_normals","fd_gj_specials","fd_misc"
	};
	private static String[] charNames = {
			"Gill", "Alex", "Ryu", "Yun",
			"Dudley", "Necro", "Hugo", "Ibuki",
			"Elena", "Oro", "Yang", "Ken",
			"Sean", "Urien", "Gouki", "",
			"Chun Li", "Makoto", "Q", "Twelve",
			"Remy"
	};
	private static String[] miscNames = {
		"jumpNeutral","jumpBackward", "jumpForward", "superjumpNeutral", 
		"superjumpBackward", "superjumpForward", "dashBackwardFull", "dashBackward", 
		"dashForwardFull", "dashForward", "wakeUp", "wakeUpQuickRoll"
	};
	
	public HitboxDataFetcher(){
		//fetchAllCharData();
		writeCharHitBoxesToFile(output_destination + charNames[20] + "/", fetchCharData(20));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HitboxDataFetcher hb = new HitboxDataFetcher();
		
		//Map<String, Map<String, MoveHitBoxData>> charData = hb.fetchCharData(2);
		//hb.writeCharHitBoxesToFile(HitboxDataFetcher.output_destination + "/"
		//		+ charNames[2] + "/", charData);
	}
	
	public MoveHitBoxData fetchMoveImages(int charId, int moveTypeId, String moveId, String hbdata){
		if(hbdata.equals("")){
			return new MoveHitBoxData();
		}
		
		GsonBuilder builder = new GsonBuilder();
		MoveHitBoxData moveData = new MoveHitBoxData();
		moveData.move_key = charId+"/"+moveTypeList[moveTypeId]+"/"+moveId;
		moveData.sprites = new ArrayList<FrameHitBoxData>();
		
		LinkedTreeMap o = (LinkedTreeMap) builder.create().fromJson(hbdata,
				Object.class);
		
		for(int i = 0; true; i++) {
			FrameHitBoxData frameData = new FrameHitBoxData();
			String frameId = "" + i;
			if (frameId.length() == 1) {
				frameId = "00" + frameId;
			} else if (frameId.length() == 2) {
				frameId = "0" + frameId;
			}
			if(o.get(frameId) == null){
				break;
			}
			frameData.json = ((LinkedTreeMap) o.get(frameId)).toString();
			String pngFileName = (String) ((LinkedTreeMap) o.get(frameId))
					.get("pngFileName");
			
			frameData.sprite = fetchImageFromWeb(
					getUrlForMoveCanvas(charId, moveTypeId, moveId,
							pngFileName));
			moveData.sprites.add(frameData);
		}
		return moveData;
	}
	
	public MoveHitBoxData fetchMiscImages(int charId, String moveName, String hbdata){
		if(hbdata.equals("")){
			return new MoveHitBoxData();
		}
		GsonBuilder builder = new GsonBuilder();
		MoveHitBoxData moveData = new MoveHitBoxData();
		moveData.move_key = charId + "/" + moveTypeList[5] + "/" + moveName;
		moveData.sprites = new ArrayList<FrameHitBoxData>();

		LinkedTreeMap o = (LinkedTreeMap) builder.create().fromJson(hbdata,
				Object.class);

		for (int i = 0; true; i++) {
			FrameHitBoxData frameData = new FrameHitBoxData();
			String frameId = "" + i;
			if (frameId.length() == 1) {
				frameId = "00" + frameId;
			} else if (frameId.length() == 2) {
				frameId = "0" + frameId;
			}
			if (o.get(frameId) == null) {
				break;
			}
			frameData.json = ((LinkedTreeMap) o.get(frameId)).toString();
			String pngFileName = (String) ((LinkedTreeMap) o.get(frameId))
					.get("pngFileName");
			frameData.sprite = fetchImageFromWeb(getUrlForOtherCanvas(charId,
					moveName, pngFileName));

			moveData.sprites.add(frameData);
		}
		return moveData;
	}
	
	private BufferedImage fetchImageFromWeb( String weblink){
		URL url;
		BufferedImage img = null;
		try {
			url = new URL(weblink);
			img = ImageIO.read(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return fetchImageFromWeb(weblink);
		}
		return img;
	}
	
	public Map<String, Map<String, Map<String, MoveHitBoxData>>> fetchAllCharData(){
		Map<String, Map<String, Map<String, MoveHitBoxData>>> hbData = 
				new HashMap<String, Map<String, Map<String, MoveHitBoxData>>>();
		
		for(int i = 0; i < charNames.length; i++){
			//hbData.put(charNames[i], fetchCharData(i));
			writeCharHitBoxesToFile(output_destination + charNames[i] + "/", fetchCharData(i));
		}
		
		return hbData;
	}
	
	public Map<String, Map<String, MoveHitBoxData>> fetchCharData(int charId){
		int move_cnt = 1; int move_max = 0;
		CharSDO _char = fetchCharDataFromFile(charNames[charId]);
		Map<String, Map<String, MoveHitBoxData>> charData = 
				new HashMap<String, Map<String, MoveHitBoxData>>();
		
		if(_char != null){
			move_max = move_max + _char.normals.size();
			charData.put(moveTypeList[0], fetchAllMoveTypeHitBoxData(charId, 0, move_cnt, move_max));
			move_cnt = move_max+1;

			move_max = move_max + _char.specials.size();
			charData.put(moveTypeList[1], fetchAllMoveTypeHitBoxData(charId, 1, move_cnt, move_max));
			move_cnt = move_max+1;

			move_max = move_max + _char.supers.size();
			charData.put(moveTypeList[2], fetchAllMoveTypeHitBoxData(charId, 2, move_cnt, move_max));
			move_cnt = move_max+1;
		
			if(charNames[charId].equals("Yun")){
				move_max = move_max + _char.genei_jin_normals.size();
				charData.put(moveTypeList[3], fetchAllMoveTypeHitBoxData(charId, 3, move_cnt, move_max));
				move_cnt = move_max+1;

				move_max = move_max + _char.genei_jin_specials.size();
				charData.put(moveTypeList[4], fetchAllMoveTypeHitBoxData(charId, 4, move_cnt, move_max));
				move_cnt = move_max+1;
			}
			
			move_max = move_max + _char.misc.size();
			charData.put(moveTypeList[5], fetchAllOtherHitBoxData(charId));
			
		}
		return charData;
	}
	
	public Map<String, MoveHitBoxData> fetchAllOtherHitBoxData(int charId){
		Map<String, MoveHitBoxData> movs = new HashMap<String, MoveHitBoxData>();
		for(int i = 0; i < miscNames.length; i++){
			movs.put(miscNames[i], fetchOtherHitBoxData(i, charId));
		}
		return movs;
	}
	
	public MoveHitBoxData fetchOtherHitBoxData(int miscName, int charId){
		Document doc;
		try {
			doc = Jsoup.connect(getUrlForMisc(charId, miscName)).get();
			String json = extractJSONFromHTML(doc);
			System.out.println("Retrieved move : " + charId + "-" + miscName);
			return fetchMiscImages(charId, miscNames[miscName], json);
		} catch (SocketTimeoutException e) {
			return fetchOtherHitBoxData(miscName, charId);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public Map<String, MoveHitBoxData> fetchAllMoveTypeHitBoxData(int charId, int type, int init, int end){
		Map<String, MoveHitBoxData> movs = new HashMap<String, MoveHitBoxData>();
		for(int i = init; i <= end; i++){
			movs.put(""+i, fetchMoveHitBoxData(charId, type, i));
		}
		System.out.println();
		return movs;
	}
	
	public MoveHitBoxData fetchMoveHitBoxData(int charId, int type, int moveId){
			Document doc;
			try {
				doc = Jsoup.connect(getUrlForMove(charId, type, ""+moveId)).get();
				String json = extractJSONFromHTML(doc);
				System.out.println("Retrieved move : " + charId + "-" + type  + "-" + moveId);
				return fetchMoveImages(charId, type, ""+moveId, json);
			} catch (SocketTimeoutException e) {
				return fetchMoveHitBoxData(charId, type, moveId);
			} catch (IOException e) {
				e.printStackTrace();
			} 
			return null;
	}
	
	private String extractJSONFromHTML(Document doc){
        try{
			Elements scripts = doc.getElementsByTag("script");
			for( Element script : scripts)
			{
				if(script.data().contains("aFramesInfos = {")){
					String[] jslines = script.data().split("\n");
					for(String jsline : jslines){
						if(jsline.contains("aFramesInfos = {")){
							jsline = jsline.replace("aFramesInfos = ", "");
							jsline = jsline.substring(0, jsline.length()-1);
							//System.out.println(jsline);
							return jsline;
						}
					}
				}
			}
        } catch (Exception e) {
			e.printStackTrace();
		}
        return "";
	}

	private String getUrlForMove(int charId, int typeId, String moveId){
		String url =  baseUrl + "iChar=" + charId + "&sMoveType=" + moveTypeList[typeId] + "&sAction=w&iMove=" + moveId;
		System.out.println("Fetching:" + url);
		return url;
	}
	
	private String getUrlForMisc(int charId, int miscId){
		
		String url =   baseUrl + "iChar=" + charId + "&iMove="+miscNames[miscId]+"&sMoveType="+miscNames[miscId];
		System.out.println("Fetching:" + url);
		return url;
	}
	
	private String getUrlForMoveCanvas(int iCharId, int moveTypeId, String moveId, String pngFileName){
		String charId = ""+iCharId;
		if (charId.length() == 1) {
			charId = "0" + charId;
		}

		if (moveId.length() == 1) {
			moveId = "00" + moveId;
		} else if (moveId.length() == 2) {
			moveId = "0" + moveId;
		}
		String weblink = "http://ensabahnur.free.fr/___dataRepository/"
				+ charId + "/" + moveTypeList[0] + "/" + moveId + "/"
				+ pngFileName;
		return weblink;
	}
	
	private String getUrlForOtherCanvas(int iCharId, String moveId, String pngFileName){
		String charId = ""+iCharId;
		if (charId.length() == 1) {
			charId = "0" + charId;
		}
		String weblink = "http://ensabahnur.free.fr/___dataRepository/"
				+ charId + "/" + moveId + "/"
				+ pngFileName;
		return weblink;
	}
	
    private CharSDO fetchCharDataFromFile (String charName) {
		String path = "/home/vhd/git/FrameDataFetcher/Resources/extracted/" + charName + ".txt";
        String sJson = "";
        CharSDO charSDO = new CharSDO();
        try {
			File file = new File(path);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				System.out.println("File doesnt exist");
				return null;
			}
			Scanner scanner = new Scanner( file );
			sJson = scanner.useDelimiter("\\A").next();
			scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            Gson gson = new Gson();
            charSDO = gson.fromJson(sJson, CharSDO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return charSDO;
    }
    
	private void writeAllHitBoxesToFile(String outputFolder, Map<String, Map<String, Map<String, MoveHitBoxData>>> data){
    	for (Entry<String, Map<String, Map<String, MoveHitBoxData>>> entry : data.entrySet()) {
    		writeCharHitBoxesToFile(outputFolder + entry.getKey() + "/", entry.getValue());
    	}
    }
    
    private void writeCharHitBoxesToFile(String outputFolder, Map<String, Map<String, MoveHitBoxData>> data){
    	for (Entry<String, Map<String, MoveHitBoxData>> entry : data.entrySet()) {
    		writeMovesHitBoxesToFile(outputFolder + entry.getKey() + "/", entry.getValue());
    	}
    }
    
    private void writeMovesHitBoxesToFile(String outputFolder, Map<String, MoveHitBoxData> data){
    	for (Entry<String, MoveHitBoxData> entry : data.entrySet()) {
    		writeMoveHitBoxesToFile(outputFolder + entry.getKey() + "/", entry.getValue());
    	}
    }
    
	private void writeMoveHitBoxesToFile(String pathOutput, MoveHitBoxData data){
        try {
        	if(data == null){
        		return;
        	}
        	if(data.sprites == null){
        		return;
        	}
        	
			File file = new File(pathOutput);
			 
			// if file or folder doesnt exists, then create it
			if (!file.exists()) {
				file.mkdirs();
			}

        	for(int i = 0; i < data.sprites.size(); i++){
        		FrameHitBoxData frameData = data.sprites.get(i);
	        	String fileOutput = pathOutput + i + ".txt";		
				file = new File(fileOutput);	
	        	if (!file.exists()) {
	    			file.createNewFile();
				}
	 
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write(data.sprites.get(i).json);
				bw.close();

				file = new File(pathOutput + i + ".png");
				ImageIO.write(data.sprites.get(i).sprite, "png", file);
				
				System.out.println("Done writing to file:" + fileOutput);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
