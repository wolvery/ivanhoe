module AccountHelper

def logged_in?
    @current_user
  end
  
  def current_user
    @current_user
  end

  def admin?
    ( @current_user and @current_user.admin )
  end
  
  def can_create_game?
    ( @current_user and @current_user.new_game_permission )
  end
  
  # a user can edit a particular game if they are the creator of the game or an admin
  def can_edit_game?(game_id)
    ( admin? or ( @current_user and @current_user.can_edit_game?(game_id) ) )
  end

end
