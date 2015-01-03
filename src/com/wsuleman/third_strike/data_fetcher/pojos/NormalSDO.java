/**
 * 
 */
package com.wsuleman.third_strike.data_fetcher.pojos;

/**
 * @author Waseem Suleman
 *
 * Simple POJO to store the properties of a move.
 */
public class NormalSDO {
	public String name;
	public String startup;
	public String hit;
	public String recovery;
	public String block_advantage;
	public String hit_advantage;
	public String crouch_hit_advantage;
	public CancelableSDO cancel;
	public ParrySDO parry;
	public String kara_range;
	public String throw_range;
	public String damage;
	public String stun;
	public BarGainSelfSDO bar_gain_self;
	public BarGainOppSDO bar_gain_opp;
}
