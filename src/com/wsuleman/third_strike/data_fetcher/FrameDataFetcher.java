/**
 * 
 */
package com.wsuleman.third_strike.data_fetcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.wsuleman.third_strike.data_fetcher.pojos.BarGainOppSDO;
import com.wsuleman.third_strike.data_fetcher.pojos.BarGainSelfSDO;
import com.wsuleman.third_strike.data_fetcher.pojos.CancelableSDO;
import com.wsuleman.third_strike.data_fetcher.pojos.CharSDO;
import com.wsuleman.third_strike.data_fetcher.pojos.GeneiJinNormalSDO;
import com.wsuleman.third_strike.data_fetcher.pojos.GeneiJinSpecialSDO;
import com.wsuleman.third_strike.data_fetcher.pojos.NormalSDO;
import com.wsuleman.third_strike.data_fetcher.pojos.ParrySDO;
import com.wsuleman.third_strike.data_fetcher.pojos.SpecialSDO;
import com.wsuleman.third_strike.data_fetcher.pojos.SuperSDO;

/**
 * @author Waseem Suleman
 * Using the jsoup library this class will collect data from
 * HTML files to acquire third strike frame data.
 * 
 * Currently set up to translate ESN's website data into json files.
 */
public class FrameDataFetcher {
	private CharSDO char_data;
	private String charId;
	private String char_name;
	private String path;
	
	public FrameDataFetcher(String path, String charId){
		this.path = path;
		char_name = "";
		this.charId = charId;
		char_data = new CharSDO();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "C:\\Documents and Settings\\Waseem\\Desktop\\ESN\\";
		for(int i = 1; i < 21; i++){
			String cId = ""+i;
			if(i < 10){
				cId = "0" + cId;
			}
			FrameDataFetcher f = new FrameDataFetcher(path, cId);
			f.translate();
		}
	}

	public void translate(){
		convert(path + charId + "_01.html");
		convert(path + charId + "_02.html");
		convert(path + charId + "_03.html");
		convert(path + charId + "_04.html");
		
		if(char_name.equals("Yun")){
			convert(path + charId + "_05.html");
			convert(path + charId + "_06.html");
		}
		Gson gson = new Gson();
		String json = gson.toJson(char_data);
		writeCharJsonToFile(json, path);
	}
	
