package com.stonedonkey.shackdroid;

import java.io.Serializable;

public class ShackMessage implements Serializable{


	private static final long serialVersionUID = 1L;
	private String name;
	private String msgSubject;
	private String msgDate;
	private String msgText;
	private String msgID;
	private String messageStatus;

	public ShackMessage(String name, String msgSubject, String msgDate, String msgText, String msgID, String messageStatus)
	{
		this.name = name;
		this.msgSubject = msgSubject;
		this.msgDate = msgDate;
		this.msgText = msgText;
		this.msgID = msgID;
		this.messageStatus = messageStatus;
	}

	public String getName() {
		return name;
	}

	public String getMsgSubject() {
		return msgSubject;
	}

	public String getMsgDate() {
		return msgDate;
	}

	public String getMsgText() {
		return msgText;
	}

	public String getMsgID() {
		return msgID;
	}

	public String getMessageStatus() {
		return messageStatus;
	}
	
}
