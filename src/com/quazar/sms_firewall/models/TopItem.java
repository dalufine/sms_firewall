package com.quazar.sms_firewall.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.util.Log;

public class TopItem {
	public enum TopCategory {
		GENERIC, SPAM, FRAUD
	};

	public enum TopType {
		PHONE_NAME, WORD, GENERIC
	};

	private int id, pos, votes;
	private String value;
	private List<String> examples;
	private TopType type;
	private TopCategory category;

	public TopItem(int id, int pos, int votes, String value, List<String> examples, int type, int category) {
		this.id = id;
		this.pos = pos;
		this.votes = votes;
		this.value = value;
		this.examples=examples;
		this.category = TopCategory.values()[category];
		this.type = TopType.values()[type];
	}

	public TopItem(int pos, JSONObject obj) {
		try {
			this.id = obj.getInt("id");
			this.pos = pos;
			this.votes = obj.getInt("votes");
			this.value = obj.getString("value");
			this.examples = new ArrayList<String>();
			//TODO proccess array
			examples.add(obj.getString("example"));
			this.category = TopCategory.values()[obj.getInt("category")];
			this.type = TopType.values()[obj.getInt("type")];
		} catch (Exception ex) {
			Log.e("top", ex.toString());
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

	public String getValue() {
		return value;
	}

	public List<String> getExamples() {
		return examples;
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
		result = prime * result + ((examples == null) ? 0 : examples.hashCode());
		result = prime * result + id;
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
		TopItem other = (TopItem) obj;
		if (category != other.category)
			return false;
		if (examples == null) {
			if (other.examples != null)
				return false;
		} else if (!examples.equals(other.examples))
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
