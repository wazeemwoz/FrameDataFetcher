package com.wsuleman.third_strike.data_fetcher.pojos;

/**
 * @author Waseem Suleman
 * 
 * Simple POJO for properties of a special move.
 */
public class SpecialSDO{
	public String name;
	public String motion;
	public String startup;
	public String hit;
	public String recovery;
	public String block_advantage;
	public CancelableSDO cancel;
	public ParrySDO parry;
	public String throw_range;
	public String damage;
	public String stun;
	public BarGainSelfSDO bar_gain_self;
	public BarGainOppSDO bar_gain_opp;
}
