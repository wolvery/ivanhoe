package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;

public class DocumentVersionMsg extends Message
{
    private final DocumentVersion version;
    
    public DocumentVersionMsg( DocumentVersion version )
    {
        super(MessageType.DOCUMENT_VERSION);
        this.version = version;        
    }

    public DocumentVersion getVersion()
    {
        return version;
    }
    
    public String toString()
    {
       String versionMessage;
       
       if( version != null )
       {
           versionMessage = this.version.toString();          
       }
       else
       {
           versionMessage = "null";
       }
       
       return super.toString() + " Document Version [" + versionMessage + "]";
    }
}
