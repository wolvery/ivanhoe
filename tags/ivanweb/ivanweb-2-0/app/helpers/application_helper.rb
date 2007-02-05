# Methods added to this helper will be available to all templates in the application.
module ApplicationHelper

  def admin?
    ( session['user'] and session['user'].admin )
  end
  
  def can_create_game?
    ( session['user'] and session['user'].new_game_permission )
  end
  
  # a user can edit a particular game if they are the creator of the game or an admin
  def can_edit_game?(game_id)
    ( admin? or (session['user'] and session['user'].can_edit_game?(game_id)) )
  end

end
