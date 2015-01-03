/**
 * 
 */
package com.wsuleman.third_strike.data_fetcher.pojos;

import java.util.List;
import java.util.Map;

/**
 * @author Waseem Suleman
 *
 * Simple POJO to store the frame data of a character.
 */
public class CharSDO {
	/*
	 * Required
	 */
	public List<NormalSDO> normals;
	public List<SpecialSDO> specials;
	public List<SuperSDO> supers;
	public Map<String, String> misc;
	
	/*
	 * Only for Yun
	 */
	public List<GeneiJinNormalSDO> genei_jin_normals;
	public List<GeneiJinSpecialSDO> genei_jin_specials;
}
