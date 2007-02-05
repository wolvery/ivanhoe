/*
 * Created on Jul 6, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.util.Collections;
import java.util.Map;

import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author benc
 */
public class MoveSubmitMsg extends MoveMsg
{
    private final Map documentVersionOrigins;
    
    /**
     * The move to be submitted along with the relation of the newly created
     * document version IDs to the previously existing document version IDs of
     * their parents
     * 
     * @param move
     *          The move to be submitted
     */
    public MoveSubmitMsg(Move move, Map documentVersionOrigins)
    {
        super(move, MessageType.MOVE_SUBMIT);
        this.documentVersionOrigins = Collections.unmodifiableMap(documentVersionOrigins);
    }
    
    protected MoveSubmitMsg(Move move, Map documentVersionOrigins, MessageType messageType )
    {
        super(move, messageType);
        this.documentVersionOrigins = Collections.unmodifiableMap(documentVersionOrigins);
    }
    
    public Map getDocumentVersionOrigins()
    {
        return documentVersionOrigins;
    }
}