	private void convert(String htmlFile){
		File input = new File(htmlFile);
		try {
			Document doc = Jsoup.parse(input, "UTF-8", "");
			if(char_name.equals("")){
				Element topTitle = doc.select("#topTitle").first();
				char_name = topTitle.text().split("/")[0].trim();
			}

			Element topTypeData = doc.select("#topTypeData").first();
			String dataType = topTypeData.text().trim();
			
			if(dataType.equals("'Normals frames data (revised data)'")){
				translateNormals(doc);
			}else if(dataType.equals("'Specials frames data (revised data)'")){
				translateSpecials(doc);
			}else if(dataType.equals("'Super Arts frames data (revised data)'")){
				translateSupers(doc);
			}else if(dataType.equals("'GeneiJin Normals frames data (revised data)'")){
				translateGJNormals(doc);
			}else if(dataType.equals("'GeneiJin Specials frames data (revised data)'")){
				translateGJSpecials(doc);
			}else if(dataType.equals("'Misc'")){
				translateMisc(doc);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void translateNormals(Document doc){
		Element table_head = doc.select("tbody").first();
		Elements rows = table_head.select("tr");
		char_data.normals = new ArrayList<NormalSDO>();
		for( Element row : rows)
		{
			NormalSDO normal = new NormalSDO();
			Element data = row.select("td").first().nextElementSibling();
			normal.name = data.text().trim();
			data = data.nextElementSibling();
			normal.startup = data.text().trim();
			data = data.nextElementSibling();
			normal.hit = data.text().trim();
			data = data.nextElementSibling();
			normal.recovery = data.text().trim();
			data = data.nextElementSibling();
			normal.block_advantage = data.text().trim();
			data = data.nextElementSibling();
			normal.hit_advantage = data.text().trim();
			data = data.nextElementSibling();
			normal.crouch_hit_advantage = data.text().trim();
			data = data.nextElementSibling();
			normal.cancel = translateCancelData(data);
			data = data.nextElementSibling();
			normal.parry = translateParryData(data.text().trim());
			data = data.nextElementSibling();
			normal.kara_range = data.text().trim();
			data = data.nextElementSibling();
			normal.throw_range = data.text().trim();
			data = data.nextElementSibling();
			normal.damage = data.text().trim();
			data = data.nextElementSibling();
			normal.stun = data.text().trim();
			data = data.nextElementSibling();
			normal.bar_gain_self = translateSelfBarGain(data.text().trim());
			data = data.nextElementSibling();
			normal.bar_gain_opp = translateOppBarGain(data.text().trim());
			char_data.normals.add(normal);
		}
	}
	
	private void translateSpecials(Document doc){
		Element table_head = doc.select("tbody").first();
		Elements rows = table_head.select("tr");
		char_data.specials = new ArrayList<SpecialSDO>();
		for( Element row : rows)
		{
			SpecialSDO move = new SpecialSDO();
			Element data = row.select("td").first().nextElementSibling();
			move.name = data.text().trim();
			data = data.nextElementSibling();
			move.motion = data.text().trim();
			data = data.nextElementSibling();
			move.startup = data.text().trim();
			data = data.nextElementSibling();
			move.hit = data.text().trim();
			data = data.nextElementSibling();
			move.recovery = data.text().trim();
			data = data.nextElementSibling();
			move.block_advantage = data.text().trim();
			data = data.nextElementSibling();
			move.cancel = translateCancelData(data);
			data = data.nextElementSibling();
			move.parry = translateParryData(data.text().trim());
			data = data.nextElementSibling();
			move.throw_range = data.text().trim();
			data = data.nextElementSibling();
			move.damage = data.text().trim();
			data = data.nextElementSibling();
			move.stun = data.text().trim();
			data = data.nextElementSibling();
			move.bar_gain_self = translateSelfBarGain(data.text().trim());
			data = data.nextElementSibling();
			move.bar_gain_opp = translateOppBarGain(data.text().trim());
			char_data.specials.add(move);
		}
	}
	
	private void translateGJNormals(Document doc){
		Element table_head = doc.select("tbody").first();
		Elements rows = table_head.select("tr");
		char_data.genei_jin_normals = new ArrayList<GeneiJinNormalSDO>();
		for( Element row : rows)
		{
			GeneiJinNormalSDO normal = new GeneiJinNormalSDO();
			Element data = row.select("td").first().nextElementSibling();
			normal.name = data.text().trim();
			data = data.nextElementSibling();
			normal.startup = data.text().trim();
			data = data.nextElementSibling();
			normal.hit = data.text().trim();
			data = data.nextElementSibling();
			normal.recovery = data.text().trim();
			data = data.nextElementSibling();
			normal.block_advantage = data.text().trim();
			data = data.nextElementSibling();
			normal.hit_advantage = data.text().trim();
			data = data.nextElementSibling();
			normal.crouch_hit_advantage = data.text().trim();
			data = data.nextElementSibling();
			normal.cancel = translateCancelData(data);
			data = data.nextElementSibling();
			normal.parry = translateParryData(data.text().trim());
			data = data.nextElementSibling();
			normal.kara_range = data.text().trim();
			data = data.nextElementSibling();
			normal.throw_range = data.text().trim();
			data = data.nextElementSibling();
			normal.damage = data.text().trim();
			data = data.nextElementSibling();
			normal.stun = data.text().trim();
			data = data.nextElementSibling();
			normal.bar_gain_opp = translateOppBarGain(data.text().trim());
			char_data.genei_jin_normals.add(normal);
		}
	}
	
	private void translateGJSpecials(Document doc){
		Element table_head = doc.select("tbody").first();
		Elements rows = table_head.select("tr");
		char_data.genei_jin_specials = new ArrayList<GeneiJinSpecialSDO>();
		for( Element row : rows)
		{
			GeneiJinSpecialSDO move = new GeneiJinSpecialSDO();
			Element data = row.select("td").first().nextElementSibling();
			move.name = data.text().trim();
			data = data.nextElementSibling();
			move.motion = data.text().trim();
			data = data.nextElementSibling();
			move.startup = data.text().trim();
			data = data.nextElementSibling();
			move.hit = data.text().trim();
			data = data.nextElementSibling();
			move.recovery = data.text().trim();
			data = data.nextElementSibling();
			move.block_advantage = data.text().trim();
			data = data.nextElementSibling();
			move.cancel = translateCancelData(data);
			data = data.nextElementSibling();
			move.parry = translateParryData(data.text().trim());
			data = data.nextElementSibling();
			move.throw_range = data.text().trim();
			data = data.nextElementSibling();
			move.damage = data.text().trim();
			data = data.nextElementSibling();
			move.stun = data.text().trim();
			data = data.nextElementSibling();
			move.bar_gain_opp = translateOppBarGain(data.text().trim());
			char_data.genei_jin_specials.add(move);
		}
	}
	
	private void translateSupers(Document doc){
		Element table_head = doc.select("tbody").first();
		Elements rows = table_head.select("tr");
		char_data.supers = new ArrayList<SuperSDO>();
		for( Element row : rows)
		{
			SuperSDO move = new SuperSDO();
			Element data = row.select("td").first().nextElementSibling();
			move.name = data.text().trim();
			data = data.nextElementSibling();
			move.motion = data.text().trim();
			data = data.nextElementSibling();
			move.startup = data.text().trim();
			data = data.nextElementSibling();
			move.hit = data.text().trim();
			data = data.nextElementSibling();
			move.recovery = data.text().trim();
			data = data.nextElementSibling();
			move.block_advantage = data.text().trim();
			data = data.nextElementSibling();
			move.cancel = translateCancelData(data);
			data = data.nextElementSibling();
			move.parry = translateParryData(data.text().trim());
			data = data.nextElementSibling();
			move.throw_range = data.text().trim();
			data = data.nextElementSibling();
			move.damage = data.text().trim();
			data = data.nextElementSibling();
			move.stun = data.text().trim();
			data = data.nextElementSibling();
			char_data.supers.add(move);
		}
	}

	private void translateMisc(Document doc){
		Elements miscs = doc.select("dd").select(".miscFd");
		char_data.misc = new HashMap<String, String>();
		for(Element row : miscs)
		{
			String[] data = row.text().trim().split(":");
			char_data.misc.put(data[0].trim(), data[1].trim());
		}
	}
	
	private CancelableSDO translateCancelData(Element td){
		CancelableSDO cancelable = new CancelableSDO();
		Elements cancels = td.select("div").select(":not([title=\"??\"])").select(":not(.BcancelBit_X)");
		for(Element cancel : cancels){
			if(cancel.attr("title").equals("SA")){
				cancelable._super = true;
			}else if(cancel.attr("title").equals("SP")){
				cancelable._special = true;
			}else if(cancel.attr("title").equals("SC")){
				cancelable._self = true;
			}else if(cancel.attr("title").equals("NC")){
				cancelable._chain = true;
			}else if(cancel.attr("title").equals("DC")){
				cancelable._dash = true;
			}else if(cancel.attr("title").equals("SJ")){
				cancelable._sjump = true;
			}
		}
		return cancelable;
	}
	
	private ParrySDO translateParryData(String data){
		ParrySDO parry = new ParrySDO();
		
		if(data.contains("H"))
			parry.high = true;
		
		if(data.contains("L"))
			parry.low = true;
		
		return parry;
	}
	
	private BarGainSelfSDO translateSelfBarGain(String val){
		BarGainSelfSDO bar = new BarGainSelfSDO();
		if(val.equals("N/A"))
			return bar;
		String[] vals = val.trim().split("/");
		if(vals.length == 3){
			if(!vals[0].trim().equals("---"))
				bar.wiff = Integer.parseInt(vals[0].trim());
			if(!vals[1].trim().equals("---"))
				bar.hit = Integer.parseInt(vals[1].trim());
			if(!vals[2].trim().equals("---"))
				bar.block = Integer.parseInt(vals[2].trim());
		}
		return bar;
	}
	
	private BarGainOppSDO translateOppBarGain(String val){
		BarGainOppSDO bar = new BarGainOppSDO();
		if(val.equals("N/A"))
			return bar;
		String[] vals = val.trim().split("/");
		if(vals.length == 2){
			if(!vals[0].trim().equals("---"))
				bar.hit = Integer.parseInt(vals[0].trim());
			if(!vals[1].trim().equals("---"))
				bar.block = Integer.parseInt(vals[1].trim());
		}
		return bar;
	}
	
	private void writeCharJsonToFile(String char_json, String folderPath){
		String filePath = folderPath + "extracted\\" + char_name + ".txt";
        try {
			//System.out.println("Writing to file");
			File file = new File(filePath);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				//System.out.println("Created file");
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(char_json);
		    
			bw.close();
 
			System.out.println("Done writing to file : " + char_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
