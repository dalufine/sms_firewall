package com.quazar.sms_firewall.models;


public class TopItemModel {
	public enum TopTypes {
		SPAM, FRAUD, WORD
	};

	private int id, pos, votes;
	private String phoneName;
	private TopTypes type;

	public TopItemModel(int id, int pos, int votes, String phoneName, int type) {
		this.id = id;
		this.pos = pos;
		this.votes = votes;
		this.phoneName = phoneName;
		for (TopTypes tt : TopTypes.values()) {
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

	public TopTypes getType() {
		return type;
	}

}
