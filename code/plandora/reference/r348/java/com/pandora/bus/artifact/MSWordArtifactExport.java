package com.pandora.bus.artifact;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import com.pandora.helper.XmlDomParse;

public class MSWordArtifactExport extends ArtifactExport {
	
	
	@Override
	public byte[] export(String header, String body, String footer) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(body.getBytes());
		FileOutputStream fos = new FileOutputStream("simpleTable.docx");
		//XWPFDocument doc = new XWPFDocument();
		byte[] response = null;
		
		try {			
			Tidy tidy = new Tidy();             
			Document docHtml = tidy.parseDOM(bis, null);
			XmlDomParse.write(docHtml, "artifact.xml");
			//TODO to be implemented...
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if (fos!=null) {
				fos.close();
			}
			if (bis!=null) {
				bis.close();
			}
		}
		return response;	
	}

	
	@Override
	public String getContentType() {
		return "application/msword";
	}
	

	@Override
	public String getExtension() {
		return ".docx";
	}
	

	@Override
	public String getUniqueName() {
		return "label.artifactTag.export.msword";
	}	
}
