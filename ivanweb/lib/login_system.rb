
module LoginSystem 
  
  protected
  
  # overwrite this method if you only want to protect certain actions of the controller
  # example:
  # 
  #  # don't protect the login and the about method
  #  def protect?(action)
  #    if ['action', 'about'].include?(action)
  #       return false
  #    else
  #       return true
  #    end
  #  end
  def protect?(action)
    true
  end
   
  # login_required filter. add 
  #
  #   before_filter :login_required
  #
  # if the controller should be under any rights management. 
  # for finer access control you can overwrite
  #   
  #   def authorize?(user)
  # 
  def login_required
    
    if not protect?(action_name)
      return true  
    end

    if !session['user'].nil? 
      @current_user = Player.find_by_id(session['user'])
      return true unless @current_user.nil?
    end

    # store current location so that we can 
    # come back after the user logged in
    store_location
  
    # call overwriteable reaction to unauthorized access
    access_denied
    return false 
  end

  # overwrite if you want to have special behavior in case the user is not authorized
  # to access the current operation. 
  # the default action is to redirect to the login screen
  # example use :
  # a popup window might just close itself for instance
  def access_denied
    redirect_to :controller=>"account", :action =>"login"
  end  
  
  # store current uri in  the session.
  # we can return to this location by calling return_location
  def store_location
    session['return-to'] = request.request_uri
  end

  # move to the last store_location call or to the passed default one
  def redirect_back_or_default(default)
    if session['return-to'].nil?
      redirect_to default
    else
      redirect_to_url session['return-to']
      session['return-to'] = nil
    end
  end

end