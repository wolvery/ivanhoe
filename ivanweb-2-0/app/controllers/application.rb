# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  include LoginSystem

  # Pick a unique cookie name to distinguish our session data from others'
  session :session_key => '_ivanweb_session_id'
  
  def game_creator_filter
    (session['user'] and session['user'].new_game_permission)
  end
  
  def admin_filter
    (session['user'] and session['user'].admin)
  end
  
end
