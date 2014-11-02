package com.quazar.sms_firewall.models;

import org.json.JSONObject;

import android.util.Log;


public class TopFilter {
	public enum TopCategory {
		GENERIC, SPAM, FRAUD
	};

	public enum TopType {
		PHONE_NAME, WORD, GENERIC
	};

	private long id; 
	private int pos, votes;
	private String value;
	private TopType type;
	private TopCategory category;

	public TopFilter(long id, int pos, int votes, String value, int type, int category) {
		this.id = id;
		this.pos = pos;
		this.votes = votes;
		this.value = value;
		this.category = TopCategory.values()[category];
		this.type = TopType.values()[type];
	}

	public TopFilter(int pos, JSONObject obj) {
		try {
			this.id = obj.getInt("id");
			this.pos = pos;
			this.votes = obj.getInt("votes");
			this.value = obj.getString("value");			
			this.category = TopCategory.values()[obj.getInt("category")];
			this.type = TopType.values()[obj.getInt("type")];
		} catch (Exception ex) {
			Log.e("top", ex.toString());
		}
	}

	public long getId() {
		return id;
	}

	public int getPos() {
		return pos;
	}

	public int getVotes() {
		return votes;
	}

	public String getValue() {
		return value;
	}


	public TopType getType() {
		return type;
	}

	public TopCategory getCategory() {
		return category;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result + (int)id;
		result = prime * result + pos;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + votes;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopFilter other = (TopFilter) obj;
		if (category != other.category)
			return false;
		if (id != other.id)
			return false;
		if (pos != other.pos)
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (votes != other.votes)
			return false;
		return true;
	}
	@Override
	public String toString() {		
		return String.format("id:%d, position:%d, votes:%d, value:%s", id, pos, votes, value);
	}
	
	
}
