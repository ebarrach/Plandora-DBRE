package com.pandora;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import com.pandora.helper.DateUtil;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class EdiDataTO extends TransferObject {

	private static final long serialVersionUID = 1L;

	private String title;
	
	private Date publishedDate;
	
	private String link;
			
	private String author;
	
	private String description;
	
	private String status;

	private ArrayList<String> additionalInfo;
			
	
	public SyndEntry toSyndEntry() {
		SyndEntry entry = new SyndEntryImpl();
		
		SyndContent desc = new SyndContentImpl();
		desc.setType("text/html");
		desc.setValue(this.description);
    	entry.setDescription(desc);

    	entry.setTitle(title);
		entry.setPublishedDate( publishedDate );
		entry.setLink(link);
		entry.setAuthor(author);

    	if (additionalInfo!=null && additionalInfo.size()>0 ) {
    		ArrayList<SyndCategory> addinfos = new ArrayList<SyndCategory>();
    		for (String ainfo : additionalInfo) {
    			SyndCategoryImpl sci = new SyndCategoryImpl();
    			sci.setName(ainfo);
    			sci.setTaxonomyUri("");
    			addinfos.add(sci);		
    		}
    		entry.setCategories(addinfos);
    	}

		return entry;
	}
	
	public JSONObject toJsonObject() {
		JSONObject entry = new JSONObject();
		entry.put("title", title);
		if (publishedDate!=null) {
			entry.put("published_date", DateUtil.getDate(new Timestamp(publishedDate.getTime()), 
					"yyyy-MM-dd HH:mm:ss", new Locale("pt", "BR") ));
		}
		entry.put("author", author);
		entry.put("description", description);
		if (additionalInfo!=null) {
			String category = additionalInfo.get(0);
			entry.put("category", category);
		}
		entry.put("status", status);
		
		return entry;
	}
	
	
	public void addAdditionalInfo(String category) {
		if (additionalInfo==null) {
			additionalInfo = new ArrayList<String>();
		}
		additionalInfo.add(category);
	}
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date setPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date pushishDate) {
		this.publishedDate = pushishDate;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
}
