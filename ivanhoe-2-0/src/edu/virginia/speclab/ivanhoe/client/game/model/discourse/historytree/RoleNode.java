/*
 * Created on Sep 21, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

public class RoleNode extends DocumentStateNode
{
    private String roleName;

    public RoleNode(String roleName, boolean showDocStates)
    {
        super(roleName, showDocStates, true);
        this.roleName = roleName;
    }

    /**
     * @return Returns the userName.
     */
    public String getRoleName()
    {
        return roleName;
    }
}