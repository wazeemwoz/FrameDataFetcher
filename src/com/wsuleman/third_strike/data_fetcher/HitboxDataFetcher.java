/**
 * 
 */
package com.wsuleman.third_strike.data_fetcher;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
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
			"Gill", "Alex", "Ryu", "yun",
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
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HitboxDataFetcher hb = new HitboxDataFetcher();
		Map<String, Map<String, String>> charData = hb.fetchCharData(2);
		hb.writeCharHitBoxesToFile(HitboxDataFetcher.output_destination, charData);
		String json = charData.get(moveTypeList[0]).get("1");
	}
	
	public void fetchMoveImages(int charId, int moveTypeId, String moveId, String hbdata){
		// http://ensabahnur.free.fr/___dataRepository/
		GsonBuilder builder = new GsonBuilder();
		LinkedTreeMap o = (LinkedTreeMap) builder.create().fromJson(hbdata,
				Object.class);
		for (int i = 0; true; i++) {
			String frameId = "" + i;
			if (frameId.length() == 1) {
				frameId = "00" + frameId;
			} else if (frameId.length() == 2) {
				frameId = "0" + frameId;
			}
			if(o.get(frameId) == null){
				break;
			}
			String pngFileName = (String) ((LinkedTreeMap) o.get(frameId))
					.get("pngFileName");
			fetchImageFromWeb(
					charNames[charId] + "/" + moveTypeList[moveTypeId] + "/"
							+ moveId + "/" + pngFileName,
					getUrlForMoveCanvas("" + charId, moveTypeId, moveId,
							pngFileName));
		}
	}
	/*
	public void fetchMiscImages(int charId, String moveName, String hbdata){
		// http://ensabahnur.free.fr/___dataRepository/
		GsonBuilder builder = new GsonBuilder();
		LinkedTreeMap o = (LinkedTreeMap) builder.create().fromJson(hbdata,
				Object.class);
		for (int i = 0; true; i++) {
			String frameId = "" + i;
			if (frameId.length() == 1) {
				frameId = "00" + frameId;
			} else if (frameId.length() == 2) {
				frameId = "0" + frameId;
			}
			if(o.get(frameId) == null){
				break;
			}
			String pngFileName = (String) ((LinkedTreeMap) o.get(frameId))
					.get("pngFileName");
			fetchImageFromWeb(
					charNames[charId] + "/" + moveTypeList[moveTypeId] + "/"
							+ moveId + "/" + pngFileName,
					getUrlForMoveCanvas("" + charId, moveTypeId, moveId,
							pngFileName));
		}
	}*/
	
	private BufferedImage fetchImageFromWeb(String filepath, String weblink){
		URL url;
		BufferedImage img = null;
		try {
			url = new URL(weblink);
			img = ImageIO.read(url);
			File file = new File(output_destination + filepath);
			ImageIO.write(img, "png", file);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}
	
	public Map<String, Map<String, Map<String, MoveHitBoxData>>> fetchAllCharData(){
		Map<String, Map<String, Map<String, MoveHitBoxData>>> hbData = 
				new HashMap<String, Map<String, Map<String, MoveHitBoxData>>>();
		
		for(int i = 0; i < 20; i++){
			hbData.put(charNames[i], fetchCharData(i));
		}
		
		return hbData;
	}
	
	public Map<String, Map<String, MoveHitBoxData>> fetchCharData(int charId){
		String sCharId = ""+charId;
		int move_cnt = 1; int move_max = 0;
		CharSDO _char = fetchCharDataFromFile(charNames[charId]);
		Map<String, Map<String, MoveHitBoxData>> charData = 
				new HashMap<String, Map<String, MoveHitBoxData>>();
		
		if(_char != null){
			move_max = move_max + _char.normals.size();
			charData.put(moveTypeList[0], fetchAllMoveTypeHitBoxData(sCharId, 0, move_cnt, move_max));
			move_cnt = move_max+1;

			move_max = move_max + _char.specials.size();
			charData.put(moveTypeList[1], fetchAllMoveTypeHitBoxData(sCharId, 1, move_cnt, move_max));
			move_cnt = move_max+1;

			move_max = move_max + _char.supers.size();
			charData.put(moveTypeList[2], fetchAllMoveTypeHitBoxData(sCharId, 2, move_cnt, move_max));
			move_cnt = move_max+1;
		
			if(charNames[charId].equals("Yun")){
				move_max = move_max + _char.genei_jin_normals.size();
				charData.put(moveTypeList[3], fetchAllMoveTypeHitBoxData(sCharId, 3, move_cnt, move_max));
				move_cnt = move_max+1;

				move_max = move_max + _char.genei_jin_specials.size();
				charData.put(moveTypeList[4], fetchAllMoveTypeHitBoxData(sCharId, 4, move_cnt, move_max));
				move_cnt = move_max+1;
			}
			
			move_max = move_max + _char.misc.size();
			charData.put(moveTypeList[5], fetchAllOtherHitBoxData(sCharId));
			
		}
		return charData;
	}
	
	public Map<String, MoveHitBoxData> fetchAllOtherHitBoxData(String charId){
		Map<String, String> movs = new HashMap<String, String>();
		for(String miscName : miscNames){
			movs.put(charId, fetchOtherHitBoxData(miscName, charId));
		}
		return movs;
	}
	
	public MoveHitBoxData fetchOtherHitBoxData(String miscName, String charId){
		Document doc;
		try {
			doc = Jsoup.connect(getUrlForMisc(charId, miscName)).get();
			String json = extractJSONFromHTML(doc);
			System.out.println("Retrieved move : " + charId + "-" + miscName);
			return json;
		} catch (SocketTimeoutException e) {
			return fetchOtherHitBoxData(miscName, charId);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public Map<String, String> fetchAllMoveTypeHitBoxData(String charId, int type, int init, int end){
		Map<String, String> movs = new HashMap<String, String>();
		for(int i = init; i <= end; i++){
			movs.put(""+i, fetchMoveHitBoxData(charId, type, i));
		}
		System.out.println();
		return movs;
	}
	
	public String fetchMoveHitBoxData(String charId, int type, int moveId){
			Document doc;
			try {
				doc = Jsoup.connect(getUrlForMove(charId, type, ""+moveId)).get();
				String json = extractJSONFromHTML(doc);
				System.out.println("Retrieved move : " + charId + "-" + type  + "-" + moveId);
				return json;
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

	private String getUrlForMove(String charId, int typeId, String moveId){
		String url =  baseUrl + "iChar=" + charId + "&sMoveType=" + moveTypeList[typeId] + "&sAction=w&iMove=" + moveId;
		System.out.println("Fetching:" + url);
		return url;
	}
	
	private String getUrlForMisc(String charId, String miscId){
		String url =   baseUrl + "iChar=" + charId + "&iMove="+miscId+"&sMoveType="+miscId;
		System.out.println("Fetching:" + url);
		return url;
	}
	
	private String getUrlForMoveCanvas(String charId, int moveTypeId, String moveId, String pngFileName){
		if (charId.length() == 1) {
			charId = "0" + charId;
		}

		if (moveId.length() == 1) {
			moveId = "00" + moveId;
		} else if (moveId.length() == 2) {
			moveId = "0" + moveId;
		}
		String weblink = "http://ensabahnur.free.fr/___dataRepository/"
				+ charId + "/" + moveTypeList[moveTypeId] + "/" + moveId + "/"
				+ pngFileName;
		return weblink;
	}
	
	private String getUrlForOtherCanvas(String charId, String moveId, String pngFileName){
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
    
	private void writeAllHitBoxesToFile(String outputFolder, Map<String, Map<String, Map<String, String>>> data){
    	for (Entry<String, Map<String, Map<String, String>>> entry : data.entrySet()) {
    		writeCharHitBoxesToFile(outputFolder + entry.getKey() + "/", entry.getValue());
    	}
    }
    
    private void writeCharHitBoxesToFile(String outputFolder, Map<String, Map<String, String>> data){
    	for (Entry<String, Map<String, String>> entry : data.entrySet()) {
    		writeMovesHitBoxesToFile(outputFolder + entry.getKey() + "/", entry.getValue());
    	}
    }
    
    private void writeMovesHitBoxesToFile(String outputFolder, Map<String, String> data){
    	for (Entry<String, String> entry : data.entrySet()) {
    		writeMoveHitBoxesToFile(outputFolder + entry.getKey() + "/", entry.getValue());
    	}
    }
    
	private void writeMoveHitBoxesToFile(String fileOutput, String data){
        try {
			File file = new File(fileOutput);
 
			// if file or folder doesnt exists, then create it
			if (!file.exists()) {
				file.mkdirs();
			}

        	fileOutput += "hbdata.txt";		
			file = new File(fileOutput);	
        	if (!file.exists()) {
    			file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(data);
		    
			bw.close();
 
			System.out.println("Done writing to file:" + fileOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
