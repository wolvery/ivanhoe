# Methods added to this helper will be available to all templates in the application.
module ApplicationHelper
  
  def logged_in?
    not @current_user.nil?
  end
  
  def current_user
    @current_user
  end

  def admin?
    ( logged_in? and @current_user.admin )
  end
  
  def can_create_game?
    ( logged_in? and @current_user.new_game_permission )
  end
  
  # a user can edit a particular game if they are the creator of the game or an admin
  def can_edit_game?(game_id)
    ( admin? or ( logged_in? and @current_user.can_edit_game?(game_id) ) )
  end

end
