/*
 * Created on Jan 12, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import java.io.File;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * Wrapper class to contain doc info and raw file info
 */
class NewDocumentRecord
{
	private File rawSourceFile;
	private DocumentInfo documentInfo;
	
	public NewDocumentRecord(File rawFile, DocumentInfo docInfo)
	{
		this.documentInfo = docInfo;
		this.rawSourceFile = rawFile;
	}
	
	public File getSourceFile()
	{
		return this.rawSourceFile;
	}
	
	public DocumentInfo getDocumentInfo()
	{
		return this.documentInfo;
	}
}