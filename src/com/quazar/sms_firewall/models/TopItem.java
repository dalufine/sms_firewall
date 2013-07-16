package com.quazar.sms_firewall.models;


public class TopItem {
	public enum TopCategory {
		SPAM, FRAUD, WORD
	};

	private int id, pos, votes;
	private String phoneName;
	private TopCategory type;

	public TopItem(int id, int pos, int votes, String phoneName, int type) {
		this.id = id;
		this.pos = pos;
		this.votes = votes;
		this.phoneName = phoneName;
		for (TopCategory tt : TopCategory.values()) {
			if (tt.ordinal() == type)
				this.type = tt;
		}
	}

	public int getId() {
		return id;
	}

	public int getPos() {
		return pos;
	}

	public int getVotes() {
		return votes;
	}

	public String getPhoneName() {
		return phoneName;
	}

	public TopCategory getType() {
		return type;
	}

}
