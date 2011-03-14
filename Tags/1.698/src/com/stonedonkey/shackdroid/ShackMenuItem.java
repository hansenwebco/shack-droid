package com.stonedonkey.shackdroid;




public class ShackMenuItem {
	private String menuTitle;
	private String menuSubTitle;
	private int icon;
	
	public ShackMenuItem(String menuTitle, String menuSubTitle, int i)
	{
		this.icon = i;
		this.menuSubTitle = menuSubTitle;
		this.menuTitle = menuTitle;
		
	}
	
	public ShackMenuItem(String menuTitle, String menuSubTitle)
	{
		this.menuSubTitle = menuSubTitle;
		this.menuTitle = menuTitle;
		this.icon = 0;
	}
	
	public String getMenuTitle() {
		return menuTitle;
	}


	public String getMenuSubTitle() {
		return menuSubTitle;
	}

	public int getMenuIcon() {
		return icon;
	}


	
	
	
}
